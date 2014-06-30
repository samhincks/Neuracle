1. ComRead.java

 * This is used for reading the data from COM3 Port. It is Java Code and GOT IDEA FROM
 * http://www.java-samples.com/showtutorial.php?tutorialid=11
 * 
 * Install the RXTX library which provide serial and parallel communication. 
 * This code have some problem,The format of the output is chaos.
 * I am not sure the problem is the code or the driver of the IT-EZ430 device.
 * So I wrote a python code. Please check ComRealtime.py
 

2.ComRealtime.py

# This python code is use to read the data from COM port and save them in the database newttt.
# Install pyserial library 
# It is connected to the mysql database and if you want to use it on your computer, 
  change the host id, port id, user name, passwd, and db name
# The data is save in the "REALTIME" table

3.FakeServer.java

 * This code is used as a fake server to send the fake fNIR data to the web server.
 * It uses socket communication.The port number is 8899.  
 * How to use it: Run this program. Then you input opensocket in the console area of the SM website. 
   The data will show in the console area
 
 