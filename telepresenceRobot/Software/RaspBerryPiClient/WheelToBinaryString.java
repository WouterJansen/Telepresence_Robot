package RaspBerryPiClient;

public class WheelToBinaryString {
	
	public String WheelToBinaryString(){
		//teststring
		String receivedString = "-0.5,0.5";
		//split string into two floats again
		String[] parts = receivedString.split(",");
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
		System.out.println(combined);
		
		return combined;
	}

	//help function to add missing leading zero's.
	public String leadingZeros(String s, int length) {
	     if (s.length() >= length) return s;
	     else return String.format("%0" + (length-s.length()) + "d%s", 0, s);
	}
}
