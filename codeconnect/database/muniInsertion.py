#!/usr/bin/env python
# coding: utf-8

# In[8]:


import csv
import psycopg2
import traceback

CSV_FILE_ENCODING = 'utf-8'

def main():
    global PARID_FILE
    PARID_FILE = 'muni_list.csv'
    
    try:
        add_munis()
    except:
        traceback.print_exc()
        print("Error! Failed to insert munis.")
    
db_conn = None
def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        dbname="cogdb",
        user="sylvia",
        password="c0d3",
        host="localhost"
    )
    return db_conn


def add_munis():
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    
    muni_insert_sql = """INSERT INTO public.municipality (municode, muniname, defaultcodeset, occpermitissuingsource_sourceid) VALUES (%(municode)s, %(muniname)s, 17, 10) ON CONFLICT DO NOTHING;"""
    
    munimap = {}
    with open(PARID_FILE) as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            print(row)
            try:
                cursor.execute(muni_insert_sql, row)
                db_conn.commit()
            except:
                print("error.")
                traceback.print_exc()
                

    cursor.close()
    print("Insert was a success!")
                

if __name__ == '__main__':
    main()


# In[ ]:




