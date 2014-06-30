Required python packages:
matplotlib:
	Web:
		https://downloads.sourceforge.net/project/matplotlib/matplotlib/matplotlib-1.3.1/matplotlib-1.3.1-py2.7-python.org-macosx10.6.dmg
	Command line:
		easy_install -m matplotlib

		git clone git://github.com/matplotlib/matplotlib.git
		cd matplotlib
		python setup.py install
pyserial:
	Web:
		https://pypi.python.org/pypi/pyserial
	Command line: (may require sudo)
		pip install pyserial
		easy_install -U pyserial

Required TI 430 driver: 
http://www.mediafire.com/download/y00g7fbz5ifk10s/MSP430LPCDC+1.0.3b.zip

Instructions for plugging in device:
	Plug in USB
	Plug in end device to battery pack
	Plug in access point to USB

Usage: python ldr.py --port=def
