# Lab 12: Managing Configuration and Sensitive Data with ConfigMaps and Secrets

## Objective
Create a ConfigMap for non-sensitive MySQL configuration and a Secret for sensitive MySQL credentials in the `ivolve` namespace.

## 1. Create the Lab Directory

```bash
mkdir -p ~/Training-labs/Kubernetes/Lab12
cd ~/Training-labs/Kubernetes/Lab12
```

## 2. Create the ConfigMap

File: `configmap.yaml`

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ivolvecm
  namespace: ivolve
data:
  DB_HOST: "mysql"
  DB_USER: "ivolveuser"
```

Apply the ConfigMap:

```bash
kubectl apply -f configmap.yaml
```

Expected output:

```text
configmap/ivolvecm created
```

The ConfigMap stores non-sensitive configuration values. Kubernetes resource names must use lowercase characters, so `ivolvecm` is used instead of `ivolveCM`.

## 3. Encode Secret Values Using Base64

Generate Base64 values:

```bash
echo -n 'RootPass123!' | base64
echo -n 'IvolvApp123!' | base64
```

Base64 is an encoding format, not encryption. It can be decoded, so it must not be treated as a way to protect passwords.

## 4. Create the Secret

File: `secret.yaml`

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: ivolvesecret
  namespace: ivolve
type: Opaque
data:
  MYSQL_ROOT_PASSWORD: Um9vdFBhc3MxMjMh
  DB_PASSWORD: SXZvbHZBcHAxMjMh
```

Apply the Secret:

```bash
kubectl apply -f secret.yaml
```

Expected output:

```text
secret/ivolvesecret created
```

The Secret stores sensitive database credentials. Its resource name also uses lowercase characters to comply with Kubernetes naming rules.

## 5. Verify the Resources

```bash
kubectl get configmap,secret -n ivolve
```

Expected resources include:

```text
configmap/ivolvecm
secret/ivolvesecret
```

The ConfigMap should contain two data entries, and the Secret should contain two data entries.

## Important Notes

- The existing cluster does not currently have a MySQL StatefulSet or a MySQL Headless Service.
- `DB_HOST: mysql` is a placeholder and must match the actual Headless Service name when one is created.
- `DB_USER: ivolveuser` must correspond to a database user that exists and has permission to access the `ivolve` database.
- Creating a Kubernetes Secret does not automatically create a MySQL user or configure the database.
- Base64 encoding is not encryption. Avoid committing real passwords or sensitive credentials to GitHub.

