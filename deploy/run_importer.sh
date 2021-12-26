#!/bin/bash -x

# To be run by cron, as user
cd /opt/apps/flibustier
git pull
cd import
rm lib.*gz*
echo "Archiving old database just in case ..."
bzip2 --best -z flibusta.db -c > flibusta_db_`(date -uI)`.db.bz2
echo "Database archivedm, proceeding."
/home/sgzmd/.local/bin/pipenv run python import.py
mv flibusta.db ../

echo "Success!"

