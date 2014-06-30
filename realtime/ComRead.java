/* This Part is used for reading the data from COM3 Port. It is Java Code and GOT IDEA FROM
 * http://www.java-samples.com/showtutorial.php?tutorialid=11
 * 
 *Install the RXTX library which provide serial and parallel communication. 
 * This code have some problem,The format of the output is chaos. I am not sure the problem is the code or the driver of the IT-EZ430 device.
 * So I wrote a python code. Please check PYCommRT file.
 * 
 * Enhao
 **/

package wireless;

import java.io.*;
import java.util.*;
import gnu.io.*;



public class ComRead implements Runnable, SerialPortEventListener {
	static CommPortIdentifier portId;
	static Enumeration portList;

	InputStream inputStream;
	SerialPort serialPort;
	Thread readThread;

	public static void main(String[] args) {
		portList = CommPortIdentifier.getPortIdentifiers();

		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (portId.getName().equals("COM3")) {
					// if (portId.getName().equals("/dev/term/a"))   If the system is linux, the port name is different
					ComRead reader = new ComRead();
				}
			}
		}
	}

	public ComRead() {
		try {
			serialPort = (SerialPort) portId.open("SimpleReadApp", 2000);
		} catch (PortInUseException e) {
			System.out.println(e);
		}
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println(e);
		}
		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			System.out.println(e);
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			System.out.println(e);
		}
		//readThread = new Thread(this);
		//readThread.start();
	}

	public void run() {
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			System.out.println(e);
		}
	}

	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
	
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[20];
			try {
				while (inputStream.available() > 0) {
					int numBytes = inputStream.read(readBuffer);
					
				}
				System.out.print(new String(readBuffer));
			} catch (IOException e) {
				System.out.println(e);
			}
			break;
		}
	}
}
