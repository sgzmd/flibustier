#!/usr/bin/env bash

function assert_exists {
  if ! command -v $1 &> /dev/null
  then
      echo "$1 could not be found"
      exit
  fi
}

if [ "$#" -ne 2 ]; then
    echo "Usage: ./create_sqlite_file.sh <active directory> </path/to/final/flibusta.db>"
    exit 2
fi

active_dir=$1
flibusta_db_path=$2

if [ ! -d $active_dir ]; then 
  echo "Active directory $1 doesn't exist"
  exit 1
fi

cd $active_dir
echo "Will be creating $flibusta_db_path"

if [ ! -f "main" ]; then
  go build cmd/downloader/main.go
fi

assert_exists awk
assert_exists sqlite3

# Downloading the files, this might take a while...
./main

if [ $? -ne 0 ]; then
    echo "File downloader has failed with error code $?"
    exit 1
fi

# Unpacking files
gunzip *.sql.gz

# Creating combined SQL dump file
cat lib*.sql > flibusta_mysql_dump.sql

# Applying MySQL -> sqlite3 converter script
awk -f mysql2sqlite flibusta_mysql_dump.sql > flibusta_sqlite_dump.sql
if [ $? -ne 0 ]; then
    echo "mysql2sqlite has failed with error code $?"
    exit 2
fi

# Removing old database copy if any
rm -rf flibusta_new.db

# Converting Flibusta SQLite3 dump to SQLite3 DB
sqlite3 flibusta_new.db < flibusta_sqlite_dump.sql

if [ $? -ne 0 ]; then
    echo "sqlite3 db creation has failed with error code $?"
    exit 3
fi

# Applying necessary patches to the DB
sqlite3 flibusta_new.db < SequenceAuthor.sql

if [ $? -ne 0 ]; then
    echo "sqlite3 patch has failed to apply with error code $?"
    exit 4
fi

# Sanity check
num_books=`sqlite3 flibusta_new.db "select count(1) from libbook"`

if [ $num_books -le 500000 ]; then 
  echo "Too few books, abort, abort!"
else
  echo "New data dump looks legitimate, creating $flibusta_db_path"
  mv flibusta_new.db $flibusta_db_path
  rm lib*.sql
fi