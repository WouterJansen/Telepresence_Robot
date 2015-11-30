package EmbeddedController;
import java.io.*;
import java.net.*;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

import VideoStream.StartStream;
 
 
public class RobotController {
       
        static double lwheelOld = 0;
        static double rwheelOld = 0;
        static double lwheel = 0;
        static double rwheel = 0;
        private DatagramSocket serverSocket;
        static int count = 0;
        final Serial serial = SerialFactory.createInstance();
        
        
        //setup function
        public RobotController() throws IOException{
        		System.out.println("Telepresence Controller - University of Antwerp");
        		System.out.println("\n Loading modules...\n");
        		//starting video stream
        		System.out.println("\nStarting VideoStream...");

        		new StartStream().start();
        		System.out.println("VideoStream started!");
                System.out.println("\nStarting Serial Communication...");
                // create an instance of the serial communications class
                try {
                	// open the default serial port provided on the GPIO header, default rpi baud rate.
                	serial.open(Serial.DEFAULT_COM_PORT, 115200);
                	System.out.println("Serial Communication success!");
                	System.out.println("\n--------------------------------------------------------");
               
                }catch(SerialPortException ex) {
                	System.out.println("Serial Communication failed:" + ex.getMessage());
                	System.out.println("\n--------------------------------------------------------");
                	return;
                }
                OutToPins();
                UDPReceive();
        }
       
        //method to receive the UDP packet from the InputController
        public void UDPReceive() throws IOException{
                serverSocket = new DatagramSocket(9876);
                //Create array to story received data
                byte[] receiveData = new byte[1024];
                while(true)
                {
                        //Create datagrampacket to receive data
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        //receive datapacket
                        serverSocket.receive(receivePacket);
                        //Store data in string
                        String clientSentence = new String(receivePacket.getData(),0,receivePacket.getLength());
                        //print the received data
                        System.out.println("Received data #" + count + ": " + clientSentence);
                        //send data over Uart.
                        UartSend(clientSentence);
                        count = count + 1;
                }       
        }
        
        //Send the byte over UART via the serial communication.
        public void UartSend(String clientByte){
        	try {
            	//send the data but first needs to be converted to right format.
            	serial.write((byte) WheelSpeedConverter.Conversion(clientByte));
            	System.out.println("Send byte #" + count + ": " + clientByte);
            }catch(IllegalStateException ex){
            	ex.printStackTrace();                    
            } 
        }
       
        //method to set a pin to LOW/HIGH.
        public void OutToPins(){
                //create gpio controller
                final GpioController gpio = GpioFactory.getInstance();
                //Pin to set UART-RTS 0 => receive mode. This is GPIO_17 but the library knows it as GPIO_11. It is set to HIGH.
                gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00,"UART0-RTS",PinState.HIGH);         
        }       
       
   public static void main(String argv[]) throws Exception
      {
         new RobotController();
      }
}