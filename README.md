# TelepresenceRobot
This project for the University of Antwerp, with the help of Eric Paillet concerns creating a robotic embedded system to remote control a motorized wheelchair. 

This project has a few modules:
* **A input controller**: this provides the input to the system.  At the moment we have a client that supports keyboard, Xbox360 controller and Oculus Rift(positional tracking for rotation) on both Windows and Linux. It should work on other systems as well given the libraries work. All is written in Java. All input is then converted to 2 wheelspeeds for the wheelchair and send over UDP to the next module. 
* **A Embedded controller**: In this case a RaspberryPi, this controller will take care of all necessary conversion towards the wheelchair robotics. It will implement acceleration limitation and convert the wheelspeeds to the right format. This is fully written in java. 
* **A RS-485 Transreceiver**: This will be the Hub between the embedded controller and the Microprocessor controlling the motor of the wheels. It will receive the data from the embedded controller on the RaspberryPi through it's UART and will send it to the next module.
* **The Motor Controller**: In this case a Atmel Microprocessor will receive the wheelspeed data from the serial communication with the RS-485 and use it to control the wheel-motors of the wheelchair.
* **Camera system**: The embedded controller, in this case a raspberry Pi has 2 camera's attached to it. This video stream can be send to the input controller to be viewed on a monitor or through the Oculus Rift.

# What can be find in this Git?
This git has the source code for the 3 controllers. Input Controller(ControllerClient Package),the Embedded Controller(RaspBerryPiClient) and Motor Controller. The camera system has parts in both the input controller and the embedded controller. 


# Credits
* Students: Wouter Jansen, Mats de Meyer, Ewoud Delabastita, Rens Baeyens, Dries Van Gestel
* University : Eric Paillet
* Libraries:
  * Pi4J: http://pi4j.com/
  * Xbox 360 Controller for Java: http://www.aplu.ch/home/apluhomex.jsp?site=36

