#!/usr/bin/bash -x

mvn package && java -jar target/flibustier-2.0-SNAPSHOT.jar --spring.config.location=../config/authconfig.yml,classpath:/application.yml