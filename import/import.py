import os
from bs4 import BeautifulSoup
from urllib.request import urlopen
from urllib.request import URLopener

BASE_URL = "http://flibusta.is/sql/"
soup = BeautifulSoup(urlopen(BASE_URL).read(), 'html.parser')
all_links = soup.find_all(name = "a")
for a in all_links:
    href = a['href']
    if href.startswith('lib.lib'):
        url = BASE_URL + href
        print("Downloading " + url)
        os.system("wget " + url)

print("Running gunzip ...")
os.system("rm flibusta.db")
os.system("gunzip *.gz")
os.system("cat lib*.sql > sqldump.sql")
print("Converting to SQLite3...")
os.system("/usr/bin/awk -f mysql2sqlite sqldump.sql | sqlite3 flibusta.db")
print("Applying SQL scripts...")
os.system("sqlite3 flibusta.db < SequenceAuthor.sql")
print("All done")
os.system("rm lib*.sql")
