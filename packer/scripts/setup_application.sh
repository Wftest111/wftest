#!/bin/bash
set -e

echo "Starting application setup..."

# Function to log messages
log_message() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') [INFO] $1" | tee -a /var/log/webapp/application.log
}

# Verify application files
if [ ! -f /tmp/webapp.jar ]; then
    log_message "ERROR: webapp.jar not found in /tmp"
    exit 1
fi

# Move and configure application files
mv /tmp/webapp.jar /opt/csye6225/webapp.jar
chown csye6225:csye6225 /opt/csye6225/webapp.jar
chmod 500 /opt/csye6225/webapp.jar
log_message "Application files configured"

# Configure systemd service
if [ -f /tmp/csye6225.service ]; then
    mv /tmp/csye6225.service /etc/systemd/system/csye6225.service
    chmod 644 /etc/systemd/system/csye6225.service
    chown root:root /etc/systemd/system/csye6225.service
    log_message "Systemd service configured"
else
    log_message "ERROR: Service file not found"
    exit 1
fi

# Create application environment configuration
cat > /etc/csye6225/application-env << EOF
# Database Configuration
DB_URL=jdbc:postgresql://\${db_host}:5432/csye6225
DB_USER=csye6225
DB_PASSWORD=\${db_password}

# AWS Configuration
AWS_REGION=\${aws_region}
S3_BUCKET_NAME=\${s3_bucket}
EOF

chown csye6225:csye6225 /etc/csye6225/application-env
chmod 600 /etc/csye6225/application-env
log_message "Environment configuration created"

# Create application properties
cat > /opt/csye6225/application.properties << EOF
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=\${DB_URL}
spring.datasource.username=\${DB_USER}
spring.datasource.password=\${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# AWS Configuration
aws.s3.bucket=\${S3_BUCKET_NAME}
aws.region=\${AWS_REGION}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true

# Logging Configuration
logging.file.name=/var/log/webapp/application.log
logging.level.root=INFO
logging.level.com.sarthak.webapp=DEBUG
logging.level.org.springframework.web=INFO

# Metrics Configuration
management.endpoints.web.exposure.include=health,metrics
management.metrics.export.cloudwatch.enabled=true
management.metrics.export.cloudwatch.namespace=CSYE6225/WebApp
management.metrics.export.cloudwatch.step=1m
EOF

chown csye6225:csye6225 /opt/csye6225/application.properties
chmod 644 /opt/csye6225/application.properties
log_message "Application properties configured"

# Enable and start services
systemctl daemon-reload
systemctl enable csye6225
systemctl enable amazon-cloudwatch-agent
log_message "Services enabled"

log_message "Application setup completed successfully"