#!/usr/bin/env bash

startup_options=( 
-Djava.security.egd=file:/dev/./urandom
-Dspring.jpa.hibernate.ddl-auto=update
-Dlogging.file=/var/log/flibustier.log
-Dspring.profiles.active=prod
-Dflibustier.myemail=flibustier@r-k.co
-Dflibusta.rpc.host=$FLIBUSERVER_HOST
-Dflibusta.rpc.port=$FLIBUSERVER_PORT
-Dspring.datasource.url=jdbc:mysql://$MARIA_HOST:$MARIA_PORT/$MYSQL_DATABASE
-Dspring.datasource.driver-class-name=org.mariadb.jdbc.Driver
-Dspring.datasource.username=$MYSQL_USER
-Dspring.datasource.password=$MYSQL_PASSWORD
-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDB53Dialect
-Dspring.security.oauth2.client.registration.google.client-id=$GOOGLE_CLIENT_ID
-Dspring.security.oauth2.client.registration.google.client-secret=$GOOGLE_CLIENT_SECRET
-Dsmtp.username=$SMTP_USERNAME
-Dsmtp.password=$SMTP_PASSWORD
)

CMD="java -XX:MaxMetaspaceSize=128M ${startup_options[*]} -jar app.jar"
echo $CMD

$CMD
