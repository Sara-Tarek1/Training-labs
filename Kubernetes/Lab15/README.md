# Lab 15: Node.js Application Deployment with ClusterIP Service

## 1. Objective

Deploy a Node.js application on Kubernetes using a Deployment with two replicas, connect it to a MySQL database, mount persistent storage, and expose the application internally using a ClusterIP Service.

## 2. Prerequisites

Resources created in previous labs:

- **Lab 8:** Custom Docker image `saratarek1/ivolve-app:lab8`
- **Lab 11:** Namespace `ivolve` and ResourceQuota
- **Lab 12:** ConfigMap `ivolvecm` and Secret `ivolvesecret`
- **Lab 13:** PersistentVolume `ivolvepv`
- **Lab 14:** MySQL StatefulSet `ivolvedb` and headless Service `ivolvedbsvc`

## 3. Project Files

| File | Description |
|---|---|
| `deployment.yaml` | Application Deployment and init container |
| `service.yaml` | ClusterIP Service |
| `pvc.yaml` | PersistentVolumeClaim |
| `README.md` | Lab documentation |

## 4. Step-by-Step Implementation

### Step 1: Verify the Kubernetes Environment

Check that the cluster nodes are running:

```bash
kubectl get nodes
```

The worker node `minikube-m02` has the label:

```text
project=ivolve
```

It also has the taint:

```text
workload=ivolveApp:NoSchedule
```

These settings control where the application Pods can run.

### Step 2: Verify Existing Resources

Check the ConfigMap and Secret:

```bash
kubectl get configmap,secret -n ivolve
```

The existing ConfigMap contains:

```yaml
DB_HOST: mysql
DB_USER: ivolveuser
```

The MySQL Service created in Lab14 is named `ivolvedbsvc`, so the database host was updated:

```bash
kubectl patch configmap ivolvecm -n ivolve --type merge -p '{"data":{"DB_HOST":"ivolvedbsvc"}}'
```

Verify the updated values:

```bash
kubectl get configmap ivolvecm -n ivolve -o yaml
```

Output:

```yaml
DB_HOST: ivolvedbsvc
DB_USER: ivolveuser
```

### Step 3: Prepare the PersistentVolume

The PV created in Lab13 used `hostPath` `/mnt/app-logs` and was originally bound to a PVC in the `default` namespace.

Since the application runs in the `ivolve` namespace and on the worker node, the existing data had to be preserved and the PV rebound.

First, verify the original log:

```bash
kubectl exec logging-pod -n default -- cat /mnt/app-logs/app.log
```

Output:

```text
Lab13 log entry
```

Copy the file locally:

```bash
kubectl cp default/logging-pod:/mnt/app-logs/app.log ./app.log
```

Create the directory on the worker:

```bash
minikube ssh --node=minikube-m02 -- sudo mkdir -p /mnt/app-logs
```

Copy the file to the worker:

```bash
minikube cp ./app.log minikube-m02:/mnt/app-logs/app.log
```

Verify the copied file:

```bash
minikube ssh --node=minikube-m02 -- cat /mnt/app-logs/app.log
```

Output:

```text
Lab13 log entry
```

Delete the old Pod and PVC:

```bash
kubectl delete pod logging-pod -n default
kubectl delete pvc ivolvepvc -n default
```

The PV uses the `Retain` reclaim policy, so its data is preserved.

Check its status:

```bash
kubectl get pv ivolvepv
```

Output:

```text
STATUS: Released
```

Remove the old claim reference:

```bash
kubectl patch pv ivolvepv -p '{"spec":{"claimRef":null}}'
```

The PV then became `Available`.

### Step 4: Create a New PVC in the ivolve Namespace

Create `pvc.yaml`:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: ivolvepvc
  namespace: ivolve
spec:
  volumeName: ivolvepv
  storageClassName: ""
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 1Gi
```

The empty `storageClassName` prevents Kubernetes from assigning the default StorageClass.

Apply the PVC:

```bash
kubectl apply -f pvc.yaml
```

Verify:

```bash
kubectl get pv,pvc -n ivolve
```

Result:

```text
ivolvepv    1Gi    RWX    Retain    Bound    ivolve/ivolvepvc
```

The PVC was successfully bound to the original Lab13 PV.

### Step 5: Create the Application Deployment

Create `deployment.yaml`.

The Deployment configuration includes:

- Two application replicas.
- Custom Docker Hub image from Lab8.
- ConfigMap and Secret environment variables.
- Node selector and toleration.
- Init container using the MySQL client image.
- PersistentVolumeClaim mount.

The init container connects to MySQL as root, creates the `ivolve` database if it does not exist, and grants privileges to `ivolveuser`.

Apply the Deployment:

```bash
kubectl apply -f deployment.yaml
```

Output:

```text
deployment.apps/ivolveapp created
```

Check the Pods:

```bash
kubectl get pods -n ivolve -o wide
```

Output:

```text
NAME                         READY   STATUS    IP           NODE
ivolveapp-67bb9ffd6d-2vv4x   1/1     Running   10.244.1.3   minikube-m02
ivolveapp-67bb9ffd6d-b4q7w   1/1     Running   10.244.1.4   minikube-m02
ivolvedb-0                   1/1     Running   10.244.1.2   minikube-m02
```

Both application replicas are running successfully on the worker node.

### Step 6: Verify the Init Container

Check the init container logs:

```bash
kubectl logs ivolveapp-67bb9ffd6d-2vv4x -n ivolve -c init-database
```

Output:

```text
mysql: [Warning] Using a password on the command line interface can be insecure.
```

The warning is related to passing the password through the command line. No connection or SQL errors appeared.

### Step 7: Verify MySQL Database and User Privileges

Connect to MySQL as root:

```bash
kubectl exec -it ivolvedb-0 -n ivolve -- mysql -u root -p
```

Enter the root password from the Kubernetes Secret when prompted.

Check the databases:

```sql
SHOW DATABASES;
```

Output:

```text
information_schema
ivolve
mysql
performance_schema
sys
```

The `ivolve` database was successfully created.

Check the application user's privileges:

```sql
SHOW GRANTS FOR 'ivolveuser'@'%';
```

Output:

```text
GRANT USAGE ON *.* TO `ivolveuser`@`%`
GRANT ALL PRIVILEGES ON `ivolve`.* TO `ivolveuser`@`%`
```

This confirms that the application user has all privileges on the `ivolve` database.

Exit MySQL:

```sql
exit
```

### Step 8: Verify Application Database Connectivity

Check the application logs:

```bash
kubectl logs ivolveapp-67bb9ffd6d-2vv4x -n ivolve
```

Output:

```text
🔄 Attempting to connect to MySQL...
✅ Connected to MySQL and 'ivolve' DB found.
🚀 Server started on http://0.0.0.0:3000
```

The application successfully connected to MySQL and found the `ivolve` database.

### Step 9: Create the ClusterIP Service

Create `service.yaml`:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: ivolvaappsvc
  namespace: ivolve
spec:
  type: ClusterIP
  selector:
    app: ivolveapp
  ports:
    - protocol: TCP
      port: 3000
      targetPort: 3000
```

Apply the Service:

```bash
kubectl apply -f service.yaml
```

Verify the Service and its endpoints:

```bash
kubectl get svc,endpoints -n ivolve
```

Output:

```text
service/ivolvaappsvc   ClusterIP   10.111.213.237   <none>   3000/TCP
service/ivolvedbsvc    ClusterIP   None             <none>   3306/TCP

endpoints/ivolvaappsvc   10.244.1.3:3000,10.244.1.4:3000
endpoints/ivolvedbsvc    10.244.1.2:3306
```

The application Service successfully discovers both application Pods.

### Step 10: Test the Application

The namespace ResourceQuota was already fully used by the MySQL Pod and two application replicas (3/3), so creating an additional temporary test Pod was rejected.

Instead, use port-forwarding:

```bash
kubectl port-forward svc/ivolvaappsvc 3000:3000 -n ivolve
```

In another terminal, test the health endpoint:

```bash
curl http://localhost:3000/health
```

Output:

```text
🚀 iVolve web app is working! Keep calm and code on! 🎉
```

Test the readiness endpoint:

```bash
curl http://localhost:3000/ready
```

Output:

```text
👍 iVolve web app is ready to rock and roll! 🤘
```

Both endpoints returned successful responses.

## 5. Final Verification

| Requirement | Result |
|---|---|
| Deployment with 2 replicas | Successful |
| Custom Docker Hub image | Successful |
| ConfigMap and Secret | Configured |
| Node selector | Verified |
| Toleration | Verified |
| Lab13 PV and PVC | Bound |
| MySQL init container | Executed |
| `ivolve` database | Created |
| Application user privileges | Verified |
| MySQL connectivity | Successful |
| ClusterIP Service | Created |
| Service endpoints | Both Pods discovered |
| Health endpoint | Successful |
| Readiness endpoint | Successful |

## 6. Conclusion

Successfully deployed the Node.js application on Kubernetes with two replicas, connected it to MySQL, reused persistent storage from Lab13, and exposed it internally through a ClusterIP Service.

The application successfully connected to the `ivolve` database, and both health and readiness endpoints returned the expected responses.

