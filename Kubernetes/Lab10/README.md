# Lab 10: Node Isolation Using Taints and Labels in Kubernetes

## Objective

The objective of this lab is to isolate a Kubernetes worker node using a taint and identify it using a label.

Tasks:
- Apply a taint with key `workload`, value `ivolveApp`, and effect `NoSchedule`.
- Label the same worker node with `project=ivolve`.
- Verify the taint and label using Kubernetes commands.

## Environment

- Kubernetes platform: Minikube
- Control Plane Node: `minikube`
- Worker Node: `minikube-m02`
- Kubernetes version: `v1.37.0`
- Worker Node status: `Ready`

## Step 1: Check the Cluster Nodes

**Command:**

```bash
kubectl get nodes -o wide
```

**Output:**

```text
NAME           STATUS   ROLES           AGE   VERSION   INTERNAL-IP
minikube       Ready    control-plane   36d   v1.37.0   192.168.49.2
minikube-m02   Ready    <none>          3m12s v1.37.0   192.168.49.3
```

**Explanation:**

This command lists the nodes in the Kubernetes cluster. The `minikube-m02` worker node was added to the cluster and reached the `Ready` state.

## Step 2: Apply a Taint to the Worker Node

**Command:**

```bash
kubectl taint nodes minikube-m02 workload=ivolveApp:NoSchedule
```

**Output:**

```text
error: node minikube-m02 already has workload taint(s) with same effect(s) and --overwrite is false
```

**Explanation:**

The command reported that the taint already existed. We verified the existing configuration instead of adding a duplicate taint.

The `NoSchedule` effect prevents new pods from being scheduled on this node unless they have a matching toleration.

## Step 3: Verify the Taint

**Command:**

```bash
kubectl describe node minikube-m02
```

**Verified output:**

```text
Taints: workload=ivolveApp:NoSchedule
```

The node description also confirmed:

```text
Ready True
Reason: KubeletReady
```

**Explanation:**

The output confirms that the required taint is present and that the worker node is ready.

## Step 4: Apply a Label to the Worker Node

**Command:**

```bash
kubectl label nodes minikube-m02 project=ivolve
```

**Output:**

```text
node/minikube-m02 labeled
```

**Explanation:**

This command adds the label `project=ivolve` to the worker node. Labels help identify nodes and can be used with node selectors to target workloads to specific nodes.

## Step 5: Verify the Node Label

**Command:**

```bash
kubectl get nodes --show-labels
```

**Verified output:**

```text
NAME           STATUS   ROLES           VERSION   LABELS
minikube       Ready    control-plane   v1.37.0   ...
minikube-m02   Ready    <none>          v1.37.0   ...,project=ivolve
```

The output confirms that the worker node has the required `project=ivolve` label.

## Step 6: Final Verification

The following configurations were verified on `minikube-m02`:

**Taint:**

```text
workload=ivolveApp:NoSchedule
```

**Label:**

```text
project=ivolve
```

**Node status:**

```text
Ready
```

## Conclusion

The lab requirements were completed successfully. The Minikube worker node `minikube-m02` has the required taint and label. The taint controls pod scheduling, while the label identifies the node for targeted scheduling.

