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
 
 
public class EmbeddedController {
       
        static double lwheelOld = 0;
        static double rwheelOld = 0;
        static double lwheel = 0;
        static double rwheel = 0;
        private DatagramSocket serverSocket;
        static int count = 0;
        final Serial serial = SerialFactory.createInstance();
        public long starttime  = 0;
        public long stoptime = 0;
        
        
        //setup function
        public EmbeddedController() throws IOException{
        		System.out.println("Telepresence Controller - University of Antwerp");
                System.out.println("--------------------------------------------------------");
        		//starting video stream
        		System.out.println("Starting VideoStream...");
        		new StartStream().start();
                System.out.println("Starting Serial Communication...");
                // create an instance of the serial communications class
                try {
                	// open the default serial port provided on the GPIO header, default rpi baud rate.
                	serial.open(Serial.DEFAULT_COM_PORT, 115200);
                	System.out.println("Serial Communication started!");
                	System.out.println("--------------------------------------------------------");
               
                }catch(SerialPortException ex) {
                	System.out.println("Serial Communication failed:" + ex.getMessage());
                	System.out.println("--------------------------------------------------------");
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
                        starttime = System.nanoTime();
                        //Store data in string
                        String clientSentence = new String(receivePacket.getData(),0,receivePacket.getLength());
                        //print the received data
                        System.out.println("Received wheelspeeds to send #" + count + ": " + clientSentence);
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
            	stoptime = System.nanoTime();
            	System.out.println("Send wheelspeeds #" + count + "!Byte send: " + WheelSpeedConverter.Conversion(clientByte) + ".Process time: " + (stoptime - starttime) + "ns");
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
         new EmbeddedController();
      }
}