packer {
  required_plugins {
    amazon = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/amazon"
    }
  }
}

# Variable definitions
variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "source_ami" {
  type    = string
  default = "ami-0866a3c8686eaeeba"
}

variable "instance_type" {
  type    = string
  default = "t2.micro"
}

variable "ssh_username" {
  type    = string
  default = "ubuntu"
}

variable "subnet_id" {
  type    = string
  default = null
}

variable "ami_users" {
  type    = list(string)
  default = ["794038208840", "724772077256"]
}

variable "ami_name_prefix" {
  type    = string
  default = "csye6225"
}

source "amazon-ebs" "ubuntu" {
  region          = var.aws_region
  instance_type   = var.instance_type
  ssh_username    = var.ssh_username
  ami_name        = "${var.ami_name_prefix}-${formatdate("YYYY-MM-DD-hh-mm-ss", timestamp())}"
  ami_description = "Ubuntu AMI for CSYE6225 with Java application"
  ami_users       = var.ami_users

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"]
  }

  launch_block_device_mappings {
    device_name           = "/dev/sda1"
    volume_size           = 25
    volume_type           = "gp2"
    delete_on_termination = true
  }
}

build {
  name    = "csye6225-ami"
  sources = ["source.amazon-ebs.ubuntu"]

  provisioner "file" {
    source      = "./../target/demo-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/webapp.jar"
  }

  provisioner "file" {
    source      = "scripts/setup_system.sh"
    destination = "/tmp/setup_system.sh"
  }

  provisioner "file" {
    source      = "scripts/setup_application.sh"
    destination = "/tmp/setup_application.sh"
  }

  provisioner "file" {
    source      = "config/csye6225.service"
    destination = "/tmp/csye6225.service"
  }

  provisioner "file" {
    source      = "config/amazon-cloudwatch-agent.json"
    destination = "/tmp/amazon-cloudwatch-agent.json"
  }

  provisioner "shell" {
    inline = [
      "chmod +x /tmp/setup_system.sh",
      "sudo /tmp/setup_system.sh",
      "chmod +x /tmp/setup_application.sh",
      "sudo /tmp/setup_application.sh",
      "sudo mv /tmp/amazon-cloudwatch-agent.json /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json",
      "sudo systemctl enable amazon-cloudwatch-agent"
    ]
  }

  provisioner "shell" {
    inline = [
      "echo 'Verifying installation...'",
      "java -version",
      "sudo systemctl status csye6225 || true",
      "sudo systemctl status amazon-cloudwatch-agent || true",
      "ls -la /opt/csye6225",
      "ls -la /var/log/webapp",
      "ls -la /etc/csye6225"
    ]
  }
}