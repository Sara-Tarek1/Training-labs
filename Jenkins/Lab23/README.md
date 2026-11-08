# Lab 23: Implementing a GitOps Workflow Using Jenkins and Argo CD

## 1. Overview

This lab demonstrates how to implement a GitOps-based Continuous Delivery workflow by integrating Jenkins, GitHub, Kubernetes, and Argo CD.

The main objective is to automate Kubernetes application deployments by allowing Jenkins to update Kubernetes manifests in GitHub and allowing Argo CD to automatically synchronize those changes with the Kubernetes cluster.

Instead of manually deploying every new application version, Git becomes the single source of truth for the desired Kubernetes state.

### Lab Objectives

- Install and configure Argo CD on Kubernetes.
- Access the Argo CD Web UI.
- Connect a GitHub repository to Argo CD.
- Create and configure an Argo CD Application.
- Enable automated synchronization.
- Integrate Jenkins with a GitOps workflow.
- Modify a Jenkins Shared Library to update Kubernetes manifests.
- Build, scan, and push Docker images.
- Automatically deploy updated application versions using Argo CD.
- Verify the deployment and synchronization status.

---

## 2. Tools and Technologies

| Tool | Purpose |
|---|---|
| Jenkins | Continuous Integration and pipeline automation |
| Docker | Building application container images |
| Docker Hub | Storing and distributing Docker images |
| Trivy | Scanning container images for vulnerabilities |
| Kubernetes | Container orchestration |
| Minikube | Running a local Kubernetes cluster |
| Argo CD | GitOps-based Continuous Delivery |
| GitHub | Source code and Kubernetes manifest repository |
| YAML | Defining Kubernetes resources |
| Groovy | Implementing Jenkins Shared Library functions |

---

## 3. GitOps Architecture

### Workflow

1. A developer triggers the Jenkins pipeline.
2. Jenkins builds the application.
3. Jenkins executes application tests.
4. Jenkins builds a Docker image.
5. Trivy scans the Docker image.
6. Jenkins pushes the image to Docker Hub.
7. Jenkins updates the image tag in the Kubernetes Deployment manifest.
8. Jenkins commits and pushes the updated manifest to GitHub.
9. Argo CD monitors the GitHub repository and detects the new commit.
10. Argo CD automatically synchronizes the Kubernetes cluster.
11. Kubernetes deploys the updated application version.

### Architecture Diagram

```text
              Developer
                  |
                  v
              GitHub
                  |
                  v
                Jenkins
                  |
          +-------+-------+
          |       |       |
          v       v       v
        Build   Test    Trivy Scan
          |
          v
       Docker Image
          |
          v
       Docker Hub
          |
          v
   Update Kubernetes YAML
          |
          v
       GitHub Push
          |
          v
        Argo CD
          |
       Auto-Sync
          |
          v
       Kubernetes
          |
          v
    Updated Application
```

---

# Part 1: Argo CD Installation and Configuration

## 4. Verify the Kubernetes Cluster

Before installing Argo CD, verify that the Kubernetes cluster is running.

### Step 1: Check Kubernetes nodes

```bash
kubectl get nodes
```

This command displays the cluster nodes and their readiness status.

### Step 2: Check cluster information

```bash
kubectl cluster-info
```

Observed output:

```text
Kubernetes control plane is running at https://127.0.0.1:32771
CoreDNS is running at https://127.0.0.1:32771/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
```

The output confirms that the Kubernetes control plane and CoreDNS are available.

---

## 5. Install Argo CD

Argo CD is installed inside a dedicated Kubernetes namespace called `argocd`.

### Step 1: Create the namespace

```bash
kubectl create namespace argocd
```

**Explanation:**

- Creates a namespace named `argocd`.
- Keeps Argo CD resources separated from application workloads.

### Step 2: Install Argo CD components

```bash
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
```

**Explanation:**

- Downloads the official Argo CD installation manifests.
- Creates the required Kubernetes resources.
- Installs the Argo CD server, repository server, controllers, and supporting components.

### Step 3: Verify Argo CD pods

```bash
kubectl get pods -n argocd
```

Observed output:

```text
NAME                                                READY   STATUS    RESTARTS         AGE
argocd-application-controller-0                     1/1     Running   4 (3h21m ago)    19h
argocd-applicationset-controller-794bb95c9d-gkflg   1/1     Running   4 (3h21m ago)    19h
argocd-dex-server-66cf64bfcf-qhc8q                  1/1     Running   1 (3h21m ago)    3h59m
argocd-notifications-controller-5878c8d644-ktqp2    1/1     Running   4 (3h21m ago)    19h
argocd-redis-5869b7547-d2jxm                        1/1     Running   3 (3h21m ago)    19h
argocd-repo-server-7d55cc466b-bbcll                 1/1     Running   4 (3h21m ago)    19h
argocd-server-85b849c688-wrh4s                      1/1     Running   21 (3h21m ago)   19h
```

**Result:** All listed Argo CD components were Running and Ready.

### Step 4: Verify the ApplicationSet CRD

```bash
kubectl get crd applicationsets.argoproj.io
```

Observed output:

```text
NAME                          SCOPE        VERSIONS            CREATED AT
applicationsets.argoproj.io   Namespaced   v1alpha1(storage)   2026-11-07T22:15:42Z
```

The CRD exists in the cluster.

**Note:** During a later `kubectl apply` of the installation manifests, Kubernetes displayed an annotation-size warning for the existing ApplicationSet CRD. The existing Argo CD pods remained Running and Ready, so the installation was verified through the running components and CRD.

---

## 6. Access the Argo CD Web UI

### Step 1: Enable port forwarding

```bash
kubectl port-forward svc/argocd-server -n argocd 8082:443
```

**Explanation:**

- Forwards local port `8082` to the Argo CD HTTPS service port `443`.
- Allows access to the dashboard from the local machine.
- Keep this terminal open while using the dashboard.

### Step 2: Open the dashboard

Navigate to:

```text
https://localhost:8082
```

Accept the browser certificate warning if it appears.

### Step 3: Retrieve the initial admin password

Open another terminal and execute:

```bash
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
echo
```

**Explanation:**

- Retrieves the initial admin Secret.
- Reads the encoded password.
- Decodes it using `base64 -d`.

Login credentials:

```text
Username: admin
Password: <password returned by the command>
```

---

## 7. Connect GitHub Repository to Argo CD

Argo CD needs access to the GitHub repository containing the Kubernetes manifests.

### Repository Details

```text
Repository:
https://github.com/Sara-Tarek1/Training-labs.git

Branch:
main
```

### Configuration Steps

1. Open the Argo CD dashboard.
2. Navigate to Settings.
3. Select Repositories.
4. Click Connect Repo.
5. Choose HTTPS.
6. Enter the GitHub repository URL.
7. Complete the repository connection.

The repository should appear in Argo CD as successfully connected.

**Why is this important?**

Argo CD continuously monitors the configured repository to compare the desired Kubernetes configuration in Git with the actual cluster state.

---

## 8. Create the Argo CD Application

An Argo CD Application defines the connection between a Git repository and a Kubernetes destination.

It tells Argo CD which repository to monitor, which directory contains the manifests, and where to deploy the resources.

### Step 1: Create application.yaml

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application

metadata:
  name: ivovle
  namespace: argocd

spec:
  project: default

  source:
    repoURL: https://github.com/Sara-Tarek1/Training-labs.git
    targetRevision: main
    path: Jenkins/Lab22

  destination:
    server: https://kubernetes.default.svc
    namespace: ivolve

  syncPolicy:
    automated:
      prune: true
      selfHeal: true

    syncOptions:
      - CreateNamespace=false
```

### Step 2: Understand the YAML Configuration

| Field | Description |
|---|---|
| `apiVersion` | Argo CD Application API version |
| `kind` | Defines an Application resource |
| `metadata.name` | Application name: `ivovle` |
| `metadata.namespace` | Argo CD namespace |
| `project` | Argo CD project |
| `repoURL` | GitHub repository URL |
| `targetRevision` | Git branch monitored by Argo CD |
| `path` | Directory containing Kubernetes manifests |
| `destination.server` | Target Kubernetes cluster |
| `destination.namespace` | Application namespace: `ivolve` |
| `automated` | Enables automatic synchronization |
| `prune` | Deletes resources removed from Git |
| `selfHeal` | Corrects manual changes in the cluster |
| `CreateNamespace=false` | Prevents automatic creation of the destination namespace |

**Important:** The Application name is `ivovle`, while its destination namespace is `ivolve`. The source manifest directory is `Jenkins/Lab22`.

### Step 3: Apply the Application

```bash
kubectl apply -f application.yaml
```

### Step 4: Verify the Application

```bash
kubectl get application ivovle -n argocd
```

---

## 9. Enable Automated Synchronization

GitOps requires Argo CD to automatically apply changes committed to Git.

The Application configuration contains:

```yaml
syncPolicy:
  automated:
    prune: true
    selfHeal: true
```

### What does each option do?

**Automated Sync**

Automatically applies changes detected in Git.

**Prune**

Removes Kubernetes resources that were deleted from the Git repository.

**Self Heal**

Reverts manual changes made directly to Kubernetes when they differ from the desired Git configuration.

### Enable Auto-Sync on the Existing Application

The following command was used to update the existing Application:

```bash
kubectl patch application ivovle -n argocd --type merge -p '{"spec":{"syncPolicy":{"automated":{"prune":true,"selfHeal":true}}}}'
```

Observed output:

```text
application.argoproj.io/ivovle patched
```

The Application now has automated synchronization enabled.

---

# Part 2: Jenkins Integration with GitOps

## 10. Jenkins Shared Library Configuration

The Jenkins Shared Library contains reusable functions used by the pipeline.

For this lab, the GitOps deployment function is:

```text
Jenkins/Lab22/shared-library/vars/deployOnK8s.groovy
```

The purpose of this function is to update the Kubernetes Deployment manifest in GitHub whenever Jenkins publishes a new Docker image.

### GitHub Credentials

A Jenkins credential is configured with the following ID:

```text
github
```

The Shared Library accesses the credential using Jenkins `withCredentials`.

Example:

```groovy
withCredentials([
    usernamePassword(
        credentialsId: 'github',
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_TOKEN'
    )
]) {
    // Git operations
}
```

**Security Note:** GitHub tokens must be stored in Jenkins Credentials and must never be written directly in the repository or pipeline code.

### GitOps Function Responsibilities

The `deployOnK8s` function performs these operations:

1. Receives the Docker image name and image tag.
2. Clones the GitHub repository.
3. Opens the Kubernetes Deployment manifest.
4. Replaces the old application image with the newly published image.
5. Configures the Git commit identity.
6. Stages the updated manifest.
7. Creates a Git commit.
8. Pushes the commit to the `main` branch.

### Update the Deployment Image

The function uses:

```bash
sed -i "s|image: saratarek1/demoapp:.*|image: ${APP_IMAGE}:${APP_TAG}|" Jenkins/Lab22/deployment.yaml
```

**Explanation:**

- `sed -i` edits the file directly.
- The old `saratarek1/demoapp` image tag is matched.
- The image reference is replaced with the new image name and tag.
- The updated manifest becomes the desired deployment configuration in Git.

### Git Operations

The function performs the equivalent of:

```bash
git add Jenkins/Lab22/deployment.yaml
git commit -m "Update demoapp image"
git push origin HEAD:main
```

This pushes the updated Kubernetes manifest to GitHub.

---

## 11. Jenkins Pipeline Execution

The Jenkins job used in this lab is:

```text
Lab22-CICD
```

The pipeline loads the Shared Library and invokes the application build, Docker image build, and GitOps deployment functions.

The main function calls are:

```groovy
buildApp()

buildImage(env.DOCKER_IMAGE, env.IMAGE_TAG)

deployOnK8s(env.DOCKER_IMAGE, env.IMAGE_TAG)
```

### Pipeline Stages

#### 1. Build Application

Jenkins builds the application and executes the configured tests.

#### 2. Build Docker Image

Jenkins creates a Docker image using the application build output.

#### 3. Scan Docker Image

Trivy scans the image for known vulnerabilities.

#### 4. Push Docker Image

The newly built image is pushed to Docker Hub.

#### 5. Update GitOps Manifest

The Shared Library updates the Deployment manifest and pushes the changes to GitHub.

### Successful Jenkins Build

The successful pipeline execution was Build #13.

Published image:

```text
saratarek1/demoapp:13
```

The Jenkins pipeline completed successfully.

---

## 12. Verify the GitHub Manifest Update

After Jenkins finishes successfully, verify that the new image tag was pushed to GitHub.

### Step 1: Navigate to the repository

```bash
cd ~/Training-labs
```

### Step 2: Fetch the latest changes

```bash
git fetch origin
```

This downloads the latest remote references without changing the local working files.

### Step 3: Check the Deployment image

```bash
git show origin/main:Jenkins/Lab22/deployment.yaml | grep 'image:'
```

Observed output:

```text
image: mysql:8.0
image: saratarek1/demoapp:13
```

**Result:** Jenkins successfully updated the application image tag in the GitHub Deployment manifest.

The new image is:

```text
saratarek1/demoapp:13
```

---

# Part 3: Argo CD Deployment and Verification

## 13. Automatic Synchronization with Argo CD

After Jenkins pushes the updated manifest to GitHub:

1. Argo CD detects the new commit.
2. Argo CD compares the Git configuration with the live Kubernetes resources.
3. Argo CD identifies the image change.
4. Automated synchronization applies the updated configuration.
5. Kubernetes starts the new application version.

No manual Deployment manifest application is required for this GitOps image update.

### Check Application Status

```bash
kubectl get application ivovle -n argocd
```

Final observed output:

```text
NAME     SYNC STATUS   HEALTH STATUS
ivovle   Synced        Healthy
```

### Status Explanation

- `Synced`: The live Kubernetes resources match the desired configuration stored in Git.
- `Healthy`: The deployed resources are healthy according to Argo CD health checks.

This confirms that Argo CD successfully synchronized the GitHub changes.

---

## 14. Verify Kubernetes Application Deployment

### Step 1: Check application pods

```bash
kubectl get pods -n ivolve
```

Observed output:

```text
NAME                         READY   STATUS    RESTARTS       AGE
ivolveapp-7ffb95d855-ftll9   1/1     Running   0              5s
ivolveapp-7ffb95d855-jhrnz   1/1     Running   0              33s
ivolvedb-0                   1/1     Running   3 (3h7m ago)   18h
```

**Result:**

- Both application pods are Running.
- The database pod is Running.
- All listed pods are Ready.

### Step 2: Verify the deployed application image

```bash
kubectl get deployment ivolveapp -n ivolve -o jsonpath='{.spec.template.spec.containers[*].image}{"\n"}'
```

Observed output:

```text
saratarek1/demoapp:13
```

This confirms that Kubernetes is running the same image version published by Jenkins and referenced in GitHub.

### Step 3: Verify Argo CD one final time

```bash
kubectl get application ivovle -n argocd
```

Final expected status:

```text
Synced   Healthy
```

The deployment and GitOps synchronization were successful.

---

## 15. Troubleshooting and Important Notes

### Argo CD Dashboard Is Not Accessible

Verify that port forwarding is active:

```bash
kubectl port-forward svc/argocd-server -n argocd 8082:443
```

Keep the terminal open while using the dashboard.

### Argo CD Application Is OutOfSync

Check the Application:

```bash
kubectl get application ivovle -n argocd
```

Verify:
- GitHub repository URL.
- Target branch.
- Manifest directory.
- Automated synchronization settings.

### Jenkins Cannot Push to GitHub

Check that the Jenkins credential exists and its ID is:

```text
github
```

Verify that it has the required repository permissions.

### Kubernetes Pods Are Not Running

Check the pods:

```bash
kubectl get pods -n ivolve
```

Inspect a specific pod:

```bash
kubectl describe pod <pod-name> -n ivolve
```

Check logs when necessary:

```bash
kubectl logs <pod-name> -n ivolve
```

### Important GitOps Concept

The Git repository stores the desired Kubernetes state.

Jenkins updates that desired state by pushing manifest changes to GitHub.

Argo CD continuously compares the desired state with the live cluster state and synchronizes differences.

This is the main principle of GitOps.

---

## 16. Final Results

The GitOps workflow was successfully implemented and verified.

| Requirement | Result |
|---|---|
| Install Argo CD | Completed |
| Access Argo CD Dashboard | Completed |
| Connect GitHub Repository | Completed |
| Create Argo CD Application | Completed |
| Enable Automated Sync | Completed |
| Update Jenkins Shared Library | Completed |
| Execute Jenkins Pipeline | Successful |
| Build and Push Docker Image | Successful |
| Update Kubernetes Manifest in GitHub | Successful |
| Argo CD Synchronization | Synced |
| Argo CD Application Health | Healthy |
| Kubernetes Application Pods | Running |
| Verify Deployed Image | Successful |

### Final Verified Values

**Jenkins Build:**

```text
Build #13 - SUCCESS
```

**Docker Image:**

```text
saratarek1/demoapp:13
```

**GitHub Deployment Manifest:**

```text
image: saratarek1/demoapp:13
```

**Argo CD Application:**

```text
Name: ivovle
Sync Status: Synced
Health Status: Healthy
```

**Kubernetes Deployment:**

```text
Namespace: ivolve
Deployment: ivolveapp
Image: saratarek1/demoapp:13
```

### Conclusion

This lab demonstrates a complete GitOps workflow by integrating Jenkins, GitHub, Docker, Kubernetes, and Argo CD.

Jenkins automates application building, testing, container image creation, security scanning, and publishing.

After publishing the image, Jenkins updates the Kubernetes Deployment manifest in GitHub.

Argo CD detects the Git change and automatically synchronizes the Kubernetes cluster with the desired state.

This approach provides automated, consistent, and reliable application deployments while keeping Git as the single source of truth.

---

# 17. Screenshots

The following screenshots document the main stages and successful results of the lab.

### 1. Argo CD Dashboard

![Argo CD Dashboard](screenshots/argo%20dashboard.png)

### 2. GitHub Repository Connected to Argo CD

![GitHub Repository Connected](screenshots/Iviolve%20repo%20on%20argocd.png)

### 3. Jenkins Pipeline Successful

![Jenkins Pipeline Success](screenshots/pipeline%20success.png)

---

**End of Lab 23**

