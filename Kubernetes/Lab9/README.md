# Kubernetes Lab 9 — Two-Node Cluster on AWS EC2

## 1. Overview

This lab demonstrates setting up a Kubernetes cluster using two AWS EC2 Ubuntu instances.

The cluster consists of one **Master** node and one **Slave** node. The Master runs the Kubernetes Control Plane, while the Slave joins the cluster as a Worker node.

The cluster uses:
- Kubernetes and `kubeadm`
- `containerd` as the container runtime
- Flannel for Pod networking
- CoreDNS for cluster DNS

**Note:** This README documents the configuration and results visible in the lab screenshots. Commands or network rules not confirmed by the screenshots are identified separately.

## 2. Lab Architecture

| Component | Details |
|---|---|
| Cloud provider | AWS EC2 |
| Operating system | Ubuntu 26.04.1 LTS, as displayed in the screenshots |
| Master hostname | `ip-172-31-13-55` |
| Master private IP | `172.31.13.55` |
| Slave hostname | `ip-172-31-7-240` |
| Slave private IP | `172.31.7.240` |
| Kubernetes version | `v1.35.9` |
| Container runtime | `containerd://2.2.2` |
| Flannel version | `v0.28.10` |

### Node Roles

- **Master (Control Plane):** manages the Kubernetes cluster and runs the control-plane components.
- **Slave (Worker):** joins the cluster and runs workloads and node-level components.

## 3. Initialize the Kubernetes Cluster on Master

The Master was initialized successfully using `kubeadm init`.

After initialization, configure `kubectl` for the regular user on Master:

```bash
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

The initialization output instructs the user to deploy a Pod network before using the cluster fully.

## 4. Configure Pod Networking with Flannel

Flannel was deployed as the cluster's Pod network plugin.

Check the Flannel DaemonSet and its Pods from Master:

```bash
kubectl -n kube-flannel get ds,pods -o wide
```

The screenshots show:
- Flannel version `v0.28.10`
- The Flannel DaemonSet with one desired and one available Pod in the displayed output
- A Flannel Pod in `Running` state

The screenshots also show Pod IP addresses in the `10.244.0.0/16` range, including `10.244.0.2`.

## 5. Join Slave to the Cluster

On Master, generate the join command:

```bash
kubeadm token create --print-join-command
```

The command output includes the API server endpoint, token, and certificate discovery hash. Use the generated command on Slave with appropriate administrative privileges.

The screenshots show the join command using the Master endpoint:

```text
172.31.13.55:6443
```

The join operation completed successfully on Slave. The terminal reported that the node had joined the cluster.

**Security note:** The token and certificate hash should be treated carefully. Do not publish active join tokens or credentials in a public repository.

## 6. Verify the Cluster

Run the following commands on Master.

### Check the nodes

```bash
kubectl get nodes -o wide
```

The screenshots show both nodes in `Ready` state:

- `ip-172-31-13-55` — `control-plane`
- `ip-172-31-7-240` — Worker node

Both nodes were displayed with Kubernetes version `v1.35.9`.

### Check the cluster Pods

```bash
kubectl get pods -A
```

The screenshots show the following components running:

- `kube-apiserver`
- `kube-controller-manager`
- `kube-scheduler`
- `etcd`
- `kube-proxy`
- Flannel
- CoreDNS

The screenshots show two CoreDNS Pods in `Running` state.

### Check the Flannel DaemonSet

```bash
kubectl -n kube-flannel get ds,pods -o wide
```

### Check the DNS Service

```bash
kubectl -n kube-system get svc kube-dns -o wide
```

The screenshot shows the `kube-dns` Service with:

- Cluster IP: `10.96.0.10`
- DNS ports: `53/UDP` and `53/TCP`
- Metrics port: `9153/TCP`

## 7. Ports and Networking

### Ports confirmed in the screenshots

| Port | Protocol | Purpose | Evidence |
|---|---|---|---|
| `6443` | TCP | Kubernetes API server endpoint used by the Slave join command | Visible in the `kubeadm join` command |
| `53` | UDP/TCP | Cluster DNS through the `kube-dns` Service | Visible in the Service output |
| `9153` | TCP | CoreDNS metrics endpoint | Visible in the Service output |

These screenshots confirm the displayed endpoints and Service configuration. They do not independently prove which ports were opened in the AWS Security Groups.

### Other common Kubernetes ports

The following ports may be needed depending on the cluster configuration. Their use or accessibility was **not verified by the provided screenshots**.

| Port | Protocol | Typical purpose |
|---|---|---|
| `10250` | TCP | Kubelet API |
| `2379–2380` | TCP | etcd client and peer communication; peer traffic is relevant to multi-member etcd |
| `10257` | TCP | Controller Manager secure endpoint, usually local |
| `10259` | TCP | Scheduler secure endpoint, usually local |
| `8472` | UDP | Flannel VXLAN, if VXLAN is the configured backend |
| `30000–32767` | TCP/UDP | Default NodePort range, when NodePort Services are used |

**Security:** Allow only the required traffic between the cluster nodes. Do not expose internal Kubernetes ports to the public internet unnecessarily. Verify the actual AWS Security Group rules separately.

## 8. Troubleshooting Commands

Run these commands on Master when checking cluster status:

```bash
kubectl get nodes -o wide
kubectl get pods -A -o wide
kubectl get svc -A
kubectl -n kube-flannel get ds,pods -o wide
kubectl describe nodes
```

On the affected instance, inspect the node services:

```bash
sudo systemctl status kubelet --no-pager
sudo systemctl status containerd --no-pager
sudo journalctl -u kubelet -n 60 --no-pager
```

## 9. Final Result

The provided screenshots demonstrate that:

1. The Kubernetes Control Plane was initialized successfully on Master.
2. Flannel was deployed and its Pod was running in the displayed output.
3. Slave successfully joined the cluster.
4. Both nodes reached the `Ready` state.
5. The displayed CoreDNS Pods were running.
6. The `kube-dns` Service was configured with ClusterIP `10.96.0.10` and ports `53/UDP`, `53/TCP`, and `9153/TCP`.

These results confirm that the two-node cluster was operational in the displayed checks.

## 10. Official References

- Kubernetes kubeadm installation: https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/install-kubeadm/
- Creating a cluster with kubeadm: https://kubernetes.io/docs/setup/production-environment/tools/kubeadm/create-cluster-kubeadm/
- Kubernetes ports and protocols: https://kubernetes.io/docs/reference/networking/ports-and-protocols/
- Flannel: https://github.com/flannel-io/flannel

