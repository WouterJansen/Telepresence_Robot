package RaspBerryPiClient;
import java.io.*;
import java.net.*;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;
 
 
public class PiRobot {
       
        static double lwheelOld = 0;
        static double rwheelOld = 0;
        static double lwheel = 0;
        static double rwheel = 0;
        private DatagramSocket serverSocket;
        static int count = 0;
        final Serial serial = SerialFactory.createInstance();
        
        public PiRobot() throws IOException{
                System.out.println("Starting serial communication.");
                // create an instance of the serial communications class
                try {
                	// open the default serial port provided on the GPIO header, default rpi baud rate.
                	serial.open(Serial.DEFAULT_COM_PORT, 115200);
                	System.out.println("Serial communication success!");
               
                }catch(SerialPortException ex) {
                	System.out.println("Serial communication failed:" + ex.getMessage());
                	return;
                }
                OutToPins();
                UDPReceive();
        }
       
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
                        System.out.println("Received " + count + ": " + clientSentence);
                        //send data over Uart.
                        UartSend(clientSentence);
                        count = count + 1;
                }       
        }
        
        public void UartSend(String clientSentence){
        	try {
            	//send the data but first needs to be converted to right format.
            	serial.write(WheelSpeedConverter.Conversion(clientSentence));
            }catch(IllegalStateException ex){
            	ex.printStackTrace();                    
            } 
        }
       
        public void OutToPins(){
                //create gpio controller
                final GpioController gpio = GpioFactory.getInstance();
                //Pin 8 to set UART-RTS 0 => receive mode
                gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11,"UART0-RTS",PinState.LOW);         
        }
       
       
        public double RPMLimiter(double wheel, double wheelOld){
                double speed = 0;
                double maximumAcc = 0.25;
                double maximumDec = 0.10;
       
               
                if(wheel > 0 && wheelOld > 0){                          //positieve waarden
                        if(wheel - wheelOld > 0){
                                if(wheel - wheelOld >= maximumAcc){
                                        speed = maximumAcc;                                    
                                }else{speed = wheel;}
                        }else if(wheel - wheelOld < 0){
                                if(-(wheel - wheelOld) >= maximumDec){
                                        speed = maximumDec;                                    
                                }else{speed = wheel;}
                        }else if(wheel == wheelOld){
                                speed = wheel;
                        }
                }else if(wheel < 0 && wheelOld < 0){            //negatieve waarden
                        if(wheel - wheelOld > 0){
                                if(wheel - wheelOld >= maximumAcc){
                                        speed = maximumAcc;                                    
                                }else{speed = wheel;}
                        }else if(wheel - wheelOld < 0){
                                if(-(wheel - wheelOld) >= maximumDec){
                                        speed = maximumDec;                                    
                                }else{speed = wheel;}
                        }else if(wheel == wheelOld){
                                speed = wheel;
                        }
                }else if((wheel > 0 && wheelOld < 0) || (wheel < 0 && wheelOld > 0)){
                       
                }
               
               
                return speed;
        }
       
   public static void main(String argv[]) throws Exception
      {
         new PiRobot();
      }
}