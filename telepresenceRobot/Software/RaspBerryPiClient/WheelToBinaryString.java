package RaspBerryPiClient;

//class to convert a set of wheel speeds in a string to a binary string used for sending over the UART. 
public class WheelToBinaryString {
	
	public WheelToBinaryString(){
	}
	
	//conversion method from wheel speed string to binary string.
	public static String Conversion(String wheelSpeeds){
		//split string into two floats again
		String[] parts = wheelSpeeds.split(",");
		
		//turn floats into bit streams
		int lwheel = Float.floatToIntBits(Float.parseFloat(parts[0]));
		int rwheel = Float.floatToIntBits(Float.parseFloat(parts[1]));
		
		//turn bitstreams into binary string
		String binaryL = Integer.toBinaryString(lwheel);
		String binaryR = Integer.toBinaryString(rwheel);
		
		//add missing leading zero's
		String formattedL = leadingZeros(binaryL,32);
		String formattedR = leadingZeros(binaryR,32);
		String combined = "1010101" + formattedL + formattedR;				
		return combined;
	}

	//help function to add missing leading zero's.
	public static String leadingZeros(String s, int length) {
	     if (s.length() >= length) return s;
	     else return String.format("%0" + (length-s.length()) + "d%s", 0, s);
	}
	
	   public static void main(String argv[]){
		//test string
		String receivedString = "-0.5,0.5";
	    String display = Conversion(receivedString);
		System.out.println(display);
	}
}
