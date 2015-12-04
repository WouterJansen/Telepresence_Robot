package InputController;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.OvrQuaternionf;
import com.oculusvr.capi.OvrVector3f;
import com.oculusvr.capi.TrackingState;
import java.io.IOException;
import java.util.ArrayList;

public class InputController {

	//Gui Elements
	public GUI gui;
	//variables
	public Input input;
	public WheelSpeeds wheelSpeeds;
	//Oculus Elements
	public Hmd oculus;
	public boolean oculusConnected = false;
	public boolean oculusEnable = false;
	//RaspberryPi IP-address
	public String address = "192.168.1.201";
	public int connections = 0;
	//list that keeps all current pressed keyboard buttons
	static ArrayList<String> keyList = new ArrayList<String>();
	public static String OS = null;
	public long starttime  = 0;
	public long stoptime = 0;
	public double leftTrigger = 0;
	public double rightTrigger = 0;

	//Constructor
	public InputController() throws InterruptedException{
		System.out.println("Telepresence Controller - University of Antwerp");
		input = new Input();
		wheelSpeeds = new WheelSpeeds();
		System.out.println("--------------------------------------------------------");
		if(isWindows()){
			gui = new GUI();
			System.out.println("Jinput for Java Loading...Detecting Devices...");
			System.out.println("--------------------------------------------------------");
			DeviceListener();
			System.out.println("--------------------------------------------------------");
			System.out.println("Oculus Rift Module Loading... - Library by Brad Davis");
			System.out.println("--------------------------------------------------------");
			Thread t = new Thread() {
				public void run() {
					OculusListener();
				}
			};
			t.start();
		}else{
			System.out.println("Jinput for Java Loading...Detecting Devices...");
			System.out.println("--------------------------------------------------------");
			DeviceListener();
		}
	}    

	//assistance method to get OS name
	public static String getOsName()
	{
		if(OS == null) { OS = System.getProperty("os.name"); }
		return OS;
	}

	//assistance method to check if OS is Windows
	public static boolean isWindows()
	{
		return getOsName().startsWith("Windows");
	}

	//Listener for input of devices
	public void DeviceListener(){
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

		//search for devices and create listeners for them.
		for(int j = 0;j<ca.length;j++){
			final Controller device = ca[j];
			System.out.println("Device #"+j+", name: "+ca[j].getName() + ", type: " + ca[j].getType()+ ", added.");
			Component[] components = ca[j].getComponents();
			for(int k = 0;k<components.length;k++){
				System.out.println("component #" + k + ": " + components[k].getName());
				//run a new polling thread to listen for changes in components for each device.
				Thread t = new Thread() {
					public void run() {
						while(true){
							poll(device);
						}
					}
				};
				t.start();
			}
		}
	}

	//this function will continuesly listen for events in the device
	public void poll(Controller device){
		device.poll();
		EventQueue queue = device.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {			
			//get information about which compontent changes and it's value.
			Component comp = event.getComponent();
			float value = event.getValue();
			if(isWindows()){
				PollActionWindows(comp,value);
			}else{
				PollActionLinux(comp,value);
			}
		}
		//delay between events
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//this function checks which component has changed and does the appropriate action for that component in Windows
	public void PollActionWindows(Component comp, float value){
		starttime = System.nanoTime();
		//Xbox Controller Back button : oculus recenter.
		if(comp.toString().equals("Button 6")){
			if(value==1.0f){
				if(oculusConnected == true){
					oculus.recenterPose();
					System.out.println("Oculus Recentered");
				}
			}
		}
		//Xbox Controller Start button: oculus enable/disable.
		else if(comp.toString().equals("Button 7")){
			if(value==1.0f){
				if(oculusConnected == true){
					oculusEnable ^= true;	
					System.out.println("Oculus Enabled:" + oculusEnable);
					if(isWindows()){
						gui.oculusEnableTitle.setText("Oculus Enabled: " + oculusEnable + "\n");
					}
				}
			}
		}
		//Xbox Controller Triggers: forward/backwards speed.
		else if(comp.toString().equals("Z Axis")){
			input.tmag = Math.round((value*-1) * 100.0) / 100.0;
			if(isWindows()){
				gui.tmagLabel.setText("Right Trigger Magnitude: " + input.tmag + "\n");
			}
			updateSpeeds();
		}
		//Xbox Controller Left Shoulder Button: rotating around midpoint to the left.
		else if(comp.toString().equals("Button 4")){
			if(value==1.0f){
				input.midmag = 1;
				input.jmag = -1;
				if(isWindows()){
					gui.lshoulderLabel.setText("Left Shoulder Pressed: " + 1 + "\n");
				}
				updateSpeeds();
			}
			else if(value==0f){
				input.midmag = 0;
				input.jmag = 0;
				if(isWindows()){
					gui.lshoulderLabel.setText("Left Shoulder Pressed: " + 0 + "\n");
				}
				updateSpeeds();
			}
		}
		//Xbox Controller Right Shoulder Button: rotating around midpoint to the right.
		else if(comp.toString().equals("Button 5")){
			if(value==1.0f){
				input.midmag = 1;
				input.jmag = 1;
				if(isWindows()){
					gui.rshoulderLabel.setText("Right Shoulder Pressed: " + 1 + "\n");
				}
				updateSpeeds();
			}

			else if(value==0f){
				input.midmag = 0;
				input.jmag = 0;
				if(isWindows()){
					gui.rshoulderLabel.setText("Right Shoulder Pressed: " + 0 + "\n");
				}
				updateSpeeds();
			}
		}
		//Xbox Controller Right Analog Joystick: direction input
		else if(comp.toString().equals("X Axis")){
			input.jmag = Math.round(value * 100.0) / 100.0;
			if(isWindows()){
				gui.jmagLabel.setText("Left Analog magnitude: " + input.jmag + "\n");
			}
			updateSpeeds();
		}

		//Keyboard F12: recenter oculus
		else if(comp.toString().equals("F12")){  
			if(value==1.0f){
				if(oculusConnected == true){
					oculus.recenterPose();
					System.out.println("Oculus Recentered");
				}
			}
		}

		//Keyboard F11: enable/disable oculus
		else if(comp.toString().equals("F11")){  
			if(value==1.0f){
				if(oculusConnected == true){
					oculusEnable ^= true;
					System.out.println("Oculus Enabled:" + oculusEnable);
					if(isWindows()){
						gui.oculusEnableTitle.setText("Oculus Enabled: " + oculusEnable + "\n");
					}
				}
			}
		}

		//Keyboard A : rotate on mid point to the left
		else if(comp.toString().equals("A")){  
			System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAA");

			if(value==1.0f){
				if(keyList.contains("A") == false)keyList.add("A");		//if the key is already part of keyList, don't add it again.
				input.midmag = 1;
				input.jmag = -1;
				updateSpeeds();
			}
			else if(value==0.0f){
				keyList.remove(getIndexByname("A"));					//if key is released, remove it from keylist (uses helper function)
				input.midmag = 0;
				input.jmag = 0;
				updateSpeeds();
			}
		}

		//Keyboard E : rotate on mid point to the right
		else if(comp.toString().equals("E")){  
			if(value==1.0f){
				if(keyList.contains("E") == false)keyList.add("E");		//if the key is already part of keyList, don't add it again.
				input.midmag = 1;
				input.jmag = 1;
				updateSpeeds();
			}
			else if(value==0.0f){
				keyList.remove(getIndexByname("E"));					//if key is released, remove it from keylist (uses helper function)
				input.midmag = 0;
				input.jmag = 0;
				updateSpeeds();
			}
		}

		//Keyboard Z : Forwards
		else if(comp.toString().equals("Z")){  
			if(value==1.0f){
				if(keyList.contains("Z") == false)keyList.add("Z");		//if the key is already part of keyList, don't add it again.
				if(keyList.contains("Q")){								//if left is also being pressed, then set direction as well.
					input.jmag = -1;
					input.tmag = 1;
				}else if(keyList.contains("S") == true){				//if both up and down are pressed, nothing happens
					input.tmag = 0;
				}else if(keyList.contains("D")){						//if right is also being pressed, then set direction as well.
					input.jmag = 1;
					input.tmag = 1;
				}else{
					input.jmag = 0;
					input.tmag = 1;
				}
				updateSpeeds();
			}
			else if(value==0.0f){
				keyList.remove(getIndexByname("Z"));									//if key is released, remove it from keylist (uses helper function)
				if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
					input.jmag = 0;
					input.tmag = 0;
					updateSpeeds();
				}else if(keyList.contains("S") == true){								//if down key is pressed when up is released, go backwards.
					input.jmag = 0;
					input.tmag = -1;
					updateSpeeds();
				}
			}
		}

		//Keyboard S : Backwards
		else if(comp.toString().equals("S")){  
			if(value==1.0f){
				if(keyList.contains("S") == false)keyList.add("S");		//if the key is already part of keyList, don't add it again.
				if(keyList.contains("Q")){								//if left is also being pressed, then set direction as well.
					input.jmag = -1;
					input.tmag = -1;
				}else if(keyList.contains("Z") == true){				//if both up and down are pressed, nothing happens.
					input.tmag = 0;
				}else if(keyList.contains("D")){						//if right is also being pressed, then set direction as well.
					input.jmag = 1;
					input.tmag = -1;
				}else{
					input.jmag = 0;
					input.tmag = -1;
				}
				updateSpeeds();
			}
			else if(value==0.0f){
				keyList.remove(getIndexByname("S"));									//if key is released, remove it from keylist (uses helper function)
				if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
					input.jmag = 0;
					input.tmag = 0;
					updateSpeeds();
				}else if(keyList.contains("Z") == true){								//if up key is pressed when down is released, go forwards.
					input.jmag = 0;
					input.tmag = 1;
					updateSpeeds();
				}
			}
		}

		//Keyboard Q : Left
		else if(comp.toString().equals("Q")){  
			if(value==1.0f){
				if(keyList.contains("Q") == false)keyList.add("Q");		//if the key is already part of keyList, don't add it again.
				input.jmag = -1;
				if(keyList.contains("Z")){								//if up is also being pressed, then set this as well.
					input.tmag = 1;
				}else if(keyList.contains("S")){						//if down is also being pressed, then set this as well.
					input.tmag = -1;
				}else{
					input.tmag = 0;
				}
				updateSpeeds();
			}
			else if(value==0.0f){
				keyList.remove(getIndexByname("Q"));									//if key is released, remove it from keylist (uses helper function)
				if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
					input.jmag = 0;
					input.tmag = 0;
					updateSpeeds();
				}else if(keyList.contains("Z") == true){								//if up key is still pressed and left released, go forward.
					input.jmag = 0;
					input.tmag = 1;
					updateSpeeds();
				}else{																	//if down key is still pressed and left released, go backward.
					input.jmag = 0;
					input.tmag = -1;
					updateSpeeds();
				}
			}
		}

		//Keyboard D : Right
		else if(comp.toString().equals("D")){  
			if(value==1.0f){
				if(keyList.contains("D") == false)keyList.add("D");		//if the key is already part of keyList, don't add it again.
				input.jmag = 1;
				if(keyList.contains("Z")){								//if up is also being pressed, then set this as well.
					input.tmag = 1;
				}else if(keyList.contains("S")){						//if down is also being pressed, then set this as well.
					input.tmag = -1;
				}else{
					input.tmag = 0;
				}
				updateSpeeds();
			}
			else if(value==0.0f){
				keyList.remove(getIndexByname("D"));									//if key is released, remove it from keylist (uses helper function)
				if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
					input.jmag = 0;
					input.tmag = 0;
					updateSpeeds();
				}else if(keyList.contains("Z") == true){								//if up key is still pressed and left released, go forward.
					input.jmag = 0;
					input.tmag = 1;
					updateSpeeds();
				}else{																	//if down key is still pressed and left released, go backward.
					input.jmag = 0;
					input.tmag = -1;
					updateSpeeds();
				}
			}
		}
	}

	//helper method to get index of certain searched object in arraylist (for keyboard list)
	public int getIndexByname(String pName)
	{
		for(String _item : keyList)
		{
			if(_item.equals(pName))
				return keyList.indexOf(_item);
		}
		return -1;
	}

	//this function checks which component has changed and does the appropriate action for that component in Linux
	public void PollActionLinux(Component comp, float value){
		starttime = System.nanoTime();
		
		if(comp.toString().equals("W")){
			System.out.println("W (z azerty): " + value);
		}
		else if(comp.toString().equals("A")){
			System.out.println("A (q azerty): " + value);
		}
		else if(comp.toString().equals("S")){
			System.out.println("S (s azerty): " + value);
		}
		else if(comp.toString().equals("D")){
			System.out.println("D (d azerty): " + value);
		}
		else if(comp.toString().equals("Z")){
			System.out.println("Z (z azerty): " + value);
		}
		else if(comp.toString().equals("Q")){
			System.out.println("Q (q azerty): " + value);
		}
		
		//Xbox Controller Left Trigger
		if(comp.toString().equals("rz")){
			double roundedvalue = Math.round((value*-1) * 100.0) / 100.0;
			double triggervalue = (roundedvalue+1)/2;
			leftTrigger = triggervalue;
			
			input.tmag = rightTrigger - leftTrigger;
			updateSpeeds();
		}
		//Xbox Controller Right Trigger
		else if(comp.toString().equals("z")){
			double roundedvalue = Math.round((value*-1) * 100.0) / 100.0;
			double triggervalue = (roundedvalue+1)/2;
			rightTrigger = triggervalue;
			
			input.tmag = rightTrigger - leftTrigger;
			updateSpeeds();
		}
		//Xbox Controller Left Shoulder Button: rotating around midpoint to the left.
		else if(comp.toString().equals("Left Thumb")){
			if(value==1.0f){
				input.midmag = 1;
				input.jmag = -1;
				updateSpeeds();
			}
			else if(value==0f){
				input.midmag = 0;
				input.jmag = 0;
				updateSpeeds();
			}
		}
		//Xbox Controller Right Shoulder Button: rotating around midpoint to the right.
		else if(comp.toString().equals("Right Thumb")){
			if(value==1.0f){
				input.midmag = 1;
				input.jmag = 1;
				updateSpeeds();
			}

			else if(value==0f){
				input.midmag = 0;
				input.jmag = 0;
				updateSpeeds();
			}
		}
		//Xbox Controller Right Analog Joystick: direction input
		else if(comp.toString().equals("x")){
			input.jmag = Math.round(value * 100.0) / 100.0;
			updateSpeeds();
		}
		
		
	}


	//Listener for input of Oculus Positional Tracking
	public void OculusListener() throws IllegalStateException{
		try{
			//initialize the Oculus
			Hmd.initialize();
			oculus = Hmd.create();
			oculusConnected = true;
			//get information about the device
			HmdDesc oculusDesc = oculus.getDesc();            
			System.out.println("Version:" + new String(oculusDesc.ProductName).toString().trim());
			System.out.println("Resolution:" + oculusDesc.Resolution.w + "x" + oculusDesc.Resolution.h);
			System.out.println("RefreshRate:" + oculusDesc.DisplayRefreshRate);
			System.out.println("--------------------------------------------------------");
			oculus.configureTracking();
			oculus.recenterPose();
			//get position information and update GUI
			while(true){
				TrackingState trackingState = oculus.getTrackingState(0);
				OvrVector3f position = trackingState.HeadPose.Pose.Position;
				OvrQuaternionf rotation = trackingState.HeadPose.Pose.Orientation;
				position.x *= 100.0f;
				position.y *= 100.0f;
				position.z *= 100.0f;
				rotation.x *= 100.0f;
				rotation.y *= 100.0f;
				rotation.z *= 100.0f;				
				if(oculusEnable == true){
					if(isWindows()){
						gui.oculusPosLabel.setText("Oculus Position Output: " + (int)position.x + ", " + (int)position.y + " " + (int)position.z + "\n");
						gui.oculusRotLabel.setText("Oculus Rotation Output: " + (int)rotation.x + ", " + (int)rotation.y + " " + (int)rotation.z + "\n");
					}
					OculusRotConverter(rotation); 
				}
			}
			//error for when oculus is not found or SDK Runtime hasn't been launched
		}catch (IllegalStateException l){
			System.out.println("No Oculus Device or active Runtime detected!\n");
		}
	}

	//function to convert the oculus rotation to controller input
	public void OculusRotConverter(OvrQuaternionf rotation){
		if(rotation.y > 15 && rotation.y < 50){  //we have a dead-zone of 30degrees
			input.jmag = rotation.y / 50;     //if the rotation is between 15 and 50, then the magnitude depends on the angle.
		}
		else if (rotation.y > 50 && rotation.y < 99){ //above 50 the magnitude stays maximum
			input.jmag = 1;
		}
		else if(rotation.y < -15 && rotation.y > -50){ // same applies for rotation to the left.
			input.jmag = rotation.y / 50;
		}
		else if(rotation.y < -50 && rotation.y > -99){
			input.jmag = -1;
		}
		else{  // in all other cases the rotation is 0.
			input.jmag = 0;
		}    	
		updateSpeeds();    	
	}

	//Updates the left & right wheel power based on the input
	public void updateSpeeds(){
		//not rotating around midpoint (normal driving)
		if(input.midmag == 0){
			//right
			if(input.jmag > 0){
				wheelSpeeds.lwheel = Math.round((input.tmag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = Math.round(((input.tmag) - (input.tmag)*input.jmag) * 100.0) / 100.0;
				//left
			}else if (input.jmag < 0){
				wheelSpeeds.lwheel = Math.round(((input.tmag) - (input.tmag)*-input.jmag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = Math.round((input.tmag) * 100.0) / 100.0; ;
				//forwards//backwards
			}else{
				wheelSpeeds.lwheel = Math.round((input.tmag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = Math.round((input.tmag) * 100.0) / 100.0;
			}
			
			//ONLY FOR SHORTAGE OF TIME WE HAVE MADE ADDITIONAL CODE here TO DIGITIZE THE WHEELSPEEDS
			//SO IT CAN ONLY BE 1 OR 0 AND NOT VALUES IN BETWEEN.
			//THIS IS DONE TO SOLVE CONFLICTS ON THE MOTORCONTROL SIDE  
			//WHICH CAN AT THE MOMENT NOT DEAL WITH MANY PACKETS. IF THIS PROBLEM IS SOLVED THE FOLLOWING
			//LINES CAN BE REMOVED TO REINTRODUCE ANALOG INPUT			
			if(wheelSpeeds.lwheel > 0){
				wheelSpeeds.lwheel = 1;
			}else if(wheelSpeeds.lwheel < 0){
				wheelSpeeds.lwheel = -1;
			}
			
			if(wheelSpeeds.rwheel > 0){
				wheelSpeeds.rwheel = 1;
			}else if(wheelSpeeds.rwheel < 0){
				wheelSpeeds.rwheel = -1;
			}
			// UNTIL HERE IS THE TEMP. CODE
		
			if(wheelSpeeds.oldLwheel != wheelSpeeds.lwheel || wheelSpeeds.oldRwheel != wheelSpeeds.rwheel){
				UDP udp = new UDP(wheelSpeeds,address,connections);
				try {
					connections = udp.UDPSend();
					stoptime = System.nanoTime();
					System.out.println("process time:" + (stoptime - starttime) + "ns");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			wheelSpeeds.oldLwheel = wheelSpeeds.lwheel;
			wheelSpeeds.oldRwheel = wheelSpeeds.rwheel;

			//rotating around midpoint
		}else if(input.midmag == 1){
			//Rotating on it's midpoint to the left
			if(input.jmag > 0){
				wheelSpeeds.rwheel = -0.7;
				wheelSpeeds.lwheel = 0.7;
				//Rotating on it's midpoint to the right
			}else if (input.jmag < 0){
				wheelSpeeds.rwheel = 0.7;
				wheelSpeeds.lwheel = -0.7;
			}	
			if(wheelSpeeds.oldLwheel != wheelSpeeds.lwheel || wheelSpeeds.oldRwheel != wheelSpeeds.rwheel){
				UDP udp = new UDP(wheelSpeeds,address,connections);
				try {
					connections = udp.UDPSend();
					stoptime = System.nanoTime();
					System.out.println("process time:" + (stoptime - starttime) + "ns");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			wheelSpeeds.oldLwheel = wheelSpeeds.lwheel;
			wheelSpeeds.oldRwheel = wheelSpeeds.rwheel;

			// no acceleration but still using analog
		}else{
			wheelSpeeds.lwheel = 0;
			wheelSpeeds.rwheel = 0;
			if(wheelSpeeds.oldLwheel != wheelSpeeds.lwheel || wheelSpeeds.oldRwheel != wheelSpeeds.rwheel){
				UDP udp = new UDP(wheelSpeeds,address,connections);
				try {
					connections = udp.UDPSend();
					stoptime = System.nanoTime();
					System.out.println("process time:" + (stoptime - starttime) + "ns");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			wheelSpeeds.oldLwheel = wheelSpeeds.lwheel;
			wheelSpeeds.oldRwheel = wheelSpeeds.rwheel;


		}
		SetGUIKeyList();
	}

	//set the GUI keylist
	public void SetGUIKeyList(){

		//builds a string off pressed keys in the keyList array
		StringBuilder builder = new StringBuilder();

		for (String string : keyList) {
			if (builder.length() > 0) {
				builder.append(" ");
			}
			builder.append(string);
		}
		String keyListString = builder.toString();
		//set the GUI labels correctly
		if(isWindows()){
			gui.keyListLabel.setText("Pressed Keys: " + keyListString + "\n");
			gui.lwheelLabel.setText("Left Wheel Power: " + wheelSpeeds.lwheel + "\n");
			gui.rwheelLabel.setText("Right Wheel Power: " + wheelSpeeds.rwheel + "\n");
		}	
	}

	public static void main(String[] args) throws InterruptedException{
		new InputController(); 
	}
}