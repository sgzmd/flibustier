FROM debian:stable

ENV PORT=9000

ENV test=0
ENV telegram_api_key=nonkey
ENV telegram_chat_id=-1

RUN mkdir /app
RUN apt update && apt install -y sqlite3 gawk bash gcc wget cron curl

RUN wget https://golang.org/dl/go1.17.3.linux-amd64.tar.gz
RUN rm -rf /usr/local/go && tar -C /usr/local -xzf go1.17.3.linux-amd64.tar.gz

ADD . /app

WORKDIR /app

RUN /usr/local/go/bin/go build -o downloader cmd/downloader/main.go
RUN /usr/local/go/bin/go build -o flibustier_server -tags fts5 flibuserver/server/*.go

RUN cat downloader.cron | crontab

EXPOSE $PORT

CMD /app/downloader_launcher.sh ; /app/flibustier_server --flibusta_db=/app/flibusta.db --port=$PORT

