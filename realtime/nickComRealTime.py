# This python code is use to read the data from COM port. Install pyserial library 
# It is connected to the mysql database and if you want to use it on your computer, 
# change the host id, port id, user name, passwd, and db name
# The data is save in the "REALTIME1" table
# Originally by Enhao, modified by Nick Chen

# This code has been modified to also accept input from a CMS50D+ pulse
# oximeter.  It stores the data in the "REALTIME" table.  The format is listed
# below, with an optional timestamp (removable for doing ML analysis).

#http://www.silabs.com/products/mcu/Pages/USBtoUARTBridgeVCPDrivers.aspx

# Byte 1: ?
# Byte 2: Wave form Y-Axis
# Byte 3: ?
# Byte 4: PRbpm
# Byte 5: SpO2

import sys
if not sys.version_info[0] == 3:
    print("Error: Please use python 3.x.")
    sys.exit(1)

import serial
import pymysql
import datetime

#DEVICE = 'CMS50D'
DEVICE = 'fNIRS'

"""
Time format info
%y: Year
%m: Month
%d: Day of the month
%H: Hour (24H)
%I: Hour (12H)
%M: Minute
%S: Second
%f: Microsecond

%x: MM/DD/(YY)YY
%X: HH:MM:SS
"""

ADDTIMESTAMP = True
TIMEFORMAT = "%X.%f"

if DEVICE == 'CMS50D':
    ser = serial.Serial(
        port='/dev/tty.SLAB_USBtoUART',\
        baudrate=19200,\
        parity=serial.PARITY_NONE,\
        stopbits=serial.STOPBITS_ONE,\
        bytesize=serial.EIGHTBITS,\
            timeout=None)
elif DEVICE == 'fNIRS':
    ser = serial.Serial(
        port='/dev/tty.uart-79FF427A4D083033',\
        baudrate=9600,\
        parity=serial.PARITY_NONE,\
        stopbits=serial.STOPBITS_ONE,\
        bytesize=serial.EIGHTBITS,\
            timeout=0)

def readFromfNIRS():
    count=1
    output = str('')

    while True:
        for line in ser.read():
            cha = chr(line)
            if cha != '$':
                output = output + cha
            else:
                if count == 1:
                    count = count + 1
                else: 
                    print(str(count)+str(':') + output)
                    li = output.split(sep=",", maxsplit=2)
                    
                    #Remove the "$" form the string
                    channel1 = li[0].replace("$","")
                    channel2 = li[1]
                    print(channel1)
                    print(channel2)
                                   
                    
    #Connct to the DB newttt
                    conn = pymysql.connect(host='127.0.0.1', port=3306,
                            user='root', passwd='fnirs196', db='newttt')
                    cur=conn.cursor()   
                             
    #Insert the data to the Table REALTIME            
                    cur.execute("""INSERT INTO REALTIME1(Channel1,Channel2) VALUES
                      (%s,%s)""",(channel1,channel2))
                    conn.commit()
                    cur.close()
                    conn.close()
                    output = str('$')
                    count = count+1
                

def addChunkToDB(host, port, user, pw, db, chunk):
    for i in range(len(chunk)):
        chunk[i] = str(chunk[i])
    conn = pymysql.connect(host=host, port=port, user=user, passwd=pw, db=db)
    cur=conn.cursor()   
                 
    #Insert the data to the Table REALTIME            
    if ADDTIMESTAMP:
        time = datetime.datetime.now().strftime(TIMEFORMAT)
        cur.execute("""INSERT INTO REALTIME(Uk1,YAxis,Uk2,PRbpm,SpO2,Time) VALUES
          (%s,%s,%s,%s,%s,%s)""",(chunk[0],chunk[1],chunk[2],chunk[3],chunk[4],time))
    else:
        cur.execute("""INSERT INTO REALTIME(Uk1,YAxis,Uk2,PRbpm,SpO2) VALUES
          (%s,%s,%s,%s,%s)""",(chunk[0],chunk[1],chunk[2],chunk[3],chunk[4]))
    conn.commit()
    cur.close()
    conn.close()

def readChunk(chunkSize):
    return [ser.read() for i in range(5)]

def printData(data, trans=True):
    # First chunk is sometimes not the right size (??)
    data.pop(0)
    if trans:
        for set in zip(*data):
            print(set)
            print()
    else:
        print(data)

def readFromCMS50D():
    chunkSize = 5
    aligned = False
    
    data = []
    oneChunk = []
    
    i = 0
    while True:
        line = ser.read()
        # The first byte is the only one that is > 127, so align based on that
        if not aligned:
            if line > 127:
                aligned = True
                oneChunk.append(line)
        else:
            oneChunk.append(line) #oneChunk = readChunk(chunkSize)
            i+=1
            if len(oneChunk) == chunkSize: # Chunk reading complete
                print(oneChunk)
                data.append(oneChunk)
                addChunkToDB('127.0.0.1', 3306, 'root', 'fnirs196',
                        'newttt', oneChunk)
                oneChunk = []
                aligned = False

def main():
    print("connected to: " + ser.portstr)

    conn = pymysql.connect(host='127.0.0.1', port=3306,
            user='root', passwd='fnirs196', db='newttt')
    cur=conn.cursor() 
    
    if DEVICE == "CMS50D":
        tableName = "REALTIME"
        cur.execute("DROP TABLE IF EXISTS " + tableName)
        if ADDTIMESTAMP:
            createQuery = "CREATE TABLE " + tableName +\
            " (Uk1 VARCHAR(45), YAxis VARCHAR(45), Uk2 VARCHAR(45), PRbpm VARCHAR(45), SpO2 VARCHAR(45), Time VARCHAR(45))"; 
        else:
            createQuery = "CREATE TABLE " + tableName +\
            " (Uk1 VARCHAR(45), YAxis VARCHAR(45), Uk2 VARCHAR(45), PRbpm VARCHAR(45), SpO2 VARCHAR(45))"; 
        cur.execute(createQuery)
        readFromCMS50D()
    elif DEVICE == "fNIRS":
        tableName = "REALTIME1"
        cur.execute("DROP TABLE IF EXISTS " + tableName)
        createQuery = "CREATE TABLE " + tableName +" (Channel1 VARCHAR(45), Channel2 VARCHAR(45))"; 
        cur.execute(createQuery)
        readFromfNIRS()
    
    ser.close()

if __name__ == "__main__":
    main()
