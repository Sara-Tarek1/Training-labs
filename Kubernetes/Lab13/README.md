# Lab 13: Persistent Storage Setup for Application Logging

## Objective
Configure persistent storage in Kubernetes using a PersistentVolume (PV), PersistentVolumeClaim (PVC), and a Pod to store application logs.

## 1. Create PersistentVolume

Created `pv.yaml` with the following configuration:
- Name: `ivolvepv`
- Capacity: `1Gi`
- Access Mode: `ReadWriteMany`
- Reclaim Policy: `Retain`
- Storage Type: `hostPath`
- Node Path: `/mnt/app-logs`

Command:

```bash
kubectl apply -f pv.yaml
```

Output:

```text
persistentvolume/ivolvepv created
```

## 2. Verify PersistentVolume

Command:

```bash
kubectl get pv
```

Output:

```text
NAME       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS      CLAIM   STORAGECLASS   VOLUMEATTRIBUTESCLASS   REASON   AGE
ivolvepv   1Gi        RWX            Retain           Available                          <unset>                          60s
```

## 3. Create PersistentVolumeClaim

Created `pvc.yaml` to request `1Gi` of storage with `ReadWriteMany` access mode.

Command:

```bash
kubectl apply -f pvc.yaml
```

The PVC was created successfully.

## 4. Verify PV and PVC Binding

Command:

```bash
kubectl get pv,pvc
```

Output:

```text
NAME                        CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS   CLAIM               STORAGECLASS   VOLUMEATTRIBUTESCLASS   REASON   AGE
persistentvolume/ivolvepv   1Gi        RWX            Retain           Bound    default/ivolvepvc                  <unset>                          11m

NAME                              STATUS   VOLUME     CAPACITY   ACCESS MODES   STORAGECLASS   VOLUMEATTRIBUTESCLASS   AGE
persistentvolumeclaim/ivolvepvc   Bound    ivolvepv   1Gi        RWX                           <unset>                 5m1s
```

Both the PV and PVC reached the `Bound` status, meaning the claim was successfully connected to the volume.

## 5. Create a Logging Pod

Created `pod.yaml` to deploy a BusyBox container that writes a log entry to `/mnt/app-logs/app.log` using the PVC `ivolvepvc`.

Command:

```bash
kubectl apply -f pod.yaml
```

## 6. Verify Pod Status

Command:

```bash
kubectl get pods
```

Output for the logging Pod:

```text
logging-pod                         1/1     Running   0                 25s
```

## 7. Verify Application Log

Command:

```bash
kubectl exec logging-pod -- cat /mnt/app-logs/app.log
```

Output:

```text
Lab13 log entry
```

The log entry was successfully written and read from inside the Pod.

## 8. Inspect PersistentVolume Details

Command:

```bash
kubectl describe pv ivolvepv
```

Relevant output:

```text
Name:            ivolvepv
Status:          Bound
Claim:           default/ivolvepvc
Reclaim Policy:  Retain
Access Modes:    RWX
VolumeMode:      Filesystem
Capacity:        1Gi

Source:
    Type:          HostPath (bare host directory volume)
    Path:          /mnt/app-logs
    HostPathType:  DirectoryOrCreate
```

## 9. Verify Log File on the Minikube Node

Command:

```bash
minikube ssh -- ls -l /mnt/app-logs
```

Output:

```text
total 4
-rw-r--r-- 1 root root 16 Oct 28 18:32 app.log
```

The `app.log` file was found on the Minikube node at `/mnt/app-logs`.

## Result

- Created a PersistentVolume with `1Gi` capacity and `Retain` reclaim policy.
- Created a PersistentVolumeClaim and verified that it was bound to the PV.
- Mounted the PVC into a logging Pod.
- Wrote and read a log entry successfully.
- Verified that `app.log` exists on the Minikube node.

## Note

The volume uses `hostPath`, which stores data on a specific node. Setting `ReadWriteMany` in the PV does not make `hostPath` shared storage across multiple nodes. A shared storage backend is required for actual multi-node read/write access.

