package ControllerClient;

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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

	//Constructor
	public InputController() throws InterruptedException{
		System.out.println("Telepresence Controller - University of Antwerp");
		input = new Input();
		wheelSpeeds = new WheelSpeeds();
		System.out.println("\nDetected OS: " + getOsName() + ". Loading supported modules...\n");
		if(isWindows()){
			gui = new GUI();
			WinKeyboardListener();
			System.out.println("\n--------------------------------------------------------");
			System.out.println("Oculus Rift for Java Library - By Brad Davis");
			System.out.println("--------------------------------------------------------");
			//setup Oculus Rift Controller
			OculusListener();
			XboxContListener();
		}else{
			LinKeyboardListener();
			XboxContListener();
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

	//Listener for input of Xbox controller
	public void XboxContListener(){
		Controller xController = null;
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();

		//search for gamepad(xbox) controllers.
		for(int i = 0;i<ca.length;i++){
			if(ca[i].getType().toString().equals("Gamepad")){
				xController = ca[i];
				break;
			}
		}	       
		//Get this controllers components (buttons and axis) and print them. FOR TESTING
		Component[] components = xController.getComponents();
		for(int j=0;j<components.length;j++){
			System.out.println("Component "+j+": "+components[j].getName());
		}
		//run the polling function to get changes in controller
		while(true){
			poll(xController);        	
		}
	}

	//this function will continuesly listen for events in the controller
	public void poll(Controller xController){
		xController.poll();
		EventQueue queue = xController.getEventQueue();
		Event event = new Event();
		while(queue.getNextEvent(event)) {			
			//get information about which compontent changes and it's value.
			Component comp = event.getComponent();
			float value = event.getValue(); 
			ControllerComponentAction(comp,value);
		}
		//delay between events
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//this function checks which component has changed and does the appropriate action for that component
	public void ControllerComponentAction(Component comp, float value){
		//Xbox Controller Back button
		if(comp.toString().equals("Button 6")){
			if(value==1.0f){
				if(oculusConnected == true){
					oculus.recenterPose();
					System.out.println("Oculus Recentered");
				}
			}
		}
		//Xbox Controller Start button
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
		//Xbox Controller Triggers
		else if(comp.toString().equals("Z Axis")){
			input.tmag = Math.round((value*-1) * 100.0) / 100.0;
			if(isWindows()){
				gui.tmagLabel.setText("Right Trigger Magnitude: " + input.tmag + "\n");
			}
			updateSpeeds();
		}
		//Xbox Controller Left Shoulder Button
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
		//Xbox Controller Right Shoulder Button
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
		//Xbox Controller Right Analog Joystick
		else if(comp.toString().equals("X Axis")){
			input.jmag = Math.round(value * 100.0) / 100.0;
			if(isWindows()){
				gui.jmagLabel.setText("Left Analog magnitude: " + input.jmag + "\n");
			}
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
				if(oculusEnable == true){
					TrackingState trackingState = oculus.getTrackingState(0);
					OvrVector3f position = trackingState.HeadPose.Pose.Position;
					OvrQuaternionf rotation = trackingState.HeadPose.Pose.Orientation;
					position.x *= 100.0f;
					position.y *= 100.0f;
					position.z *= 100.0f;
					rotation.x *= 100.0f;
					rotation.y *= 100.0f;
					rotation.z *= 100.0f;
					if(isWindows()){
						gui.oculusPosLabel.setText("Oculus Position Output: " + (int)position.x + ", " + (int)position.y + " " + (int)position.z + "\n");
						gui.oculusRotLabel.setText("Oculus Rotation Output: " + (int)rotation.x + ", " + (int)rotation.y + " " + (int)rotation.z + "\n");
					}
					OculusRotConverter(rotation); 
				}
			}
			//error for when oculus is not found or SDK Runtime hasn't been launched
		}catch (IllegalStateException l){
			System.out.println("no Oculus or active Runtime detected!");
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

	//helper method to get index of certain searched object in arraylist
	public int getIndexByname(String pName)
	{
		for(String _item : keyList)
		{
			if(_item.equals(pName))
				return keyList.indexOf(_item);
		}
		return -1;
	}

	//keyboard input listener for Windows
	public void WinKeyboardListener(){

		gui.frame.addKeyListener(new KeyListener(){

			//listens for keys being pressed
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()){
				case KeyEvent.VK_F12:											//recenter Oculus
					if(oculusConnected == true){
						oculus.recenterPose();
						System.out.println("Oculus Recentered");
					}

					break;
				case KeyEvent.VK_F11:		
					//enable or disable Oculus Tracking
					if(oculusConnected == true){
						oculusEnable ^= true;
						System.out.println("Oculus Enabled:" + oculusEnable);
						if(isWindows()){
							gui.oculusEnableTitle.setText("Oculus Enabled: " + oculusEnable + "\n");
						}
					}
					break;
				case KeyEvent.VK_Z:  										//up
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
					break;			
				case KeyEvent.VK_S:											//down
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
					break;
				case KeyEvent.VK_Q:											//left 
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
					break;
				case KeyEvent.VK_D:											//right 
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
					break;
				case KeyEvent.VK_A:											//left (mid rotation)
					if(keyList.contains("A") == false)keyList.add("A");		//if the key is already part of keyList, don't add it again.
					input.midmag = 1;
					input.jmag = -1;
					updateSpeeds();
					break;
				case KeyEvent.VK_E:											//right (mid rotation)
					if(keyList.contains("E") == false)keyList.add("E");		//if the key is already part of keyList, don't add it again.
					input.midmag = 1;
					input.jmag = 1;
					updateSpeeds();
					break;
				}		
			}

			//listens for keys being released.
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()){												
				case KeyEvent.VK_Z:															//up
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
					break;			
				case KeyEvent.VK_S:															//down 
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
					break;	
				case KeyEvent.VK_Q:															//left 
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
					break;	
				case KeyEvent.VK_D:															//right
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
					break;
				case KeyEvent.VK_A:											// left (mid rotation)
					keyList.remove(getIndexByname("A"));					//if key is released, remove it from keylist (uses helper function)
					input.midmag = 0;
					input.jmag = 0;
					updateSpeeds();
					break;
				case KeyEvent.VK_E:											// right (mid rotation)
					keyList.remove(getIndexByname("E"));					//if key is released, remove it from keylist (uses helper function)
					input.midmag = 0;
					input.jmag = 0;
					updateSpeeds();
					break;	
				}		
			}

			//listens for keys being typed (unused)
			public void keyTyped(KeyEvent e) {					
			}        	
		});

	}

	//keyboard input listener for Linux
	public void LinKeyboardListener(){

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
			if(wheelSpeeds.oldLwheel != wheelSpeeds.lwheel || wheelSpeeds.oldRwheel != wheelSpeeds.rwheel){
				UDP udp = new UDP(wheelSpeeds,address,connections);
				try {
					connections = udp.UDPSend();
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
				wheelSpeeds.rwheel = -0.5;
				wheelSpeeds.lwheel = 0.5;
				//Rotating on it's midpoint to the right
			}else if (input.jmag < 0){
				wheelSpeeds.rwheel = 0.5;
				wheelSpeeds.lwheel = -0.5;
			}	
			if(wheelSpeeds.oldLwheel != wheelSpeeds.lwheel || wheelSpeeds.oldRwheel != wheelSpeeds.rwheel){
				UDP udp = new UDP(wheelSpeeds,address,connections);
				try {
					connections = udp.UDPSend();
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




