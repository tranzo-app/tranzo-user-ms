# Go Live: tranzo-user-ms on AWS (Single Guide)

One document for taking **tranzo-user-ms** live on AWS with your **GoDaddy domain**, HTTPS, and GitHub deploy. Do the steps in order.

**Architecture:** Internet → ALB (80/443) → EC2 (Docker on 8083). Your domain points to the ALB.

**Server OS:** This guide uses **Amazon Linux** (Amazon Linux 2 or 2023) on EC2. The default user is **ec2-user**; app directory is `/home/ec2-user/tranzo-user-ms`. No changes are needed in the **Dockerfile** or **docker-compose.yml** — they run the same on any host OS.

---

## What You Need Before Starting

- AWS account
- Domain on GoDaddy (e.g. `yourdomain.com`)
- GitHub repo with this code and the deploy workflow (push to `master` triggers deploy)

---

## Step 1: Create VPC and Subnets

1. In AWS Console open **VPC** (search “VPC” in the top bar).
2. Click **Create VPC**.
3. Choose **VPC and more**.
4. Set:
   - **Name:** `tranzo-vpc`
   - **IPv4 CIDR:** `10.0.0.0/16`
   - **Availability Zones:** 2
   - **Public subnets:** 2
   - **Private subnets:** 0
   - **NAT gateway:** None
   - Leave **Create internet gateway** checked.
5. Click **Create VPC**.
6. Note and save:
   - **VPC ID** (e.g. `vpc-xxxxxxxx`)
   - The two **public subnet** IDs (e.g. `subnet-xxxx` in two different AZs).

---

## Step 2: Create Security Groups

### 2.1 Security group for the Load Balancer (ALB)

1. In **VPC** go to **Security Groups** → **Create security group**.
2. **Name:** `tranzo-alb-sg`
3. **VPC:** select `tranzo-vpc`.
4. **Inbound rules** — Add:
   - Type **HTTP**, Port **80**, Source **0.0.0.0/0**
   - Type **HTTPS**, Port **443**, Source **0.0.0.0/0**
5. **Outbound:** leave default (all traffic).
6. **Create security group**. Copy the **Security group ID** (e.g. `sg-xxxxxxxx`) and label it **ALB_SG_ID**.

### 2.2 Security group for the EC2 server

1. **Security Groups** → **Create security group** again.
2. **Name:** `tranzo-ec2-sg`
3. **VPC:** `tranzo-vpc`.
4. **Inbound rules** — Add:
   - Type **Custom TCP**, Port **8083**, Source: click and choose **tranzo-alb-sg** (the ALB security group).
   - Type **SSH**, Port **22**, Source **My IP** (or **0.0.0.0/0** only if you need GitHub Actions from anywhere; less secure).
5. **Create security group**. Note **EC2_SG_ID**.

---

## Step 3: Create EC2 Key Pair (for SSH and GitHub)

1. In **EC2** go to **Key Pairs** (under Network & Security) → **Create key pair**.
2. **Name:** `tranzo-deploy`
3. **Format:** `.pem`
4. **Create** and **download** the `.pem` file. Keep it safe; you will use it for SSH and for the GitHub secret.

---

## Step 4: Launch EC2 Instance (Amazon Linux)

1. **EC2** → **Instances** → **Launch instance**.
2. **Name:** `tranzo-user-ms-server`
3. **AMI:** **Amazon Linux 2023** or **Amazon Linux 2** (choose one; user data below is for both).
4. **Instance type:** e.g. **t3.small** (or **t3.micro** if still in free tier).
5. **Key pair:** select **tranzo-deploy**.
6. **Network settings:**
   - **VPC:** tranzo-vpc
   - **Subnet:** one of the **public subnets**
   - **Auto-assign public IP:** Enable
   - **Firewall:** **Select existing** → **tranzo-ec2-sg**
7. **Advanced details** → scroll to **User data** → paste this (as text):

**For Amazon Linux 2023:**

```bash
#!/bin/bash
set -e
dnf update -y
dnf install -y docker
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user
dnf install -y docker-compose-plugin || true
mkdir -p /home/ec2-user/tranzo-user-ms
chown ec2-user:ec2-user /home/ec2-user/tranzo-user-ms
```

**For Amazon Linux 2** (if you picked AL2 AMI instead):

```bash
#!/bin/bash
set -e
yum update -y
yum install -y yum-utils
yum-config-manager --add-repo https://download.docker.com/linux/amazon/docker-ce.repo
yum install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user
mkdir -p /home/ec2-user/tranzo-user-ms
chown ec2-user:ec2-user /home/ec2-user/tranzo-user-ms
```

8. **Launch instance**.
9. Wait until **Instance state** is **Running**. Copy the **Public IPv4 address** (e.g. `3.x.x.x`) — this is your **SERVER_IP** for GitHub.

### 4.1 Allow ec2-user to run Docker without password (for GitHub Actions)

1. On your Mac, open Terminal. SSH into the server (default user for Amazon Linux is **ec2-user**; replace with your key path and SERVER_IP):

   ```bash
   chmod 400 ~/Downloads/tranzo-deploy.pem
   ssh -i ~/Downloads/tranzo-deploy.pem ec2-user@<SERVER_IP>
   ```

2. On the server run:

   ```bash
   sudo usermod -aG docker ec2-user
   sudo visudo
   ```

3. In the editor, add this line at the end (then save and exit, e.g. in nano: Ctrl+O, Enter, Ctrl+X):

   ```
   ec2-user ALL=(ALL) NOPASSWD: /usr/bin/docker
   ```

   Allowing `/usr/bin/docker` lets `sudo docker compose` work (the compose plugin runs as a subcommand of docker). If your install uses a separate binary, you can instead add: `ec2-user ALL=(ALL) NOPASSWD: /usr/bin/docker, /usr/bin/docker compose`.

4. Exit SSH: `exit`. Log in again once so the docker group is applied.

---

## Step 5: Request SSL Certificate in AWS (ACM)

1. In AWS Console open **Certificate Manager** (search “Certificate Manager”). **Switch region** to the same region as your EC2 (e.g. us-east-1).
2. Click **Request certificate**.
3. **Certificate type:** Request a public certificate.
4. **Domain names:** add the name you will use for this API, e.g.:
   - `api.yourdomain.com`  
   Or both:
   - `api.yourdomain.com`
   - `www.api.yourdomain.com`
5. **Validation method:** DNS validation.
6. **Request**.
7. On the certificate detail page you will see **CNAME name** and **CNAME value** for each domain. **Leave this tab open** — you will add these in GoDaddy in the next step.

---

## Step 6: Add ACM Validation CNAME in GoDaddy

1. Log in to **GoDaddy** → **My Products** → your domain → **DNS** (or **Manage DNS**).
2. Click **Add** (or **Add Record**).
3. **Type:** CNAME  
   **Name:** copy from ACM’s “CNAME name” but **remove the domain suffix**.  
   Example: if ACM shows `_abc123.api.yourdomain.com`, use only `_abc123.api` (or what GoDaddy expects; some want the full name, some only the subdomain part — follow GoDaddy’s hint).
4. **Value:** paste the full **CNAME value** from ACM (e.g. `_xyz.acm-validations.aws.`).
5. **TTL:** 600 or default. Save.
6. Repeat for every domain you added in ACM (e.g. second CNAME for `www.api...`).
7. Back in **AWS Certificate Manager**, wait until the certificate status is **Issued** (can take 5–30 minutes). You will use this certificate on the ALB.

---

## Step 7: Create Target Group and Load Balancer (ALB)

### 7.1 Create target group

1. **EC2** → **Target Groups** (left menu) → **Create target group**.
2. **Target type:** Instances.
3. **Name:** `tranzo-user-ms-tg`
4. **Protocol:** HTTP, **Port:** **8083**
5. **VPC:** tranzo-vpc.
6. **Health check:**  
   - **Path:** `/actuator/health` (if your app has Actuator) or `/`  
   - **Protocol:** HTTP, **Port:** 8083
7. **Create target group**.
8. Open **tranzo-user-ms-tg** → **Targets** tab → **Register targets** → select your **tranzo-user-ms-server** instance, port **8083** → **Include as pending below** → **Add to target group**.

### 7.2 Create Application Load Balancer

1. **EC2** → **Load Balancers** → **Create load balancer**.
2. Choose **Application Load Balancer**.
3. **Name:** `tranzo-alb`
4. **Scheme:** Internet-facing.
5. **Network mapping:**
   - **VPC:** tranzo-vpc
   - **Mappings:** select **both public subnets** (two AZs).
6. **Security groups:** remove default, **Add** **tranzo-alb-sg**.
7. **Listeners:**
   - **Add listener:** **HTTPS**, **443**.  
     **Default action:** Forward to **tranzo-user-ms-tg**.  
     **Secure listener settings** → **From ACM** → select your **Issued** certificate (e.g. `api.yourdomain.com`).  
     **Save**.
   - **Add listener:** **HTTP**, **80**.  
     **Default action:** **Redirect to** → Protocol **HTTPS**, Port **443**.  
     **Save**.
8. **Create load balancer**.
9. Open the new ALB → **Details** tab. Copy the **DNS name** (e.g. `tranzo-alb-xxxxx.us-east-1.elb.amazonaws.com`). This is your **ALB_DNS_NAME**.

---

## Step 8: Point Your GoDaddy Domain to the ALB

1. In **GoDaddy** → your domain → **DNS**.
2. **Add** a record:
   - **Type:** CNAME (or A if GoDaddy supports “alias” to another name)
   - **Name:** the subdomain you use for the API, e.g. `api` (so the URL is `https://api.yourdomain.com`)
   - **Value:** paste **ALB_DNS_NAME** (e.g. `tranzo-alb-xxxxx.us-east-1.elb.amazonaws.com`) — **no** `https://`, just the hostname.
   - **TTL:** 600 or default.
3. Save. DNS can take a few minutes to an hour to propagate.

---

## Step 9: Add GitHub Secrets (for Deploy Workflow)

1. Open your **GitHub repo** → **Settings** → **Secrets and variables** → **Actions**.
2. **New repository secret:**
   - **Name:** `SERVER_IP`  
     **Value:** the EC2 **Public IPv4** from Step 4.
3. **New repository secret:**
   - **Name:** `SSH_PRIVATE_KEY`  
     **Value:** open your **tranzo-deploy.pem** in a text editor and paste the **entire** content (including `-----BEGIN ...` and `-----END ...`).

---

## Step 10: First Deploy and Go-Live Check

1. **Trigger deploy:** either push/merge to **master**, or in GitHub **Actions** tab run the **Build & Deploy** workflow manually (**Run workflow**).
2. Wait for the workflow to finish (Checkout → Transfer code → Build and deploy on server).
3. On the server the workflow runs:  
   `cd /home/ec2-user/tranzo-user-ms` and `sudo docker compose up -d --build --force-recreate --no-deps app`.  
   So the image is **built on the EC2 server** and the app runs in Docker on port 8083.
4. In AWS **EC2** → **Target Groups** → **tranzo-user-ms-tg** → **Targets**. Wait until the target shows **Healthy** (can take 1–2 minutes).
5. Test in browser or with curl:
   - **HTTPS:** `https://api.yourdomain.com` (use the name you set in GoDaddy in Step 8).
   - You should get a response (e.g. 401 if the API requires auth, or your app’s response).

---

## Order Summary (Go Live Checklist)

| Order | What to do |
|-------|------------|
| 1 | Create VPC and 2 public subnets |
| 2 | Create ALB security group (80, 443) and EC2 security group (8083 from ALB, 22 for SSH) |
| 3 | Create EC2 key pair and download .pem |
| 4 | Launch EC2 with user data (Docker + directory), then add sudo rule for docker/docker compose |
| 5 | Request certificate in ACM for your domain (e.g. api.yourdomain.com) |
| 6 | Add ACM CNAME in GoDaddy until certificate is Issued |
| 7 | Create target group (8083), register EC2, create ALB with HTTPS (443) and HTTP→HTTPS redirect (80) |
| 8 | In GoDaddy add CNAME (e.g. api → ALB DNS name) |
| 9 | Add GitHub secrets SERVER_IP and SSH_PRIVATE_KEY |
| 10 | Run deploy (push to master or manual workflow), wait for healthy target, test https://api.yourdomain.com |

---

## Version History and Rollback

**Seeing previous versions:** Every merge to `master` is a commit. In GitHub go to **Code** → **Commits** (or the commit history of any file) to see all past versions. Each commit has a unique **SHA** (e.g. `0629082` or full `0629082...`).

**Reverting to a previous version:** You can deploy an older version without changing `master`:

1. In GitHub go to **Actions** → **Build & Deploy** → **Run workflow**.
2. In **Branch** leave `master` (or pick the branch that has the version you want).
3. In **ref** (if shown) enter the **commit SHA** or **branch/tag** you want to deploy:
   - **Commit SHA:** e.g. `0629082` or the full 40-character SHA from the commit list.
   - **Branch/tag:** e.g. `release/v1` if you use release branches.
4. Click **Run workflow**. The workflow will checkout that ref, copy it to the server, build the image, and start the container. The server will run that version until the next deploy.

So: push/merge to master = deploy latest; manual **Run workflow** with a **ref** = deploy that version (rollback or re-deploy any previous commit).

---

## How It All Connects

- **User** visits `https://api.yourdomain.com` → GoDaddy DNS points to **ALB**.
- **ALB** (HTTPS 443, certificate from ACM) forwards to **EC2:8083** (target group).
- **EC2** security group allows 8083 only from the ALB security group; SSH 22 from you/GitHub.
- **EC2** runs Docker; the deploy workflow copies code and runs `docker compose up -d --build` so the image is built on the server and the app goes live.

This is the only deployment doc for this project; use it as the single guide to go live.
