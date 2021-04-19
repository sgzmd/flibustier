#!/usr/bin/env bash

# Run this script as
#   ./run_dev.sh <google-client-id> <google-client-secret>

GOOGLE_CLIENT_ID="${1:-error}"
GOOGLE_CLIENT_SECRET="${2:-error}"

if [ $GOOGLE_CLIENT_SECRET = "error" ]; then
  echo "GOOGLE_CLIENT_SECRET must be specified"
fi

if [ $GOOGLE_CLIENT_ID = "error" ]; then
  echo "GOOGLE_CLIENT_ID must be specified"
fi

startup_options=(
-Dspring.jpa.hibernate.ddl-auto=update
-Dlogging.file=./flibustier.log
-Dspring.profiles.active=dev
-Dflibusta.dburl=jdbc:sqlite:../flibusta.db
-Dflibustier.myemail=flibustier@r-k.co
-Dspring.datasource.url=jdbc:h2:mem:test
-Dspring.jpa.hibernate.ddl-auto=create
-Dspring.datasource.driver-class-name=org.h2.Driver
-Dspring.datasource.username=sa
-Dspring.datasource.password="sa"
-Dspring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
-Dspring.security.oauth2.client.registration.google.client-id=$GOOGLE_CLIENT_ID
-Dspring.security.oauth2.client.registration.google.client-secret=$GOOGLE_CLIENT_SECRET
)

mvn package
java -jar "${startup_options[*]}" -jar target/flibustier-web-0.0.1-SNAPSHOT.jar