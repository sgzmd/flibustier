
import sqlite3 as sq3

def check_file_sanity(file_name):
    conn = sq3.connect(file_name)
    try:
      for row in conn.execute("select count(1) from libbook"):
          if row[0] > 10000:
              return True
          else:
              return False
    except sq3.OperationalError as err:
      print(err)
      return False

# print(check_file_sanity("/home/sgzmd/Documents/flibusta_db_2021-11-10.db"))