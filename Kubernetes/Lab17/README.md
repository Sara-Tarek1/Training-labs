# Lab 17: Control Pod-to-Pod Traffic via Network Policy

## Objective

Create a Kubernetes NetworkPolicy to control incoming traffic to the MySQL Pod, allowing connections only from the application Pods on TCP port 3306.

## Environment

- Kubernetes: v1.37.0
- Cluster: Minikube
- Namespace: `ivolve`

## 1. Identify Application and MySQL Pods

Command:

```bash
kubectl get pods -n ivolve --show-labels
```

Relevant output:

```text
NAME                         READY   STATUS    LABELS
ivolveapp-68594f8567-76rq6   1/1     Running   app=ivolveapp
ivolveapp-68594f8567-v96rq   1/1     Running   app=ivolveapp
ivolvedb-0                   1/1     Running   app=ivolvedb
```

The labels used in the NetworkPolicy are:

- MySQL: `app=ivolvedb`
- Application: `app=ivolveapp`

## 2. Create the NetworkPolicy

Created `networkpolicy.yaml` with the following configuration:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: ivolvenp
  namespace: ivolve
spec:
  podSelector:
    matchLabels:
      app: ivolvedb
  policyTypes:
    - Ingress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app: ivolveapp
      ports:
        - protocol: TCP
          port: 3306
```

### Configuration Explanation

- `podSelector`: Targets the MySQL Pod.
- `policyTypes: Ingress`: Controls incoming traffic only.
- `from`: Allows traffic from application Pods in the same namespace.
- `ports`: Allows TCP traffic on port 3306.

## 3. Apply the NetworkPolicy

Command:

```bash
kubectl apply -f networkpolicy.yaml
```

Output:

```text
networkpolicy.networking.k8s.io/ivolvenp created
```

## 4. Verify the NetworkPolicy

Command:

```bash
kubectl get networkpolicy -n ivolve
```

Output:

```text
NAME       POD-SELECTOR   AGE
ivolvenp   app=ivolvedb   43s
```

Command:

```bash
kubectl describe networkpolicy ivolvenp -n ivolve
```

Relevant output:

```text
Name:         ivolvenp
Namespace:    ivolve

Spec:
  PodSelector:     app=ivolvedb
  Allowing ingress traffic:
    To Port: 3306/TCP
    From:
      PodSelector: app=ivolveapp
  Not affecting egress traffic
  Policy Types: Ingress
```

## 5. Check Network Components

Command:

```bash
kubectl get pods -n kube-system
```

The cluster system Pods were Running. The network Pods included:

```text
kindnet-8p8lj     1/1   Running
kindnet-f862g    1/1   Running
```

**Note:** The NetworkPolicy resource was successfully created and its configuration was verified. Actual traffic enforcement was not independently tested. NetworkPolicy enforcement depends on support from the cluster's network plugin.

## Final Result

- Created a NetworkPolicy named `ivolvenp`.
- Selected the MySQL Pod using `app=ivolvedb`.
- Configured Ingress-only traffic rules.
- Allowed traffic from application Pods using `app=ivolveapp`.
- Restricted the allowed traffic to TCP port 3306.
- Verified the applied NetworkPolicy using `kubectl describe`.

**Lab 17 configuration completed successfully.**

