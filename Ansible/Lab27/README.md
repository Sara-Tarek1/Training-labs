# Lab 27: Securing Sensitive Data with Ansible Vault

## Overview

The goal of this lab is to automate MySQL installation and configuration on a managed node using Ansible Roles, while protecting sensitive information using Ansible Vault.

### Tasks
- Install MySQL Server.
- Create a database named `ivolve`.
- Create a MySQL user named `ivolveuser`.
- Grant all privileges on the `ivolve` database to `ivolveuser`.
- Encrypt the database password using Ansible Vault.
- Verify database access using the created user.

## Project Structure

```text
Lab27/
├── install-mysql.yml
├── inventory.ini
├── vault.yml
├── README.md
└── roles/
    └── mysql/
        └── tasks/
            └── main.yml
```

## Ansible Role

The MySQL role is located at:

`roles/mysql/tasks/main.yml`

It performs the following tasks:

1. Installs `mysql-server` and `python3-pymysql`.
2. Starts and enables the MySQL service.
3. Creates the `ivolve` database.
4. Creates `ivolveuser` and grants all privileges on `ivolve`.

## Ansible Vault

Sensitive information, such as the MySQL password, is stored in `vault.yml`.

The file is encrypted using Ansible Vault:

```bash
ansible-vault create vault.yml
```

To view the encrypted file:

```bash
cat vault.yml
```

To edit the encrypted file:

```bash
ansible-vault edit vault.yml
```

**Note:** The Vault password and database password must never be committed to GitHub in plain text.

## Playbook Execution

Check the playbook syntax:

```bash
ansible-playbook -i inventory.ini --syntax-check install-mysql.yml --ask-vault-pass
```

Run the playbook:

```bash
ansible-playbook -i inventory.ini install-mysql.yml --ask-vault-pass
```

The playbook installs and configures MySQL on the managed node using the MySQL role.

## Verification

Connect to the managed node's MySQL instance using the created user:

```bash
docker exec ansible-managed-node bash -c 'MYSQL_PWD="<MYSQL_PASSWORD>" mysql -u ivolveuser -D ivolve -e "SHOW DATABASES;"'
```

Expected output:

```text
Database
information_schema
ivolve
performance_schema
```

The output confirms that `ivolveuser` can successfully authenticate and access the `ivolve` database.

## Result

**Lab 27 completed successfully.**

- MySQL installed successfully.
- Database `ivolve` created.
- User `ivolveuser` created with the required privileges.
- Sensitive password protected using Ansible Vault.
- Database access successfully verified.
