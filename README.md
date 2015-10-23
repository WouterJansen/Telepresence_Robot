# TelepresenceRobot
This project for the University of Antwerp, with the help of Eric Paillet concerns creating a robotic embedded system to remote control a motorized wheelchair. 

This project has a few modules:
* **A input controller**: this provides the input to the system. This can be a windows machine with a connected Xbox360 controller or keyboard, or a raspberryPI and Oculus Rift. At the moment we have a client that supports keyboard, Xbox360 controller and Oculus Rift on both Windows and Linux. It should work on other systems as well given the libraries work. All is written in Java. All input is then send over UDP to the next module. 
* **A Embedded controller**: In this case a RaspberryPi, this controller will take care of all necessary conversion towards the wheelchair robotics. It will implement acceleration limitation and convert the wheelspeeds to the right format.
* **A RS-485 Transreceiver**: This will be the Hub between the embedded controller and the Microprocessor controlling the motor of the wheels. It will receive the data from the embedded controller on the RaspberryPi through it's UART and will send it to the next module.
* **The Motor Controller**: In this case a Atmel Microprocessor will receive the wheelspeed data from the serial communication with the RS-485 and use it to control the wheel-motors of the wheelchair.
* **Camera system**: The embedded controller, in this case a raspberry Pi has 2 camera's attached to it. This video stream can be send to the input controller to be viewed on a monitor or through the Oculus Rift.


# Credits
* Students: Wouter Jansen, Mats de Meyer, Ewoud Delabastita, Rens Baeyens, Dries Van Gestel
* University : Eric Paillet
* Libraries:
  * Pi4J: http://pi4j.com/
  * Xbox 360 Controller for Java: http://www.aplu.ch/home/apluhomex.jsp?site=36

