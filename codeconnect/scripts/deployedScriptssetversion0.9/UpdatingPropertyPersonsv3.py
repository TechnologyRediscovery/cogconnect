#!/usr/bin/env python
# coding: utf-8

# In[8]:
import csv
import random
import re
import time
import psycopg2
import itertools
# import csv_utils
import requests
import bs4
from unidecode import unidecode

CSV_FILE_ENCODING = 'utf-8'
# global municodemap
# global current_muni
# global MUNI_PARCELID_LIST_FILE
# global muni_idbase_map
# global ID_BASE
# global TEST_PARID_FILE

def main():
    # for accessing a list of all parcelIDs by with one muni's list per file
    # global INPUT_FILE
    # INPUT_FILE = 'workspace/pitcairnparids.csv'

    # parcels whose base property data is not written are written here before moving on
    global LOG_FILE
    LOG_FILE = 'output/swissvaleerror.csv'

    # this log file is used for storing parcels whose person and propertyperson inserts fail
    global LOG_FILE_AUX
    LOG_FILE_AUX = 'output/swissvaleerror_aux.csv'

    # ID number of the system user connected to these original inserts
    # user 99 is sylvia, our COG robot
    global UPDATING_USER_ID
    UPDATING_USER_ID = 99

    global PARID_FILE
    # PARID_FILE = 'parcelidlists/wilmerdingparids.csv'
    # PARID_FILE = 'parcelidlists/parcelidstest.csv'
    #PARID_FILE = 'parcelidlists/pitcairnparids_correct.csv'
    #PARID_FILE = 'parcelidlists/eastmckeesportparids.csv'
    #PARID_FILE = 'parcelidlists/wilkinsparcelids.csv'
    #PARID_FILE = 'parcelidlists/chalfantparcelids.csv'
    PARID_FILE = 'parcelidlists/swissvaleparcelids.csv'
    
    # used as the access key for muni codes and ID bases in the dictionaries below
    global current_muni
    current_muni = 'swissvale'

    # use as floor value for all new propertyIDs
    global PROP_ID_BASE
    PROP_ID_BASE = 100000

    # floor value for new personIDs
    global PERSON_ID_BASE
    PERSON_ID_BASE = 10000

    # used for sliding starting ID up to accommodate a parcelid list adjustment due to errors on pesky parcels
    # must be manually set if there's an error: start one above the most recently issued ID
    # this gets added to the base ID in the ID range generation methods
    global BUMP_UP
    BUMP_UP = 0

    global municodemap
    municodemap = {'chalfant':814,'churchhill':816, 'eastmckeesport':821, 'pitcairn':847, 'wilmerding':867, 'wilkins':953, 'cogland':999, 'swissvale':111}

    # add these base amounts to the universal base to get starting IDs
    global muni_idbase_map
    muni_idbase_map = {'chalfant':10000,'churchhill':20000, 'eastmckeesport':30000, 'pitcairn':40000, 'wilmerding':0, 'wilkins':50000, 'cogland':60000, 'swissvale':700000}

    # add these base amounts to the universal base to get starting IDs
    global person_idbase_map
    person_idbase_map = {'chalfant':10000,'churchhill':20000, 'eastmckeesport':30000, 'pitcairn':40000, 'wilmerding':0, 'wilkins':50000, 'cogland':60000, 'swissvale':700000}

    # jump into the actual work here
    #insert_property_basetableinfo()
    update_persons()
    
    # Add owner data to properties
    # properties_with_owner_info = add_prop_info(properties)
    # Save new CSV file with property plus owner data
    # OUTPUT_FILE = 'output/testPropOut.txt'
    # save_properties_as_csv(properties_with_owner_info, OUTPUT_FILE)

    

def get_nextpersonid(munioffset):
    for i in list(range(PERSON_ID_BASE + munioffset + BUMP_UP, PERSON_ID_BASE + munioffset + 9999)):
        yield i;

        
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

county_info_cache = {}
def get_county_page_for(parcel_id):
    if parcel_id in county_info_cache:
        return county_info_cache[parcel_id]
    COUNTY_REAL_ESTATE_URL = ('http://www2.county.allegheny.pa.us/'
                              'RealEstate/GeneralInfo.aspx?')
    search_parameters = {
        'ParcelID': parcel_id,
        'SearchType': 3,
        'SearchParcel': parcel_id}
    waittime = random.uniform(0.0,1.0)
    print("waiting:" + str(waittime))
    time.sleep(waittime)
    try:
        response = requests.get(
                COUNTY_REAL_ESTATE_URL,
                params=search_parameters,
                timeout=5)
        print('Scraping data from county: %s' + parcel_id)
    except requests.exceptions.Timeout:
        # Wait 10 secs and try one more time
        time.sleep(10)
        response = requests.get(
                COUNTY_REAL_ESTATE_URL,
                params=search_parameters,
                timeout=5)
    county_info_cache[parcel_id] = response.text
    return response.text

def extract_owneraddress(property_html):
    print('--------- extracting owner address ------------')
    owneraddrmap = {}
    # print(property_html)
    soup = bs4.BeautifulSoup(property_html, 'lxml')
    # this yeilds something like
    # 471&nbsp;WALNUT  ST<br>PITTSBURGH,&nbsp;PA&nbsp;15238
    scrapedhtml = soup.select("#lblChangeMail")
    owneraddrmap['notes_adrdump'] = str(scrapedhtml)

    print("Scraped:" + str(scrapedhtml))
    soup = bs4.BeautifulSoup(str(scrapedhtml), 'lxml')
    # this spits out a three-item list. We throw away the <br> which is 
    # the middle item. The first is the street, the second gets chopped up
    adrlistraw = soup.span.contents 
    # make sure we have all the parts of the address
    if len(adrlistraw) < 3:
        print("Raising Exception")
        raise Exception
    
    addrlistindex = 0
    if len(adrlistraw) > 3:
        print("Extra line address")
        addrlistindex += 2
        
    ownerstreet = re.sub('  ', ' ', adrlistraw[addrlistindex])
    owneraddrmap['street'] = str(unidecode(ownerstreet)).strip()
    print(owneraddrmap['street'])
    # on the city, state, zip line, grab until the comma before the state
    exp = re.compile('[^,]*')
    addrlistindex += 2
    owneraddrmap['city'] = exp.search(adrlistraw[addrlistindex]).group()
    print("city:" + owneraddrmap['city'])
    # grap with string indexes (fragile if there is more than one space before zip)
    # TODO: use regexp
    exp=re.compile(r',\s*(\w\w)')
    m = re.search(exp,adrlistraw[addrlistindex])
    owneraddrmap['state'] = str(m.group(1))
    
    # abandoned string slicing approach (too brittle; use regexp)
    # owneraddrmap['state']= adrlistraw[2][-13:-11]
    print("state:" + owneraddrmap['state'])
    
    # owner zips could come in as: 15218 OR 15218- OR 15218-2342
    # just lose the routing numbers and take the first digits until the -
    exp=re.compile(r'\d+')
    m = re.search(exp, adrlistraw[addrlistindex])
    owneraddrmap['zipc'] = str(m.group())

    # another abandoned string slicing approach: 
    # also too brittle given range of scraped inputs
    # owneraddrmap['zip'] = adrlistraw[2][-10:]
    print("zip:" + owneraddrmap['zipc'])

    return owneraddrmap


def connect_person_to_property(propertyid, personid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    insert_sql = """
        INSERT INTO public.propertyperson(
            property_propertyid, person_personid)
    VALUES (%(prop)s, %(pers)s);
    """
    insertmap = {}
    
    # load up vars for use in SQL from each of the parse methods
    insertmap['prop'] = propertyid
    insertmap['pers'] = personid

    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print('----- connected person owner to property -----')
    

def extract_owner_name(property_html):
    OWNER_NAME_SPAN_ID = 'BasicInfo1_lblOwner'
    persondict = {}

    soup = bs4.BeautifulSoup(property_html, 'lxml')
    owner_full_name = soup.find('span', id=OWNER_NAME_SPAN_ID).text
    print('owner_raw_name:' + str(owner_full_name))
    # Remove extra spaces from owner's name
    full_owners = re.sub(r'\s+', ' ', owner_full_name.strip())
    return full_owners


def extract_and_insert_person(rawhtml, ownername, propinserts):
    # fixed values specific to keys in lookup tables
    

    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    notemsg = """In case of confusion, check autmated record entry with raw text from the county database:"""

    insert_sql = """
        INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18, humanverifiedby,compositelname, mailing_address_street, mailing_address_city,
            mailing_address_state, mailing_address_zip, useseparatemailingaddr, mailing_address_thirdline)
    VALUES (DEFAULT, cast ( 'ownercntylookup' as persontype), 
            %(muni_municode)s, NULL, %(lname)s, 'Property Owner', 
            NULL, NULL, NULL, NULL, %(address_street)s, %(address_city)s, 
            %(address_state)s, %(address_zip)s, %(notes)s, now(), NULL, TRUE, 
            FALSE, NULL, true, %(mailing_street)s, %(mailing_city)s, %(mailing_state)s, %(mailing_zip)s,
            %(mailingsameasres)s, %(thirdline)s);
    """
    

    
    insertmap = {}
    
    # load up vars for use in SQL from each of the parse methods
    insertmap['muni_municode'] = str(municodemap[current_muni])
    
    insertmap['mailing_street'] = None
    insertmap['mailing_city'] = None
    insertmap['mailing_state'] = None
    insertmap['mailing_zip'] = None
    insertmap['thirdline'] = None
    insertmap['mailingsameasres'] = 'false'
    insertmap['lname'] = ownername
    
    try:
        owneraddrmap = extract_owneraddress(rawhtml)
        print("ARE THEY THE SAME??? " + str(owneraddrmap['street'] == propinserts['street']) + owneraddrmap['street'] + propinserts['street'])
        print(propinserts)
        if owneraddrmap['street'] == propinserts['street']:
            print("SAME ADDRESS")
            insertmap['address_street'] = owneraddrmap['street']
            insertmap['address_city'] = owneraddrmap['city']
            insertmap['address_state'] = owneraddrmap['state']
            insertmap['address_zip'] = owneraddrmap['zipc']
            insertmap['notes'] = notemsg + ownername + " Raw Address: " + owneraddrmap['notes_adrdump']
        else:
            print("DIFFERENT MAILING")
            insertmap['address_street'] = None
            insertmap['address_city'] = None
            insertmap['address_state'] = None
            insertmap['address_zip'] = None
            insertmap['mailing_street'] = owneraddrmap['street']
            insertmap['mailing_city'] = owneraddrmap['city']
            insertmap['mailing_state'] = owneraddrmap['state']
            insertmap['mailing_zip'] = owneraddrmap['zipc']
            insertmap['thirdline'] = owneraddrmap['thirdline']
            insertmap['mailingsameasres'] = 'true'
            insertmap['notes'] = notemsg + ownername + " Raw address: " + owneraddrmap['notes_adrdump']
            print(insertmap['thirdline'])
            print(insertmap['notes'])

    except Exception:
        print("NO MAILING")
        insertmap['address_street'] = propinserts['street']
        insertmap['address_city'] = propinserts['city']
        insertmap['address_state'] = propinserts['state']
        insertmap['address_zip'] = propinserts['zipc']
        insertmap['notes'] = "Owner does not have mailing address"

        insertmap['notes'] = "Owner does not have mailing address"

        
    print(insertmap['mailing_zip'])
    if insertmap['address_street']:
        print('extracted owner address:'             + ' ' + insertmap['address_street']             + ' ' + insertmap['address_city']             + ', ' + insertmap['address_state']             + ' ' + insertmap['address_zip'])
    if insertmap['mailing_street']:
        print('extracted mailing address:'             + ' ' + insertmap['mailing_street']             + ' ' + insertmap['mailing_city']             + ', ' + insertmap['mailing_state']             + ' ' + insertmap['mailing_zip'])

    
    print('Inserting person data for id: %s' % (insertmap['personid']))
    print('person notes:' + insertmap['notes'])
    
    cursor.execute(insert_sql, insertmap)
    db_conn.commit()
    print('----- committed person owner -----')
    # commit core propertytable insert
    
    

def check_current_owner(parcelid):
    namemap = {}
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    inserts = {}
    inserts['parid'] = parcelid
    get_names = """SELECT person.lname, person.personid FROM ((public.person INNER JOIN public.propertyperson
    ON person.personid=propertyperson.person_personid) INNER JOIN public.property 
    ON property.propertyid=propertyperson.property_propertyid) WHERE property.parid=%(parid)s AND person.isactive=true;"""
    cursor.execute(get_names, inserts)
    names = cursor.fetchall()
    print(names)
    namemap = {}
    namemap['lname'] = names[0][0]
    namemap['personid'] = names[0][1]
    print(namemap)
    return namemap

def update_isactive(personid):
    update_sql = """
    UPDATE public.person
    SET isactive = false 
    WHERE personid = %s"""
    
    db = get_db_conn()
    cursor = db.cursor()
    cursor.execute(update_sql, (personid, ))
    cursor.close()
    print("----Old person set to inactive----")


def create_ce_event(caseid, eventcat):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    insertmap = {}
    insertsql = """INSERT INTO public.ceevent(
    eventid, ceeventcategory_catid, cecase_caseid, dateofrecord, eventtimestamp,
    eventdescription, owner_userid, disclosetomunicipality, disclosetopublic, activeevent,
    requiresviewconfirmation, hidden, notes, responsetimestamp, actionrequestedby_userid,
    respondernotes, responderintended_userid, requestedeventcat_catid, requestedevent_eventid,
    rejeecteventrequest, responderactual_userid, responseevent_eventid, directrequesttodefaultmuniceo)
    VALUES (DEFAULT, %(eventcat)s, %(caseid)s, now(), now(), 'Update event from county', 99, TRUE, FALSE, FALSE, FALSE, FALSE, NULL,
    now(), NULL, NULL, NULL, NULL, NULL, FALSE, NULL, NULL, FALSE);"""
    insertmap['caseid'] = caseid
    insertmap['eventcat'] = eventcat
    cursor.execute(insertsql, insertmap)
    db_conn.commit()


def create_cecase(propid, caseid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()
    #selectPropCase = """SELECT caseid FROM public.cecase WHERE """


    insertsql = """INSERT INTO public.cecase(
    caseid, cecasepubliccc, property_propertyid, propertyunit_unitid,
    login_userid, casename, casephase, originationdate, closingdate,
    creationtimestamp, notes, paccenabled, allowuplinkaccess, propertyinfocase)
    VALUES (%(caseid)s, 111111, %(propid)s, NULL, %(updater)s, %(casename)s, cast ('CountySiteImport' as casephase), now(),
    now(), now(), %(notes)s, FALSE, NULL, NULL);"""

    imap = {}
    imap['caseid'] = caseid
    imap['propid'] = propid
    imap['casename'] = "Import from county site"
    imap['notes'] = "Initial case for each property"
    imap['updater'] = UPDATING_USER_ID
    cursor.execute(insertsql, imap)
    db_conn.commit()

def get_caseid(propid):
    db_conn = get_db_conn()
    cursor = db_conn.cursor()

    caseidsql = """SELECT caseid FROM public.cecase WHERE property_propertyid=%(prop)s AND propertyinfocase=TRUE"""
    cursor.execute(caseidsql, dict(prop=propid))
    props = cursor.fetchall()
    print(props)
    return props[0]

    
def update_persons():
    db = get_db_conn()
    municode = str(municodemap[current_muni])

    props1 = "SELECT propertyid, parid, address, addr_city, addr_state, addr_zip FROM public.property WHERE municipality_municode=%s ORDER BY propertyid;"
    propperson = "SELECT person_personid FROM public.propertyperson WHERE property_propertyid = %s;"

    props = """SELECT person.personid, person.lname, person.address_street, person.address_city, person.address_state,
        person.address_zip, person.mailing_address_street, person.useseparatemailingaddr, property.propertyid, person.notes,
        property.parid, person.mailing_address_city, person.mailing_address_state, person.mailing_address_zip, cecase.caseid
        FROM ((public.person INNER JOIN public.propertyperson
            ON person.personid=propertyperson.person_personid) INNER JOIN public.property 
            ON property.propertyid=propertyperson.property_propertyid) INNER JOIN public.cecase
            ON property.propertyid=cecase.property_propertyid WHERE person.isactive=true 
            AND person.muni_municode = %(muni)s;"""

    maps = {}
    maps['muni'] = municode
    cursor = db.cursor()
    cursor.execute(props, maps)
    propidlist = cursor.fetchall()


    print(municode)

    for p in propidlist:
        print(p)
        propinserts = {}
        propinserts['street'] = p[2]
        propinserts['city'] = p[3]
        propinserts['state'] = p[4]
        propinserts['zip'] = p[5]
        propinserts['mailing_street'] = p[6]
        propinserts['mailing_city'] = p[11]
        propinserts['mailing_state'] = p[12]
        propinserts['mailing_zip'] = p[13]
        propinserts['uses_separate'] = p[7]

        print(propinserts)
        parid = p[10]
        propid = p[8]
        personid = p[0]
        personname = p[1]
        personnotes = p[9]
        print(p)
        print(repr(p[2]))
        print(parid)
        print(repr(parid))
        db_owner_map = check_current_owner(parid)
        db_owner_name = p[1]
        db_owner_personid = p[0]
        print(personid)
        rawhtml = get_county_page_for(parid)
        county_owner_name = extract_owner_name(rawhtml)
        print("db: %s **** county: %s" % (db_owner_name, county_owner_name))
        print(db_owner_name == county_owner_name)
        try:
            caseid = get_caseid(propid)
        except:
            print("ERROR!!! Couldn't get caseid for prop")
        if(db_owner_name != county_owner_name):
            try:
                extract_and_insert_person(rawhtml, county_owner_name, propinserts)
                for person in personid:
                    update_isactive(person[0])
            except Exception:
                print("ERROR extracting and inserting new person\n")
                continue
            try:
                connect_person_to_property(propid, firstavailablepersonid)
            except Exception:
                print("ERROR connecting person to property")
                continue
            try:
                create_ce_event(caseid, 212)
            except:
                print("ERROR creating ceevent for updated person") 
            firstavailablepersonid += 1
            
        else:
            try:
                create_ce_event(caseid, 213)
            except:
                print("ERROR creating ceevent for updated person") 
            print("-----OWNER IS THE SAME...CONTINUING-----")

        """try:
            owneraddrmap = extract_owneraddress(rawhtml)
            if(propinserts['uses_separate'] == 'false' and propinserts[ 'mailing_street'] != owneraddrmap['street']):
                personnotes += format("FORMER MAILING ADDRESS: %s %s, %s %s", (propinserts['mailing_street'], propinserts['mailing_city'], propinserts['mailing_state'], propinserts['mailing_zip']))
                

                sql = 'UPDATE person.'

        except:
            continue
        #extract_and_insert_person(rawhtml, ownername, personid, propinserts)"""

if __name__ == '__main__':
    main()

