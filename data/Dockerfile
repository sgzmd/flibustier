FROM tetafro/golang-gcc:latest

ENV test=0
ENV telegram_api_key=nonkey
ENV telegram_chat_id=-1

RUN mkdir /app
# RUN apt update && apt install -y sqlite3 gawk bash gcc wget cron curl
RUN apk add sqlite gawk bash wget curl

# RUN wget https://golang.org/dl/go1.17.3.linux-amd64.tar.gz
# RUN rm -rf /usr/local/go && tar -C /usr/local -xzf go1.17.3.linux-amd64.tar.gz

ADD . /app

WORKDIR /app

RUN go build -o downloader cmd/downloader/main.go
RUN go build -o flibustier_server -tags fts5 flibuserver/server/*.go

FROM alpine:latest
RUN mkdir /app
RUN apk add sqlite gawk bash curl

ADD . /app

WORKDIR /app

COPY --from=0 /app/downloader /app/downloader
COPY --from=0 /app/flibustier_server /app/flibustier_server

RUN crontab downloader.cron

EXPOSE 9000

CMD /app/downloader_launcher.sh ; /app/flibustier_server --flibusta_db=/app/flibusta.db --port=9000

