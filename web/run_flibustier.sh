#!/usr/bin/env bash

# Run this script as
#   ./run_dev.sh <google-client-id> <google-client-secret>

# GOOGLE_CLIENT_ID="${1:-error}"
# GOOGLE_CLIENT_SECRET="${2:-error}"
# FLIBUSERVER_HOST="${3:-error}"
# FLIBUSERVER_PORT="${4:-error}"
# MARIA_HOST="${5:-error}"
# MARIA_PORT="${6:-error}"
# MARIA_DATABASE="${7:-error}"
# MARIA_USER="${8:-error}"
# MARIA_PASSWORD="${9:-error}"

startup_options=( 
-Dspring.jpa.hibernate.ddl-auto=update
-Dlogging.file=/var/log/flibustier.log
-Dspring.profiles.active=prod
-Dflibustier.myemail=flibustier@r-k.co
-Dflibusta.rpc.host=$FLIBUSERVER_HOST
-Dflibusta.rpc.port=$FLIBUSERVER_PORT
-Dspring.datasource.url=jdbc:mysql://$MARIA_HOST:$MARIA_PORT/$MYSQL_DATABASE
-Dspring.jpa.hibernate.ddl-auto=create
-Dspring.datasource.driver-class-name=org.mariadb.jdbc.Driver
-Dspring.datasource.username=$MYSQL_USER
-Dspring.datasource.password=$MYSQL_PASSWORD
-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDB53Dialect
-Dspring.security.oauth2.client.registration.google.client-id=$GOOGLE_CLIENT_ID
-Dspring.security.oauth2.client.registration.google.client-secret=$GOOGLE_CLIENT_SECRET
-Dsmtp.username=$SMTP_USERNAME
-Dsmtp.password=$SMTP_PASSWORD
)

CMD="java ${startup_options[*]} -jar app.jar"
echo $CMD

$CMD

sleep 10m