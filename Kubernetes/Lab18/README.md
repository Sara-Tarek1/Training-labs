# Lab 18: Node-Wide Pod Management with DaemonSet

## Objective

Deploy a monitoring stack in Kubernetes using Prometheus, Alertmanager, Grafana, and Prometheus Node Exporter.

### Lab Requirements

- Create a `monitoring` namespace.
- Deploy Prometheus, Alertmanager, and Grafana.
- Deploy Node Exporter using a DaemonSet.
- Verify that a Node Exporter Pod runs on each node.
- Access Node Exporter metrics through `<node_IP>:9100/metrics`.
- Configure Prometheus to scrape Node Exporter metrics.
- Configure a Grafana dashboard for node monitoring.

## 1. Create the Monitoring Namespace

```bash
kubectl create namespace monitoring
kubectl get namespace monitoring
```

**Output:**

```text
NAME         STATUS   AGE
monitoring   Active   3h1m
```

## 2. Apply Kubernetes Manifests

```bash
kubectl apply -f prometheus-config.yaml
kubectl apply -f prometheus-deployment.yaml
kubectl apply -f prometheus-service.yaml

kubectl apply -f alertmanager-deployment.yaml
kubectl apply -f alertmanager-service.yaml

kubectl apply -f grafana-deployment.yaml
kubectl apply -f grafana-service.yaml

kubectl apply -f node-exporter-daemonset.yaml
kubectl apply -f node-exporter-service.yaml
```

## 3. Verify Kubernetes Nodes

```bash
kubectl get nodes
```

**Output:**

```text
NAME           STATUS   ROLES           AGE    VERSION
minikube       Ready    control-plane   43d    v1.37.0
minikube-m02   Ready    <none>          7d3h   v1.37.0
```

## 4. Verify Monitoring Pods

```bash
kubectl get pods -n monitoring -o wide
```

**Output:**

```text
NAME                            READY   STATUS    RESTARTS   AGE   IP            NODE           NOMINATED NODE   READINESS GATES
alertmanager-7f5b7564d9-dz8ls   1/1     Running   0          71m   10.244.0.9    minikube       <none>           <none>
grafana-6cbcf4fdb8-7nmmk        1/1     Running   0          67m   10.244.0.10   minikube       <none>           <none>
node-exporter-lj5wp             1/1     Running   0          83m   10.244.0.6    minikube       <none>           <none>
node-exporter-r9cmw             1/1     Running   0          83m   10.244.1.5    minikube-m02   <none>           <none>
prometheus-b94545cb8-rct47      1/1     Running   0          75m   10.244.0.8    minikube       <none>           <none>
```

All monitoring Pods were running and ready. Two Node Exporter Pods were scheduled, one on each node.

## 5. Verify the Node Exporter DaemonSet

```bash
kubectl get daemonset -n monitoring
```

**Output:**

```text
NAME            DESIRED   CURRENT   READY   UP-TO-DATE   AVAILABLE   NODE SELECTOR   AGE
node-exporter   2         2         2       2            2           <none>          83m
```

The DaemonSet successfully scheduled one Node Exporter Pod on each of the two Kubernetes nodes.

## 6. Verify Services and Endpoints

```bash
kubectl get services -n monitoring
```

**Output:**

```text
NAME            TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)    AGE
alertmanager    ClusterIP   10.104.241.155   <none>        9093/TCP   72m
grafana         ClusterIP   10.104.77.105    <none>        3000/TCP   68m
node-exporter   ClusterIP   None             <none>        9100/TCP   79m
prometheus      ClusterIP   10.99.86.214     <none>        9090/TCP   75m
```

The Node Exporter Service is a **Headless Service**, indicated by `CLUSTER-IP: None`. It allows Prometheus to discover the individual Node Exporter endpoints through DNS.

```bash
kubectl get endpointslice -n monitoring
```

**Output:**

```text
NAME                  ADDRESSTYPE   PORTS   ENDPOINTS               AGE
alertmanager-hzt5b    IPv4          9093    10.244.0.9              72m
grafana-v475n         IPv4          3000    10.244.0.10             68m
node-exporter-hp56h   IPv4          9100    10.244.0.6,10.244.1.5   79m
prometheus-lrqmm      IPv4          9090    10.244.0.8              76m
```

The Node Exporter EndpointSlice contains both Node Exporter Pod IP addresses.

## 7. Test Node Exporter Metrics

Access the metrics endpoint on both Kubernetes nodes:

```bash
curl http://192.168.49.2:9100/metrics
curl http://192.168.49.3:9100/metrics
```

Both requests successfully returned Prometheus-format metrics, including `node_*` metrics and HTTP request metrics such as:

```text
promhttp_metric_handler_requests_total{code="200"} 1
```

This confirmed that Node Exporter was exposing metrics on both nodes.

## 8. Configure Prometheus

Prometheus was configured through the `prometheus-config` ConfigMap to discover Node Exporter using the headless Service DNS name:

```text
node-exporter.monitoring.svc.cluster.local
```

Apply the updated configuration and restart Prometheus:

```bash
kubectl apply -f prometheus-config.yaml
kubectl rollout restart deployment/prometheus -n monitoring
kubectl rollout status deployment/prometheus -n monitoring
```

Access the Prometheus UI using port forwarding:

```bash
kubectl port-forward -n monitoring svc/prometheus 9090:9090
```

Open:

```text
http://localhost:9090/targets
```

**Verified Targets:**

```text
node-exporter
2 / 2 up

Endpoint http://10.244.0.6:9100/metrics   job="node-exporter" instance="10.244.0.6:9100" State up
Endpoint http://10.244.1.5:9100/metrics   job="node-exporter" instance="10.244.1.5:9100" State up

prometheus
1 / 1 up

Endpoint http://localhost:9090/metrics   job="prometheus" instance="localhost:9090" State up
```

Both Node Exporter targets were successfully scraped by Prometheus and showed the `UP` state.

## 9. Configure Grafana

Start port forwarding:

```bash
kubectl port-forward -n monitoring svc/grafana 3000:3000
```

Open Grafana:

```text
http://localhost:3000
```

Add Prometheus as a data source using:

```text
http://prometheus.monitoring.svc.cluster.local:9090
```

The connection test returned:

```text
Successfully queried the Prometheus API.
```

A dashboard named **Kubernetes Node Monitoring** was created and saved with CPU and Memory panels.

### CPU Usage Query

```promql
100 * (1 - avg by (instance) (
  rate(node_cpu_seconds_total{job="node-exporter",mode="idle"}[5m])
))
```

### Memory Usage Query

```promql
100 * (
  1 -
  node_memory_MemAvailable_bytes{job="node-exporter"}
  /
  node_memory_MemTotal_bytes{job="node-exporter"}
)
```

## 10. Final Verification

The following checks were successful:

- The `monitoring` namespace was created successfully.
- Prometheus, Alertmanager, and Grafana Pods were running.
- Node Exporter was deployed as a DaemonSet.
- One Node Exporter Pod was running on each Kubernetes node.
- Metrics were accessible through both node IPs on port `9100`.
- Prometheus successfully scraped both Node Exporter targets.
- Grafana successfully connected to Prometheus.
- The `Kubernetes Node Monitoring` dashboard was created and saved with CPU and Memory panels.

## Notes

- Alertmanager was deployed, and its Pod and Service were verified. However, it was not integrated with Prometheus using an Alertmanager configuration, and alert rules and notification receivers were not configured or tested.
- The Grafana dashboard uses Node Exporter host-level metrics for CPU and memory. It is not a full Kubernetes Pod or object monitoring dashboard.
- CPU and Memory panels were verified; a Disk panel is not included as a completed item.

