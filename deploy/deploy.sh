#!/bin/bash

# This should be run on the deploy box.

cd /opt/apps/
git clone git@github.com:sgzmd/flibustier.git
cd flibustier
git pull
git checkout master
cd import
rm lib.*gz*
/home/sgzmd/.local/bin/pip3 install pipenv
/home/sgzmd/.local/bin/pipenv run python import.py
cd ..
mv import/flibusta.db ./
cd web
mvn package
JAR_NAME=`ls -1 target/flibustier-web*jar`

echo "Restarting Flibustier ... "
sudo systemctl restart flibustier
sleep 5
sudo tail /var/log/flibustier.log