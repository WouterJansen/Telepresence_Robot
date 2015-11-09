package RaspBerryPiClient;

//class to convert a set of wheel speeds in a string containing wheel direction and speed in value between 0 and 255
// to make it easier for the Atmel Microprocessor to read it in. This string can then be send over Serial UART. 
public class WheelSpeedConverter {
	
	public WheelSpeedConverter(){
	}
	
	//conversion method from wheel speed.
	public static String Conversion(String wheelSpeeds){
		//split string into two floats again
		String[] parts = wheelSpeeds.split(",");
		float lwheel = Float.parseFloat(parts[0]);
		float rwheel = Float.parseFloat(parts[1]);
		
		//Determine direction of wheels
		int wheelDirectionL = 0;
		int wheelDirectionR = 0;
		if(lwheel >= 0)wheelDirectionL = 1;
		else if (lwheel < 0)wheelDirectionL = -1;
		if(rwheel >= 0)wheelDirectionR = 1;
		else if (rwheel < 0)wheelDirectionR = -1;
		
		
		//Conversion of number between 0 and 1 to between 0 and 255
		int lwheelC = Math.round((Math.abs(lwheel)) * 255);
		int rwheelC = Math.round((Math.abs(rwheel)) * 255);
		
		//combining into one string
		String combined = wheelDirectionL + " " + wheelDirectionR + " " + lwheelC + " " + rwheelC;				
		return combined;
	}
	
	   public static void main(String argv[]){
		//test string
		String receivedString = "0.81,0.45";
	    String display = Conversion(receivedString);
		System.out.println(display);
	}
}
