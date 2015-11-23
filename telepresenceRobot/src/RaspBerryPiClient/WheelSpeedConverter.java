package RaspBerryPiClient;

import java.util.Arrays;

//class to convert a set of wheel speeds in a string containing wheel direction and speed in value between 0 and 255
// to make it easier for the Atmel Microprocessor to read it in. This string can then be send over Serial UART. 
public class WheelSpeedConverter {

	public WheelSpeedConverter(){
	}

	//conversion method from wheel speed.
	public static byte Conversion(String wheelSpeeds){
		//split string into two floats again
		String[] parts = wheelSpeeds.split(",");
		float lwheel = Float.parseFloat(parts[0]);
		float rwheel = Float.parseFloat(parts[1]);

		//Determine direction of wheels
		int wheelDirectionL = 0;
		int wheelDirectionR = 0;
		if(lwheel >= 0)wheelDirectionL = 1;
		else if (lwheel < 0)wheelDirectionL = 0;
		if(rwheel >= 0)wheelDirectionR = 1;
		else if (rwheel < 0)wheelDirectionR = 0;


		//Conversion of number between 0 and 1 to between 0 and 255
		int lwheelC = Math.round((Math.abs(lwheel)) * 7);
		String lbits = Arrays.toString(toBinary(lwheelC)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim();
		int rwheelC = Math.round((Math.abs(rwheel)) * 7);
		String rbits = Arrays.toString(toBinary(rwheelC)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim();
		//combining into one string
		String combined = wheelDirectionL + Integer.toString(wheelDirectionR) + lbits + rbits;	
		byte value = (byte) Integer.parseInt(combined, 2);
		return value;
	}

	public static int[] toBinary(int input){
		int[] bits = new int[3];
		for (int i = 2; i >= 0; i--) {
			if((input & (1 << i)) != 0){
				bits[i] = 1;
			}
			else{
				bits[i] = 0;
			}			
		}
		bits = reverse(bits);
		return bits;
	}
	
	public static int[] reverse(int[] data) {
	    for (int left = 0, right = data.length - 1; left < right; left++, right--) {
	        // swap the values at the left and right indices
	        int temp = data[left];
	        data[left]  = data[right];
	        data[right] = temp;
	    }
	    return data;
	}
	
	public static void main(String argv[]){
		//test string
		String receivedString = "0.81,0.45";
		byte display = Conversion(receivedString);
		System.out.println(display);
	}
}
