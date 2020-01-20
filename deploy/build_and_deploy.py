import argparse
import os
import logging
import pprint
import time
from pathlib import Path
from subprocess import call, run


parser = argparse.ArgumentParser(description='Deploys Flibustier to VPS')
parser.add_argument('--vps_key', type=str, help='SSH key to use when connecting to the host')
parser.add_argument('--host', type=str, help='Host to communicate with')
parser.add_argument('--user', type=str, help='Username at remote host')
parser.add_argument('--remote_path', type=str, help="Path to Flibustier jar directory on remote host")

args = parser.parse_args()

def CHECK(returncode):
    if returncode != 0:
        logging.fatal("Last command failed, cannot continue")

def scp(args, cwd, fname):
    CHECK(call(["scp -i {} {} {}@{}:{}".format(
    args.vps_key,
    fname,
    args.user,
    args.host,
    args.remote_path)], shell=True, cwd=cwd))

def ssh(args, cwd, command):
    CHECK(call(["ssh -i {} {}@{} {}".format(
    args.vps_key,
    args.user,
    args.host,
    command)], shell=True, cwd=cwd))
    

dir_path = os.path.dirname(os.path.realpath(__file__))
path = Path(dir_path)
web = str(path.parent) + "/web"
CHECK(call(["mvn package"], cwd=web, shell=True))
target_jar = run(["ls -1 target/flibustier-web*jar"], cwd=web, shell=True, capture_output=True).stdout.decode('utf-8').strip()
print("Found target jar: {}".format(target_jar))

print("Copying JAR to VPS... ")
scp(args, web, target_jar)
print("Jar copied, restarting Flibustier.")

ssh(args, web, command="sudo systemctl restart flibustier")
print("Sleeping for 5 seconds... ")
time.sleep(5)
ssh(args, web, command="sudo tail /var/log/flibustier.log")