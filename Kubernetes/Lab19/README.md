# Lab 19: Securing Kubernetes with RBAC and Service Accounts

## Objective

Configure Kubernetes RBAC using a ServiceAccount, Role, and RoleBinding to control access to Deployments within the `ivolve` namespace.

### Lab Requirements

- Create a `jenkinsSA` ServiceAccount in the `ivolve` namespace.
- Generate a token for the ServiceAccount.
- Create a Role named `ivolveDeployRole` that grants `create` and `update` permissions on Deployments in the `ivolve` namespace.
- Create a RoleBinding named `ivolveDeployRoleBinding` to bind the Role to the ServiceAccount.
- Validate that the ServiceAccount can only create and update Deployments in the `ivolve` namespace.

> **Naming note:** Kubernetes resource names must use lowercase letters. Therefore, `jenkinsSA`, `ivolveDeployRole`, and `ivolveDeployRoleBinding` were created using lowercase names: `jenkinssa`, `ivolve-deploy-role`, and `ivolve-deploy-role-binding`.

## 1. Verify the Namespace

```bash
kubectl get namespace ivolve
```

**Output:**

```text
NAME     STATUS   AGE
ivolve   Active   7d3h
```

## 2. Create the ServiceAccount

Create the ServiceAccount in the `ivolve` namespace:

```bash
kubectl create serviceaccount jenkinssa -n ivolve
```

Verify it:

```bash
kubectl get serviceaccount -n ivolve
```

The ServiceAccount was created successfully and later verified:

```text
NAME        AGE
default     7d4h
jenkinssa   41m
```

The declarative manifest is `serviceaccount.yaml`.

## 3. Generate a ServiceAccount Token

```bash
kubectl create token jenkinssa -n ivolve
```

A token was generated successfully. The token value is intentionally not included in this README because it is a credential and must be kept private.

## 4. Create the Role

Create a Role that grants only `create` and `update` permissions on Deployments in the `ivolve` namespace.

File: `role.yaml`

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: ivolve-deploy-role
  namespace: ivolve
rules:
  - apiGroups: ["apps"]
    resources: ["deployments"]
    verbs: ["create", "update"]
```

Apply the Role:

```bash
kubectl apply -f role.yaml
```

Verify its permissions:

```bash
kubectl describe role ivolve-deploy-role -n ivolve
```

**Verified output:**

```text
Name:         ivolve-deploy-role
Labels:       <none>
Annotations:  <none>
PolicyRule:
  Resources         Non-Resource URLs  Resource Names  Verbs
  ---------         -----------------  --------------  -----
  deployments.apps  []                 []              [create update]
```

## 5. Create the RoleBinding

The RoleBinding connects the ServiceAccount to the Role.

File: `rolebinding.yaml`

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: ivolve-deploy-role-binding
  namespace: ivolve
subjects:
  - kind: ServiceAccount
    name: jenkinssa
    namespace: ivolve
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: ivolve-deploy-role
```

Apply the RoleBinding:

```bash
kubectl apply -f rolebinding.yaml
```

**Output:**

```text
rolebinding.rbac.authorization.k8s.io/ivolve-deploy-role-binding created
```

Verify the RoleBinding:

```bash
kubectl get rolebinding -n ivolve
```

**Output:**

```text
NAME                         ROLE                      AGE
ivolve-deploy-role-binding   Role/ivolve-deploy-role   73s
```

Inspect the binding:

```bash
kubectl describe rolebinding ivolve-deploy-role-binding -n ivolve
```

**Verified details:**

```text
Role:
  Kind:  Role
  Name:  ivolve-deploy-role
Subjects:
  Kind            Name       Namespace
  ----            ----       ---------
  ServiceAccount  jenkinssa  ivolve
```

## 6. Validate RBAC Permissions

Use `kubectl auth can-i` to check what the ServiceAccount is allowed to do.

### Create Deployments in ivolve

```bash
kubectl auth can-i create deployments \
  --as=system:serviceaccount:ivolve:jenkinssa -n ivolve
```

**Output:**

```text
yes
```

### Update Deployments in ivolve

```bash
kubectl auth can-i update deployments \
  --as=system:serviceaccount:ivolve:jenkinssa -n ivolve
```

**Output:**

```text
yes
```

### Delete Deployments in ivolve

```bash
kubectl auth can-i delete deployments \
  --as=system:serviceaccount:ivolve:jenkinssa -n ivolve
```

**Output:**

```text
no
```

### Create Pods in ivolve

```bash
kubectl auth can-i create pods \
  --as=system:serviceaccount:ivolve:jenkinssa -n ivolve
```

**Output:**

```text
no
```

### Create Deployments in another namespace

```bash
kubectl auth can-i create deployments \
  --as=system:serviceaccount:ivolve:jenkinssa -n default
```

**Output:**

```text
no
```

## 7. Final Results

| Permission | Result |
|---|---|
| Create Deployments in `ivolve` | Allowed |
| Update Deployments in `ivolve` | Allowed |
| Delete Deployments in `ivolve` | Denied |
| Create Pods in `ivolve` | Denied |
| Create Deployments in `default` | Denied |

The RBAC configuration was successfully validated. The ServiceAccount has only the requested Deployment create and update permissions within the `ivolve` namespace.

## Files

```text
Lab19/
├── README.md
├── serviceaccount.yaml
├── role.yaml
└── rolebinding.yaml
```

## Important Security Note

The ServiceAccount token is a sensitive credential. It was generated successfully but is not stored in this repository or included in the README.

