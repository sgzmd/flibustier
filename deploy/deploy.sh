#!/bin/bash

# This should be run on the deploy box.

cd /opt/apps/
git clone git@github.com:sgzmd/flibustier.git
cd flibustier
git pull
git checkout master
cd import
/home/sgzmd/.local/bin/pip3 install pipenv
/home/sgzmd/.local/bin/pipenv run python import.py
cd ..
mv import/flibusta.db ./
cd web
mvn package
JAR_NAME=`ls -1 target/flibustier-web*jar`

echo "java -Dspring.profiles.active=prod -Dflibusta.dburl=jdbc:sqlite:`pwd`/flibusta.db -Dspring.datasource.url=jdbc:h2:file:`pwd`/h2.db" -jar $JAR_NAME
