#!/usr/bin/env bash

function assert_exists {
  if ! command -v $1 &> /dev/null
  then
      echo "$1 could not be found"
      exit
  fi
}

function send_update {
  key=$1
  chat=$2
  text=$3

  if [ ! -z "$key" ] && [ ! -z "$chat" ] 
  then
  curl -X POST "https://api.telegram.org/bot$key/sendMessage" -d "chat_id=$chat&text=$text"
  echo $text
  else 
  echo "No Telegram, trace: $text"
  fi
}

# list of arguments expected in the input
optstring="d:t:c:f:"

active_dir="."
telegram_key=""
telegram_chat_id=""
flibusta_db_path="none.db"

while getopts ${optstring} arg; do
  case ${arg} in
    t) 
      echo "Telegram key ${OPTARG}"
      telegram_key="${OPTARG}"
      ;;
    c) 
      echo "Telegram chat ID ${OPTARG}"
      telegram_chat_id="${OPTARG}"
      ;;
    f) 
      flibusta_db_path="${OPTARG}"
      echo "Using flibusta db path $flibusta_db_path"
      ;;
    d) 
      active_dir="${OPTARG}"
      echo "Using active directory $active_dir"
      ;;
    :)
      echo "$0: Must supply an argument to -$OPTARG." >&2
      exit 1
      ;;
    ?)
      echo "Invalid option: -${OPTARG}."
      exit 2
      ;;
  esac
done

echo "Active dir: $active_dir, flibusta db: $flibusta_db_path, T-Key: $telegram_key, Chat ID: $telegram_chat_id"

if [ ! -d $active_dir ]; then 
  send_update $telegram_key $telegram_chat_id "Active directory $active_dir doesn't exist"
  
  exit 1
fi

cd $active_dir
echo "Will be creating $flibusta_db_path"

assert_exists awk
assert_exists sqlite3

# Downloading the files, this might take a while...
./downloader

if [ $? -ne 0 ]; then
    send_update $telegram_key $telegram_chat_id "File downloader has failed with error code $?"
    exit 11
fi

# Unpacking files
gunzip *.sql.gz

# Creating combined SQL dump file
cat lib*.sql > flibusta_mysql_dump.sql

# Applying MySQL -> sqlite3 converter script
awk -f mysql2sqlite flibusta_mysql_dump.sql > flibusta_sqlite_dump.sql
if [ $? -ne 0 ]; then
    send_update $telegram_key $telegram_chat_id "mysql2sqlite has failed with error code $?"
    exit 2
fi

# Removing old database copy if any
rm -rf flibusta_new.db

# Converting Flibusta SQLite3 dump to SQLite3 DB
sqlite3 flibusta_new.db < flibusta_sqlite_dump.sql

if [ $? -ne 0 ]; then
    send_update $telegram_key $telegram_chat_id "sqlite3 db creation has failed with error code $?"
    exit 3
fi

# Applying necessary patches to the DB
sqlite3 flibusta_new.db < SequenceAuthor.sql

if [ $? -ne 0 ]; then
    send_update $telegram_key $telegram_chat_id "sqlite3 patch has failed to apply with error code $?"
    exit 4
fi

# Sanity check
num_books=`sqlite3 flibusta_new.db "select count(1) from libbook"`

if [ $num_books -le 500000 ]; then 
  send_update $telegram_key $telegram_chat_id "Too few books ($num_books), abort, abort!"
else
  send_update $telegram_key $telegram_chat_id "New data dump looks ($num_books books) legitimate, creating $flibusta_db_path"
  mv flibusta_new.db $flibusta_db_path
  rm lib*.sql
fi