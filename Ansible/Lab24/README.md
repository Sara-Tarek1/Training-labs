# Lab 24: Initial Ansible Configuration and Ad-Hoc Execution

## Objective

The objective of this lab is to install and configure Ansible on the Control Node, establish SSH connectivity with a Managed Node, create an inventory file, and use Ansible Ad-Hoc commands to install Docker on the Managed Node.

## Environment

- **Control Node:** Ubuntu on WSL
- **Managed Node:** Ubuntu 24.04 Docker container
- **Ansible Version:** 2.20.1
- **SSH Port:** 2222
- **Managed Node User:** `ansible`

---

## Step 1: Install Ansible on the Control Node

Update the package repository and install Ansible Core.

**Commands:**

```bash
sudo apt update
sudo apt install ansible-core -y
```

**Verify Ansible installation:**

```bash
ansible --version
```

**Output:**

```text
ansible [core 2.20.1]
config file = None
configured module search path = ['/home/sara/.ansible/plugins/modules', '/usr/share/ansible/plugins/modules']
ansible python module location = /usr/lib/python3/dist-packages/ansible
ansible collection location = /home/sara/.ansible/collections:/usr/share/ansible/collections
executable location = /usr/bin/ansible
python version = 3.14.4
jinja version = 3.1.6
pyyaml version = 6.0.3
```

## Step 2: Generate an SSH Key Pair

Generate an ED25519 SSH key pair on the Control Node.

```bash
ssh-keygen -t ed25519 -C "sara-lab24"
```

The generated keys are stored in:

- Private Key: `~/.ssh/id_ed25519`
- Public Key: `~/.ssh/id_ed25519.pub`

The private key remains on the Control Node and must not be shared.

## Step 3: Prepare the Managed Node

Create an Ubuntu 24.04 container and expose its SSH port on host port 2222.

```bash
docker run -d \
  --name ansible-managed-node \
  -p 2222:22 \
  ubuntu:24.04 \
  sleep infinity
```

Install the required packages inside the container:

```bash
docker exec ansible-managed-node bash -c "apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y openssh-server python3 sudo"
```

Create the `ansible` user and configure passwordless sudo:

```bash
docker exec ansible-managed-node bash -c "useradd -m -s /bin/bash ansible && echo 'ansible:YOUR_PASSWORD' | chpasswd && echo 'ansible ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/ansible && chmod 0440 /etc/sudoers.d/ansible"
```

> Replace `YOUR_PASSWORD` with the password configured for the Managed Node. Do not commit the actual password to GitHub.

Start the SSH server:

```bash
docker exec ansible-managed-node bash -c "mkdir -p /run/sshd && sed -i 's/^#PasswordAuthentication yes/PasswordAuthentication yes/' /etc/ssh/sshd_config && /usr/sbin/sshd"
```

## Step 4: Copy the SSH Public Key to the Managed Node

Transfer the public key using `ssh-copy-id`:

```bash
ssh-copy-id -i ~/.ssh/id_ed25519.pub -p 2222 ansible@127.0.0.1
```

Test SSH key authentication:

```bash
ssh -i ~/.ssh/id_ed25519 -p 2222 ansible@127.0.0.1
```

**Output:**

```text
Welcome to Ubuntu 24.04.5 LTS ...
Last login: Mon Nov 9 14:20:37 2026 from 172.17.0.1
ansible@abb6e2df5bda:~$
```

The successful login without a password prompt confirms that SSH key authentication is working.

Exit the Managed Node:

```bash
exit
```

## Step 5: Create the Ansible Inventory

Create an inventory file in the Lab24 directory:

```bash
nano inventory.ini
```

Add the following configuration:

```ini
[managed]
managed-node ansible_host=127.0.0.1 ansible_port=2222 ansible_user=ansible ansible_ssh_private_key_file=~/.ssh/id_ed25519 ansible_python_interpreter=/usr/bin/python3
```

Verify the inventory:

```bash
cat inventory.ini
```

**Output:**

```ini
[managed]
managed-node ansible_host=127.0.0.1 ansible_port=2222 ansible_user=ansible ansible_ssh_private_key_file=~/.ssh/id_ed25519 ansible_python_interpreter=/usr/bin/python3
```

## Step 6: Test Ansible Connectivity

Use the Ansible ping module to verify communication with the Managed Node:

```bash
ansible managed -i inventory.ini -m ping
```

**Output:**

```text
managed-node | SUCCESS => {
    "changed": false,
    "ping": "pong"
}
```

The `pong` response confirms that Ansible can successfully connect to the Managed Node.

## Step 7: Install Docker Using an Ansible Ad-Hoc Command

Use the Ansible `apt` module with privilege escalation to install Docker on the Managed Node.

```bash
ansible managed -i inventory.ini -b -m apt -a "name=docker.io state=present update_cache=yes"
```

**Explanation:**

- `managed`: Target inventory group.
- `-i inventory.ini`: Specifies the inventory file.
- `-b`: Enables privilege escalation (sudo).
- `-m apt`: Uses the APT module.
- `name=docker.io`: Specifies the Docker package.
- `state=present`: Ensures that the package is installed.
- `update_cache=yes`: Updates the package cache before installation.

The installation output confirmed that Docker and its dependencies were installed:

```text
Setting up containerd (2.2.1-0ubuntu1~24.04.4) ...
Setting up docker.io (29.1.3-0ubuntu3~24.04.2) ...
info: Adding group `docker' (GID 103) ...
```

Some service startup warnings appeared because the Managed Node is a regular Docker container without a full systemd environment. The package installation itself succeeded.

## Step 8: Verify Docker Installation

Run the following command to check the Docker version on the Managed Node:

```bash
ansible managed -i inventory.ini -b -m command -a "docker --version"
```

**Output:**

```text
managed-node | CHANGED | rc=0 >>
Docker version 29.1.3, build 29.1.3-0ubuntu3~24.04.2
```

The output confirms that Docker was successfully installed on the Managed Node.

---

## Lab Summary

In this lab, the following tasks were completed:

1. Installed Ansible Core on the Control Node.
2. Generated an ED25519 SSH key pair.
3. Prepared an Ubuntu Managed Node.
4. Transferred the SSH public key using `ssh-copy-id`.
5. Configured the Ansible inventory file.
6. Verified connectivity using the Ansible ping module.
7. Installed Docker using an Ansible Ad-Hoc command.
8. Verified the Docker installation using `docker --version`.

**Result:** All required tasks in Lab 24 were completed successfully.
