# Lab 14: StatefulSet with Headless Service

## Objective
Deploy a MySQL database using a Kubernetes StatefulSet and a Headless Service. Configure the database using a ConfigMap and Secret, schedule the Pod on a specific worker node, attach persistent storage, and verify database connectivity and DNS resolution.

## Step 1: Create the ConfigMap and Secret

The ConfigMap and Secret were created in the `ivolve` namespace in Lab12 and reused in this lab.

**ConfigMap:** `ivolvecm`

Contains:
- `DB_HOST`
- `DB_USER`

**Secret:** `ivolvesecret`

Contains:
- `MYSQL_ROOT_PASSWORD`
- `DB_PASSWORD`

The Secret provides database passwords to the container without writing them directly in the StatefulSet manifest.

Verify the resources:

```bash
kubectl get configmap ivolvecm -n ivolve
kubectl get secret ivolvesecret -n ivolve
```

## Step 2: Create the Headless Service

Created `service.yaml` to expose the MySQL Pod through a Headless Service.

```yaml
apiVersion: v1
kind: Service
metadata:
  name: ivolvedbsvc
  namespace: ivolve
spec:
  clusterIP: None
  selector:
    app: ivolvedb
  ports:
    - port: 3306
      targetPort: 3306
```

**Explanation:**
- `clusterIP: None` makes the Service headless.
- `selector` identifies the Pods belonging to the MySQL StatefulSet.
- Port `3306` is the default MySQL port.

Apply the Service:

```bash
kubectl apply -f service.yaml
```

**Output:**

```text
service/ivolvedbsvc created
```

Verify the Service:

```bash
kubectl get service -n ivolve
```

**Output:**

```text
NAME          TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)
ivolvedbsvc   ClusterIP   None         <none>        3306/TCP
```

## Step 3: Create the StatefulSet

Created `statefulset.yaml` to deploy MySQL with one replica, environment variables from the ConfigMap and Secret, and persistent storage.

The configuration includes:

- StatefulSet name: `ivolvedb`
- Namespace: `ivolve`
- MySQL image: `mysql:8.0`
- Replicas: `1`
- Container port: `3306`
- Node selector: `project=ivolve`
- Toleration: `workload=ivolveApp:NoSchedule`
- Persistent volume mount: `/var/lib/mysql`
- Requested storage: `1Gi`

The StatefulSet uses the existing ConfigMap and Secret to configure the MySQL container.

Apply the StatefulSet:

```bash
kubectl apply -f statefulset.yaml
```

**Output:**

```text
statefulset.apps/ivolvedb created
```

Verify the StatefulSet:

```bash
kubectl get statefulset -n ivolve
```

The initial output showed:

```text
NAME       READY   AGE
ivolvedb   0/1     8s
```

The Pod was still being created at this stage.

## Step 4: Verify the MySQL Pod

Check the Pods and the node where MySQL is running:

```bash
kubectl get pods -n ivolve -o wide
```

**Output after the Pod became ready:**

```text
NAME          READY   STATUS    RESTARTS   AGE     IP           NODE
ivolvedb-0    1/1     Running   0          3m10s   10.244.1.2   minikube-m02
```

The StatefulSet created the Pod `ivolvedb-0`, and it reached the `Running` state with `1/1` containers ready.

The Pod runs on `minikube-m02`, which matches the node selector and toleration configuration.

## Step 5: Verify Persistent Storage

Check the PersistentVolumeClaim:

```bash
kubectl get pvc -n ivolve
```

**Output:**

```text
NAME                    STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS
mysql-data-ivolvedb-0   Bound    pvc-5baff486-13f2-4b6d-a046-3b30929b1900   1Gi        RWO            standard
```

The PVC reached the `Bound` status, confirming that Kubernetes successfully provisioned and attached persistent storage for the MySQL Pod.

The volume is mounted at `/var/lib/mysql`, where MySQL stores its database files.

## Step 6: Check MySQL Logs

Check the database logs:

```bash
kubectl logs ivolvedb-0 -n ivolve
```

The logs confirmed that MySQL initialized successfully, created the configured database user, and became ready for connections.

The successful startup message included:

```text
ready for connections
```

## Step 7: Test MySQL Connectivity

Connect to MySQL from inside the Pod:

```bash
kubectl exec -it ivolvedb-0 -n ivolve -- mysql -u ivolveuser -p
```

Enter the configured password when prompted:

```text
IvolvApp123!
```

The MySQL client opened successfully:

```text
Welcome to the MySQL monitor.
Server version: 8.0.46
```

Check the MySQL version:

```sql
SELECT VERSION();
```

**Output:**

```text
8.0.46
```

List the available databases:

```sql
SHOW DATABASES;
```

**Output:**

```text
information_schema
performance_schema
```

This confirmed that the MySQL server accepted a client connection. Only the default system databases were listed because no application database was configured in this lab.

Exit the MySQL client:

```sql
exit;
```

## Step 8: Verify Pod DNS Resolution

Test whether Kubernetes DNS can resolve the StatefulSet Pod's fully qualified domain name:

```bash
kubectl exec ivolvedb-0 -n ivolve -- getent hosts ivolvedb-0.ivolvedbsvc.ivolve.svc.cluster.local
```

**Output:**

```text
10.244.1.2      ivolvedb-0.ivolvedbsvc.ivolve.svc.cluster.local ivolvedb-0
```

The DNS lookup successfully resolved the Pod's DNS name to its IP address, `10.244.1.2`.

The DNS name follows this structure:

```text
<pod-name>.<headless-service>.<namespace>.svc.cluster.local
```

For this lab:

- Pod: `ivolvedb-0`
- Headless Service: `ivolvedbsvc`
- Namespace: `ivolve`

The Headless Service allows StatefulSet Pods to be discovered through stable DNS names.

## Final Verification

The following checks were completed successfully:

- MySQL StatefulSet created with one replica.
- MySQL Pod reached `Running` and `1/1 Ready`.
- Pod scheduled on `minikube-m02`.
- ConfigMap and Secret values consumed by the container.
- PersistentVolumeClaim reached `Bound` with `1Gi` storage.
- MySQL client connection succeeded.
- MySQL version returned `8.0.46`.
- Headless Service created with `clusterIP: None`.
- Pod DNS resolution returned `10.244.1.2`.

**Conclusion:** The MySQL StatefulSet, Headless Service, configuration resources, persistent storage, database connectivity, and Kubernetes DNS resolution were verified successfully.

