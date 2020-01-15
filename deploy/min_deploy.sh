#!/bin/bash -x

# This should be run on the deploy box.

set -e

cd /opt/apps/flibustier
git pull
git checkout master

echo "Restarting Flibustier ... "
sudo systemctl restart flibustier
sleep 5
sudo tail /var/log/flibustier.log