# Lab 11: Namespace Management and Resource Quota Enforcement

## Objective
Create a Kubernetes Namespace and configure a ResourceQuota to limit the total number of Pods to 3. Verify that Kubernetes prevents creating additional Pods after reaching the limit.

## Step 1: Create a Namespace

Command:
```bash
kubectl create namespace ivolve
```

Explanation:
A Namespace is a logical way to organize and separate resources inside the same Kubernetes Cluster.

Output:
```text
namespace/ivolve created
```

## Step 2: Create the ResourceQuota YAML File

Command:
```bash
nano resourcequota.yaml
```

Configuration:
```yaml
apiVersion: v1
kind: ResourceQuota
metadata:
  name: ivolvequota
  namespace: ivolve
spec:
  hard:
    pods: "3"
```

Explanation:
- `apiVersion`: Specifies the Kubernetes API version.
- `kind`: Defines the resource type as ResourceQuota.
- `metadata.name`: Sets the quota name.
- `metadata.namespace`: Specifies the namespace where the quota applies.
- `spec.hard.pods`: Sets the maximum number of Pods to 3.

Note: Kubernetes requires this resource name to be lowercase, so `ivolvequota` is used instead of `ivolveQuota`.

## Step 3: Apply the ResourceQuota

Command:
```bash
kubectl apply -f resourcequota.yaml
```

Explanation:
The `-f` option tells kubectl to read the configuration from a file and apply it to the cluster.

## Step 4: Verify the ResourceQuota

Command:
```bash
kubectl describe resourcequota ivolvequota -n ivolve
```

Output:
```text
Name:       ivolvequota
Namespace:  ivolve
Resource    Used  Hard
--------    ----  ----
pods        0     3
```

Explanation:
- `Used: 0` means no Pods were counted in the namespace at that time.
- `Hard: 3` means the maximum allowed number of Pods is 3.

## Step 5: Check Existing Pods

Command:
```bash
kubectl get pods -n ivolve
```

Output:
```text
No resources found in ivolve namespace.
```

Explanation:
The namespace was empty before testing the quota.

## Step 6: Create Three Pods

Commands:
```bash
kubectl run quota-pod-1 --image=nginx --restart=Never -n ivolve
kubectl run quota-pod-2 --image=nginx --restart=Never -n ivolve
kubectl run quota-pod-3 --image=nginx --restart=Never -n ivolve
```

Explanation:
- `kubectl run`: Requests creation of a Pod.
- `--image=nginx`: Uses the Nginx container image.
- `--restart=Never`: Creates a standalone Pod instead of a Deployment.
- `-n ivolve`: Creates the Pod in the `ivolve` namespace.

Each Pod creation succeeded, reaching the quota limit of 3 Pods.

## Step 7: Test the Quota Limit

Command:
```bash
kubectl run quota-pod-4 --image=nginx --restart=Never -n ivolve
```

Output:
```text
Error from server (Forbidden): pods "quota-pod-4" is forbidden: exceeded quota: ivolvequota, requested: pods=1, used: pods=3, limited: pods=3
```

Explanation:
Kubernetes rejected the fourth Pod because the namespace had already reached its maximum limit of 3 Pods.

## Conclusion

The ResourceQuota was successfully applied to the `ivolve` namespace. Kubernetes allowed three Pods and rejected the fourth, confirming that the quota enforcement works correctly.

