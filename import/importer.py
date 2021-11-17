import argparse
import os
from bs4 import BeautifulSoup
from urllib.request import urlopen
from urllib.request import URLopener

import sanity_check    

BASE_URL = "http://flibusta.is/sql/"

parser = argparse.ArgumentParser(description="Downloads data dumps and converts to sqlite3")
parser.add_argument('--skip_download', default=False, action='store_true', help='Skips download')
parser.add_argument('--base_url', default=BASE_URL, help='Flibusta base URL')
args = parser.parse_args()

def build_url_list():
    urls = []
    soup = BeautifulSoup(urlopen(args.base_url).read(), 'html.parser')
    all_links = soup.find_all(name = "a")
    for a in all_links:
        href = a['href']
        if href.startswith('lib.lib'):
            urls.append(href)
    return urls

if __name__ == '__main__':
    if not args.skip_download:
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
    os.system("gunzip *.gz")
    os.system("cat lib*.sql > sqldump.sql")
    print("Converting to SQLite3...")
    os.system("/usr/bin/awk -f mysql2sqlite sqldump.sql | sqlite3 flibusta_new.db")
    print("Applying SQL scripts...")
    os.system("sqlite3 flibusta_new.db < SequenceAuthor.sql")
    print("All done")
    os.system("rm lib*.sql")

    if sanity_check.check_file_sanity("flibusta_new.db"):
        os.system("rm flibusta.db")
        os.system("mv flibusta_new.db flibusta.db")
    else:
        print("New file not sane, keeping things as is")
