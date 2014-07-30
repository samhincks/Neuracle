"""
ldr.py

Display analog data from Arduino using Python (matplotlib)

Author: Mahesh Venkitachalam
Website: electronut.in


Driver for mac:
http://forum.43oh.com/topic/1161-launchpad-osx-usb-drivers-cdc-vcp/page-4 
Direct download:
http://www.mediafire.com/download/y00g7fbz5ifk10s/MSP430LPCDC+1.0.3b.zip

Plug in receiver->usb into computer
Attach transmitter to battery pack
Put black dongle onto battery pack
Plug receiver into USB
Start program
Attach sensor to transmitter

-Figure out what port is being used: ls /dev/tty.* 
-Change PORT = to that address
-then run with command python ldr.py --port=def
"""

import sys
import serial #https://learn.adafruit.com/arduino-lesson-17-email-sending-movement-detector/installing-python-and-pyserial
import argparse
import numpy as np
import pymysql
import glob
from time import sleep
from collections import deque

import matplotlib.pyplot as plt 
import matplotlib.animation as animation

PORT ='/dev/tty.uart-B7FF5D7F0B161D0D'
#PORT = '/dev/tty.uart-79FF427A4D083033'
PORT = glob.glob('/dev/tty.uart-*')[0]
MYSQL = True

X_MIN = 0
X_MAX = 100
Y_MIN = 0
Y_MAX = 256

# plot class
class AnalogPlot:
    # constr
    def __init__(self, strPort, maxLen):
        # open serial port
        self.ser = serial.Serial(strPort, 9600)

        self.ax = deque([0.0]*maxLen)
        self.ay = deque([0.0]*maxLen)
        self.maxLen = maxLen

    # add to buffer
    def addToBuf(self, buf, val):
        if len(buf) < self.maxLen:
            buf.append(val)
        else:
            buf.pop()
            buf.appendleft(val)

    # add data
    def add(self, data):
        assert(len(data) == 2)
        self.addToBuf(self.ax, data[0])
        self.addToBuf(self.ay, data[1])

    # update plot
    def update(self, frameNum, a0, a1):
        try:
            line = self.ser.readline()
            data = []
            print(line)
            if line[0] == '$':
                v1 = line[1:5]
                v2 = line[6:10]

                print "Frame= ", frameNum
                print "Line=  ", line
                data = [float(int(v1)),float(int(v2))]

                if MYSQL:
                    conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', passwd='fnirs196', db='newttt')
                    cur=conn.cursor()   
                             
                    #Insert the data to the Table REALTIME            
                    cur.execute("""INSERT INTO REALTIME1(Channel1,Channel2) VALUES
                      (%s,%s)""",(v1,v2))
                    conn.commit()
                    cur.close()
                    conn.close()


                #data = [float(val) for val in line.split()]
                # print data
            if(len(data) == 2):
                self.add(data)
                a0.set_data(range(self.maxLen), self.ax)
                a1.set_data(range(self.maxLen), self.ay)
        except KeyboardInterrupt:
            print('exiting')

        return a0, 

    # clean up
    def close(self):
        # close serial
        self.ser.flush()
        self.ser.close()    

# main() function
def main():

    # Set up mysql table, drop existing table if it exists
    if MYSQL:
        # Set up mysql database
        conn = pymysql.connect(host='127.0.0.1', port=3306, user='root', passwd='fnirs196', db='newttt')
        cur=conn.cursor() 
        tableName = "REALTIME1"
        cur.execute("DROP TABLE IF EXISTS " + tableName)
        createQuery = "CREATE TABLE " + tableName +" (Channel1 VARCHAR(45), Channel2 VARCHAR(45))"; 
        cur.execute(createQuery)

    # create parser
    parser = argparse.ArgumentParser(description="LDR serial")
    # add expected arguments
    parser.add_argument('--port', dest='port', required=False)

    # parse args
    args = parser.parse_args()

    if args.port == None or args.port == "def":
        print "yay"
        strPort = PORT
    else:
        strPort = args.port


    print('reading from serial port %s...' % strPort)


    s = serial.Serial(strPort, 9600)

    # plot parameters
    analogPlot = AnalogPlot(strPort, 100)

    print('plotting data...')

    # set up animation
    fig = plt.figure()
    ax = plt.axes(xlim=(X_MIN, X_MAX), ylim=(Y_MIN, Y_MAX))
    a0, = ax.plot([], [])
    a1, = ax.plot([], [])
    anim = animation.FuncAnimation(fig, analogPlot.update, 
                                   fargs=(a0, a1), 
                                   interval=1)#,
                                   #blit=True)
                                   #interval=50)

    # show plot
    plt.show()

    # clean up
    analogPlot.close()

    print('exiting, success.')


# call main
if __name__ == '__main__':
        main()
