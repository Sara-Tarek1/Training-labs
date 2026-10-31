agement with CPU and Memory Requests and Limits

## Objective
Configure CPU and memory requests and limits for the `ivolveApp` Deployment, then verify the configuration and monitor actual resource usage.

## Environment
- Kubernetes running on Minikube
- Namespace: `ivolve`
- Deployment: `ivolveapp`
- Replicas: 2

## 1. Configure Resource Requests and Limits

Updated the application container in `deployment.yaml` with the following resources:

```yaml
resources:
  requests:
    cpu: "1"
    memory: "1Gi"
  limits:
    cpu: "2"
    memory: "2Gi"
```

- **Requests:** Resources Kubernetes reserves for the container when scheduling the Pod.
- **Limits:** Maximum CPU and memory the container is allowed to use.

## 2. Configure Rolling Update Strategy

The namespace has a ResourceQuota limiting the total number of Pods to 3. To avoid exceeding this quota during the Deployment update, the rolling update strategy was configured as follows:

```yaml
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxSurge: 0
    maxUnavailable: 1
```

This allows Kubernetes to replace Pods one at a time without creating an extra Pod during the update.

## 3. Apply the Deployment

```bash
kubectl apply -f deployment.yaml
```

Output:

```text
deployment.apps/ivolveapp configured
```

## 4. Verify Running Pods

```bash
kubectl get pods -n ivolve
```

The updated application Pods reached the Running state:

```text
ivolveapp-68594f8567-76rq6   1/1   Running
ivolveapp-68594f8567-v96rq   1/1   Running
ivolvedb-0                   1/1   Running
```

## 5. Verify Resource Configuration

```bash
kubectl describe pod ivolveapp-68594f8567-76rq6 -n ivolve
```

The output confirmed:

```text
Limits:
  cpu:     2
  memory:  2Gi
Requests:
  cpu:     1
  memory:  1Gi

QoS Class: Burstable
```

The Pod has the expected CPU and memory requests and limits.

## 6. Enable Metrics Server

Metrics Server is required to monitor live resource usage.

```bash
minikube addons enable metrics-server
```

Output:

```text
The 'metrics-server' addon is enabled
```

Verify that Metrics Server is running:

```bash
kubectl get pods -n kube-system | grep metrics
```

Output:

```text
metrics-server-768f9f6999-pksgh   1/1   Running
```

Verify the Metrics API:

```bash
kubectl get apiservices v1beta1.metrics.k8s.io
```

Output:

```text
v1beta1.metrics.k8s.io   kube-system/metrics-server   True
```

## 7. Monitor Actual Resource Usage

```bash
kubectl top pod -n ivolve
```

Output:

```text
NAME                         CPU(cores)   MEMORY(bytes)
ivolveapp-68594f8567-76rq6   1m           20Mi
ivolveapp-68594f8567-v96rq   1m           20Mi
ivolvedb-0                   14m          361Mi
```

- `1m` CPU means 0.001 CPU core.
- `20Mi` means approximately 20 MiB of memory.
- `kubectl top` displays current usage, while requests and limits define scheduling requirements and maximum allowed usage.

## Final Result

- Configured CPU and memory requests and limits.
- Successfully updated the `ivolveapp` Deployment.
- Verified resource settings using `kubectl describe pod`.
- Enabled Metrics Server.
- Successfully monitored live Pod resource usage using `kubectl top pod`.

**Lab 16 completed successfully.**


