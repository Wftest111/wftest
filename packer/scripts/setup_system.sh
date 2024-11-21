#!/bin/bash
set -e

echo "Starting system setup..."

# Function to log messages
log_message() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [INFO] $1" | tee -a /var/log/syslog
}

# Function to check command status
check_command() {
    if [ $? -ne 0 ]; then
        log_message "ERROR: $1 failed"
        exit 1
    fi
    log_message "SUCCESS: $1 completed"
}

# Update and install packages
apt-get update
check_command "System update"
apt-get upgrade -y
check_command "System upgrade"

# Install required packages
apt-get install -y maven openjdk-17-jdk curl unzip
check_command "Package installation"

# Install CloudWatch Agent
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
dpkg -i -E ./amazon-cloudwatch-agent.deb
check_command "CloudWatch agent installation"
rm -f ./amazon-cloudwatch-agent.deb

# Create application user and group
useradd -m -s /bin/bash csye6225 || log_message "User already exists"
groupadd -f csye6225
usermod -aG csye6225 csye6225
check_command "User and group setup"

# Create necessary directories with proper permissions
directories=(
    "/opt/csye6225"
    "/etc/csye6225"
    "/var/log/webapp"
    "/opt/aws/amazon-cloudwatch-agent/etc"
)

for dir in "${directories[@]}"; do
    mkdir -p "$dir"
    chown -R csye6225:csye6225 "$dir"
    chmod 755 "$dir"
    check_command "Directory setup: $dir"
done

# Initialize log file
touch /var/log/webapp/application.log
chown csye6225:csye6225 /var/log/webapp/application.log
chmod 644 /var/log/webapp/application.log
check_command "Log file initialization"

# Clean up
apt-get clean
rm -rf /var/lib/apt/lists/*

log_message "System setup completed successfully"