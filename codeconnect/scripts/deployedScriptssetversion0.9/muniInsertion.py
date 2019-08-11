#!/usr/bin/env python
# coding: utf-8

# In[8]:


import csv
import psycopg2

CSV_FILE_ENCODING = 'utf-8'

def main():
    global PARID_FILE
    PARID_FILE = 'parcelidlists/parcelandmunidata.csv'
    
    try:
        add_munis()
    except:
        print("Error! Failed to insert munis.")
    
db_conn = None
def get_db_conn():
    global db_conn
    if db_conn is not None:
        return db_conn
    db_conn = psycopg2.connect(
        dbname="csvtest",
        user="sylvia",
        password="c0d3",
        host="localhost"
    )
    return db_conn


def add_munis():
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    
    muni_insert_sql = """INSERT INTO public.muni (muni_id, muni_name) VALUES (%s, %s);"""
    
    munimap = {}
    with open(PARID_FILE) as csvfile:
        reader = csv.reader(csvfile)
        next(reader)
        for row in reader:
            if str(row[1]) not in munimap:
                munimap[str(row[1])] = row[2]
                
    for k,v in munimap.items():
        l = [k, v]
        cursor.execute(muni_insert_sql, tuple(l))
    db_conn.commit()
    cursor.close()
    print("Insert was a success. Fuck yeah. ")
                
            
    
if __name__ == '__main__':
    main()


# In[ ]:




