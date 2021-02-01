import logging
import os
import requests
import threading
import time


from urllib.parse import urlparse

logging.basicConfig(level=logging.DEBUG)

os.system("rm -rf *.gz *.sql ./dump")

DB_HOST = os.environ.get('DB_HOST') or "roman.mshome.net"
DB_USERNAME = os.environ.get('DB_USERNAME') or "root"
DB_PASSWORD = os.environ.get('DB_PASSWORD') or "toor"

FILES = [
    "http://flibustier.lan/sql/lib.libavtor.sql.gz",
    "http://flibustier.lan/sql/lib.libavtorname.sql.gz",
    "http://flibustier.lan/sql/lib.libbook.sql.gz",
    "http://flibustier.lan/sql/lib.libseqname.sql.gz",
    "http://flibustier.lan/sql/lib.libseq.sql.gz"
]

def download(url):
    parsed_url = urlparse(url)
    file_name = os.path.basename(parsed_url.path)
    logging.info(f"Downloading {url} to {file_name}")
    r = requests.get(url, allow_redirects=True)
    open(file_name, 'wb').write(r.content)

threads = map(lambda x: threading.Thread(target=download, args=(x,)), FILES)
for thread in threads:
    thread.start()

for thread in threads:
    thread.join()

time.sleep(5)

os.system("gunzip *.gz")
os.system("mkdir dump")
os.system("cat *.sql > dump/dump.sql")

cmd = f"mysql --user={DB_USERNAME} --password={DB_PASSWORD} --host={DB_HOST} -e 'CREATE DATABASE fli_temp;'"
logging.info(f"Running command {cmd}")
os.system(cmd)
os.system(f"mysql --user={DB_USERNAME} --password={DB_PASSWORD} --host={DB_HOST} fli_temp < dump/dump.sql")