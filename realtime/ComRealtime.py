# This python code is use to read the data from COM port. Install pyserial library 
# It is connected to the mysql database and if you want to use it on your computer, 
# change the host id, port id, user name, passwd, and db name
# The data is save in the "REALTIME" table
# by Enhao 




import serial
import pymysql

# Read data from Serial port. When you use different COM PORT, Change "port"
ser = serial.Serial(
    port='/dev/tty.uart-79FF427A4D083033',\
    baudrate=9600,\
    parity=serial.PARITY_NONE,\
    stopbits=serial.STOPBITS_ONE,\
    bytesize=serial.EIGHTBITS,\
        timeout=0)

print("connected to: " + ser.portstr)
count=1
output = str('')
tableName = "REALTIME1"

conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', passwd='fnirs196', db='newttt')
cur=conn.cursor() 

cur.execute("DROP TABLE IF EXISTS " + tableName)
createQuery = "CREATE TABLE " + tableName +" (Channel1 VARCHAR(45), Channel2 VARCHAR(45))"; 
cur.execute(createQuery)

         

#Connct to the DB newttt
while True:
    for line in ser.read():
        cha = chr(line)
        print(cha)
        if cha != '$':
            output = output + cha
        else:
            if count == 1:
                count = count + 1
            else: 
                print(str(count)+str(':') + output)
                li = output.split(sep=",", maxsplit=2)
                
                #Remove the "$" form the string
                chanel1 = float(li[0].replace("$",""))
                chanel2 = li[1] #float(li[1].replace("\x00\r\n",""))
                
                conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', passwd='fnirs196', db='newttt')
                cur=conn.cursor() 
         
                #Insert the data to the Table REALTIME 
                #query = "INSERT INTO " + tableName + "(IndexID, Channel1,Channel2) VALUES ( "+count +","+channel1+","+channel2+")"
                #print(query)   
                #cur.execute(query)        
                cur.execute("""INSERT INTO REALTIME1(Channel1,Channel2) VALUES
                  (%s,%s)""",(chanel1,chanel2))
                conn.commit()
                cur.close()
                conn.close()
                output = str('$')
                count = count+1

ser.close()
