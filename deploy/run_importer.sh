#!/bin/bash -x

# To be run by cron, as user
cd /opt/apps/flibustier
git pull
cd import
/home/sgzmd/.local/bin/pipenv run python import.py
mv flibusta.db ../

echo "Success!"



