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
        URLopener().retrieve(url, href)

