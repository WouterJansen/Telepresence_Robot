import java.io.*;
import java.net.*;

public class PiRobot {
	
	static double lwheelOld = 0;
	static double rwheelOld = 0;
	static double lwheel = 0;
	static double rwheel = 0;
	
	public void TCPListener() throws IOException{
		
		String clientSentence;
        ServerSocket welcomeSocket = new ServerSocket(6789);
        System.out.println("Pi says Hi!\n");
        while(true)
        {
           Socket connectionSocket = welcomeSocket.accept();
           BufferedReader inFromClient =
              new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
           clientSentence = inFromClient.readLine();
           System.out.println("Received: " + clientSentence);
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
		
		double acc = 0;
		
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
         
      }
}