package RaspBerryPiClient;
import java.io.*;
import java.net.*;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


import com.pi4j.io.gpio.GpioController;


public class PiRobot {
	
	static double lwheelOld = 0;
	static double rwheelOld = 0;
	static double lwheel = 0;
	static double rwheel = 0;
	private DatagramSocket serverSocket;
	
	public PiRobot() throws IOException{
		//TCPReceive();
		UDPReceive();
	}
	
	public void UDPReceive() throws IOException{
		System.out.println("udpi says hi!");
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
	        System.out.println("Received: " + clientSentence);
	        //Split this data back to 2 separate wheelvariables 
	        String[] splitArray = clientSentence.split(",",2);
	        lwheelOld = lwheel;
	        rwheelOld = rwheel;
	        lwheel = Double.parseDouble(splitArray[0]);
	        rwheel = Double.parseDouble(splitArray[1]);
			
		}
	}
	
	public void OutToPins(){
		//create gpio controller
		final GpioController gpio = GpioFactory.getInstance();
		//Pin 8 voor UART-RTS op 0 zetten in receive mode te zetten
		final GpioPinDigitalOutput pinrts = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11,"UART0-RTS",PinState.LOW);
		//DataPin initialiseren op 1 (uart0-
		final GpioPinDigitalOutput pindata = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_08,"UART0-TX",PinState.HIGH);
		
	}
	
	public void TCPReceive() throws IOException{
		System.out.println("tcpi says hi!");
		String clientSentence;
		//open Serversocket 
        @SuppressWarnings("resource")
		ServerSocket welcomeSocket = new ServerSocket(6789);
        //Welcome message
        
        //While(true) to keep receiving data from PC
        while(true)
        {  
       //Open connectionSocket to receive data
       Socket connectionSocket = welcomeSocket.accept();
           

           //Put received data in buffer
           BufferedReader inFromClient =
              new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
           //Read data out of buffer           
           clientSentence = inFromClient.readLine();
           //print the received data
           System.out.println("Received: " + clientSentence);
           //Split this data back to 2 separate wheelvariables 
           String[] splitArray = clientSentence.split(",",2);
           lwheelOld = lwheel;
           rwheelOld = rwheel;
           lwheel = Double.parseDouble(splitArray[0]);
           rwheel = Double.parseDouble(splitArray[1]);
        }
	}
	
	
	public double RPMLimiter(double wheel, double wheelOld){
		double speed = 0;
		double maximumAcc = 0.25;
		double maximumDec = 0.10;
	
		
		if(wheel > 0 && wheelOld > 0){				//positieve waarden
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
		}else if(wheel < 0 && wheelOld < 0){		//negatieve waarden
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