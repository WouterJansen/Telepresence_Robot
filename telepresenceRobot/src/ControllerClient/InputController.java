package ControllerClient;

import ch.aplu.xboxcontroller.*;
import javax.swing.*;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.OvrQuaternionf;
import com.oculusvr.capi.OvrVector3f;
import com.oculusvr.capi.TrackingState;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class InputController {

	//Gui Elements
	public GUI gui;
	//variables
	public Input input;
	public WheelSpeeds wheelSpeeds;
	//Xbox Controller Elements
	public XboxController xc;
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
			System.out.println("--------------------------------------------------------");
			System.out.println("Xbox 360 Controller for Java Library - By Aegidius Plüss");
			System.out.println("--------------------------------------------------------");
			xc = new XboxController();
			// Left thumb deadzone to ignore small input by moving controller.
			xc.setLeftThumbDeadZone(0.2);
			WinXboxContListener();
			System.out.println("\n--------------------------------------------------------");
			System.out.println("Oculus Rift for Java Library - By Brad Davis");
			System.out.println("--------------------------------------------------------");
			//setup Oculus Rift Controller
			OculusListener();
		}else{
			LinKeyboardListener();
			LinXboxContListener();
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

	//Listener for input of Xbox controller on Windows
	public void WinXboxContListener(){
		xc.addXboxControllerListener(new XboxControllerAdapter(){

			//listens to changes in the back button. This resets the oculus tracking.
			public void back(boolean pressed){
				if(oculusConnected == true){
					oculus.recenterPose();
					System.out.println("Oculus Recentered");
				}

			}

			//listens to changes in the start button reading to enable or disable oculus tracking.
			public void start(boolean pressed){
				if(oculusConnected == true){
					oculusEnable ^= true;	
					System.out.println("Oculus Enabled:" + oculusEnable);
					if(isWindows()){
						gui.oculusEnableTitle.setText("Oculus Enabled: " + oculusEnable + "\n");
					}
				}
			}

			//listens to changes in the left thumbs magnitude reading
			public void leftThumbMagnitude(double magnitude){
				input.mag = Math.round(magnitude * 100.0) / 100.0;
				if(isWindows()){
					gui.magLabel.setText("Left Analog magnitude: " + input.mag + "\n");
				}
				updateSpeeds();
			}

			//listens to changes in the left thumbs direction reading
			public void leftThumbDirection(double direction){
				input.dir = Math.round(direction * 100.0) / 100.0;
				if(isWindows()){
					gui.dirLabel.setText("Left Analog Direction: " + input.dir + "\n");
				}
				updateSpeeds();
			}   

			//listens to changes in the left triggers magnitude reading
			public void leftTrigger(double lmagnitude){
				input.lmag = Math.round(lmagnitude * 100.0) / 100.0;
				if(isWindows()){
					gui.lmagLabel.setText("Left Trigger Magnitude: " + input.lmag + "\n");
				}
				updateSpeeds();
			}

			//listens to changes in the right triggers magnitude reading
			public void rightTrigger(double rmagnitude){
				input.rmag = Math.round(rmagnitude * 100.0) / 100.0;
				if(isWindows()){
					gui.rmagLabel.setText("Right Trigger Magnitude: " + input.rmag + "\n");
				}
				updateSpeeds();
			}

			//listens to changes in the right shoulder reading
			public void rightShoulder(boolean rshoulder){
				if(rshoulder == true){
					input.midmag = 1;
					input.mag = 1;
					input.dir = 90;
					if(isWindows()){
						gui.rshoulderLabel.setText("Right Shoulder Pressed: " + 1 + "\n");
					}
					updateSpeeds();
				}
				else if(rshoulder == false){
					input.midmag = 0;
					input.mag = 0;
					input.dir = 0;
					if(isWindows()){
						gui.rshoulderLabel.setText("Right Shoulder Pressed: " + 0 + "\n");
					}
					updateSpeeds();
				}
			}

			//listens to changes in the left shoulder reading
			public void leftShoulder(boolean lshoulder){
				if(lshoulder == true){
					input.midmag = 1;
					input.mag = 1;
					input.dir = 270;
					if(isWindows()){
						gui.lshoulderLabel.setText("Left Shoulder Pressed: " + 1 + "\n");
					}
					updateSpeeds();
				}
				else if(lshoulder == false){
					input.midmag = 0;
					input.mag = 0;
					input.dir = 0;
					if(isWindows()){
						gui.lshoulderLabel.setText("Left Shoulder Pressed: " + 0 + "\n");
					}
					updateSpeeds();
				}
			}

			//listens to changes in the connection of the controller and displays messages on loss of connection or connecting restored.
			public void isConnected(boolean connected){
				if (!connected)
				{
					System.out.println("Xbox controller not connected.");
					if(isWindows()){
						JOptionPane.showMessageDialog(null,"Xbox controller not connected.","Connection Lost",JOptionPane.ERROR_MESSAGE);
					}
				}else{
					System.out.println("Xbox controller connected.");
					xc.vibrate(50000, 50000, 300);
				}
			}
		});
	}

	//Listener for input of Xbox controller on Linux
	public void LinXboxContListener(){
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
			input.mag = Math.abs(rotation.y) / 50;     //if the rotation is between 15 and 50, then the magnitude depends on the angle.
			input.dir= 90;
		}
		else if (rotation.y > 50 && rotation.y < 99){ //above 50 the magnitude stays maximum
			input.mag = 1;
			input.dir = 90;
		}
		else if(rotation.y < -15 && rotation.y > -50){ // same applies for rotation to the left.
			input.mag = Math.abs(rotation.y) / 50;
			input.dir = 270;
		}
		else if(rotation.y < -50 && rotation.y > -99){
			input.mag = 1;
			input.dir = 270;
		}
		else{  // in all other cases the rotation is 0.
			input.dir = 0;
			input.mag = 0;
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
				case KeyEvent.VK_Z:  											// up
					if(keyList.contains("Z") == false)keyList.add("Z");		//if the key is already part of keyList, don't add it again.
					if(keyList.contains("Q")){								//if left is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 270;
						input.lmag = 0;
						input.rmag = 1;
					}else if(keyList.contains("S") == true){					// if both up and down are pressed, nothing happens
						input.rmag = 0;
						input.lmag = 0;
					}else if(keyList.contains("D")){						//if right is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 90;
						input.lmag = 0;
						input.rmag = 1;
					}else{
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 1;
					}
					updateSpeeds();
					break;			
				case KeyEvent.VK_S:											// down
					if(keyList.contains("S") == false)keyList.add("S");	//if the key is already part of keyList, don't add it again.
					if(keyList.contains("Q")){								//if left is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 270;
						input.lmag = 1;
						input.rmag = 0;
					}else if(keyList.contains("Z") == true){					//if both up and down are pressed, nothing happens.
						input.rmag = 0;
						input.lmag = 0;
					}else if(keyList.contains("D")){						//if right is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 90;
						input.lmag = 1;
						input.rmag = 0;
					}else{
						input.mag = 0;
						input.dir = 0;
						input.lmag = 1;
						input.rmag = 0;
					}
					updateSpeeds();
					break;
				case KeyEvent.VK_Q:											// left 
					if(keyList.contains("Q") == false)keyList.add("Q");	//if the key is already part of keyList, don't add it again.
					input.mag = 1;
					input.dir = 270;
					if(keyList.contains("Z")){									//if up is also being pressed, then set this as well.
						input.lmag = 0;
						input.rmag = 1;
					}else if(keyList.contains("S")){							//if down is also being pressed, then set this as well.
						input.lmag = 1;
						input.rmag = 0;
					}else{
						input.lmag = 0;
						input.rmag = 0;
					}
					updateSpeeds();
					break;
				case KeyEvent.VK_D:											// right 
					if(keyList.contains("D") == false)keyList.add("D");	//if the key is already part of keyList, don't add it again.
					input.mag = 1;
					input.dir = 90;
					if(keyList.contains("Z")){									//if up is also being pressed, then set this as well.
						input.lmag = 0;
						input.rmag = 1;
					}else if(keyList.contains("S")){							//if down is also being pressed, then set this as well.
						input.lmag = 1;
						input.rmag = 0;
					}else{
						input.lmag = 0;
						input.rmag = 0;
					}
					updateSpeeds();
					break;
				case KeyEvent.VK_A:											// left (mid rotation)
					if(keyList.contains("A") == false)keyList.add("A");	//if the key is already part of keyList, don't add it again.
					input.midmag = 1;
					input.mag = 1;
					input.dir = 270;
					updateSpeeds();
					break;
				case KeyEvent.VK_E:											// right (mid rotation)
					if(keyList.contains("E") == false)keyList.add("E");	//if the key is already part of keyList, don't add it again.
					input.midmag = 1;
					input.mag = 1;
					input.dir = 90;
					updateSpeeds();
					break;
				}				
			}

			//listens for keys being released.
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()){												
				case KeyEvent.VK_Z:											// up
					keyList.remove(getIndexByname("Z"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("S") == true){					//if down key is pressed when up is released, go backwards.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 0;
						input.lmag = 1;
						updateSpeeds();
					}
					break;			
				case KeyEvent.VK_S:											// down 
					keyList.remove(getIndexByname("S"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("Z") == true){					//if up key is pressed when down is released, go forwards.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 1;
						input.lmag = 0;
						updateSpeeds();
					}
					break;	
				case KeyEvent.VK_Q:											// left 
					keyList.remove(getIndexByname("Q"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("Z") == true){					//if up key is still pressed and left released, go forward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 1;
						input.lmag = 0;
						updateSpeeds();
					}else{														//if down key is still pressed and left released, go backward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 0;
						input.lmag = 1;
						updateSpeeds();
					}
					break;	
				case KeyEvent.VK_D:											// right
					keyList.remove(getIndexByname("D"));					//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("Z") == true){					//if up key is still pressed and left released, go forward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 1;
						input.lmag = 0;
						updateSpeeds();
					}else{														//if down key is still pressed and left released, go backward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 0;
						input.lmag = 1;
						updateSpeeds();
					}
					break;
				case KeyEvent.VK_A:											// left (mid rotation)
					keyList.remove(getIndexByname("A"));					//if key is released, remove it from keylist (uses helper function)
					input.midmag = 0;
					input.mag = 0;
					input.dir = 0;
					updateSpeeds();
					break;
				case KeyEvent.VK_E:											// right (mid rotation)
					keyList.remove(getIndexByname("E"));					//if key is released, remove it from keylist (uses helper function)
					input.midmag = 0;
					input.mag = 0;
					input.dir = 0;
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

		System.out.println("Listening for command line input, press zqsd or ae.");

		try {
			GlobalScreen.registerNativeHook();
		}
		catch (NativeHookException ex) {
			System.err.println("There was a problem registering the native hook.");
			System.err.println(ex.getMessage());
		}

		// Clear previous logging configurations.
		LogManager.getLogManager().reset();

		// Get the logger for "org.jnativehook" and set the level to off.
		Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
		logger.setLevel(Level.OFF);

		GlobalScreen.addNativeKeyListener(new NativeKeyListener(){
			//listens for keys being pressed
			public void nativeKeyPressed(NativeKeyEvent e) {


				switch(e.getKeyCode()){
				case NativeKeyEvent.VC_F12:											//recenter Oculus
					if(oculusConnected == true){
						oculus.recenterPose();
						System.out.println("Oculus Recentered");
					}

					break;
				case NativeKeyEvent.VC_F11:		
					//enable or disable Oculus Tracking
					if(oculusConnected == true){
						oculusEnable ^= true;
						System.out.println("Oculus Enabled:" + oculusEnable);
						if(isWindows()){
							gui.oculusEnableTitle.setText("Oculus Enabled: " + oculusEnable + "\n");
						}
					}
					break;
				case NativeKeyEvent.VC_Z:  											// up
					if(keyList.contains("Z") == false)keyList.add("Z");		//if the key is already part of keyList, don't add it again.
					if(keyList.contains("Q")){								//if left is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 270;
						input.lmag = 0;
						input.rmag = 1;
					}else if(keyList.contains("S") == true){					// if both up and down are pressed, nothing happens
						input.rmag = 0;
						input.lmag = 0;
					}else if(keyList.contains("D")){						//if right is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 90;
						input.lmag = 0;
						input.rmag = 1;
					}else{
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 1;
					}
					updateSpeeds();
					break;			
				case NativeKeyEvent.VC_S:											// down
					if(keyList.contains("S") == false)keyList.add("S");	//if the key is already part of keyList, don't add it again.
					if(keyList.contains("Q")){								//if left is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 270;
						input.lmag = 1;
						input.rmag = 0;
					}else if(keyList.contains("Z") == true){					//if both up and down are pressed, nothing happens.
						input.rmag = 0;
						input.lmag = 0;
					}else if(keyList.contains("D")){						//if right is also being pressed, then set direction as well.
						input.mag = 1;
						input.dir = 90;
						input.lmag = 1;
						input.rmag = 0;
					}else{
						input.mag = 0;
						input.dir = 0;
						input.lmag = 1;
						input.rmag = 0;
					}
					updateSpeeds();
					break;
				case NativeKeyEvent.VC_Q:											// left 
					if(keyList.contains("Q") == false)keyList.add("Q");	//if the key is already part of keyList, don't add it again.
					input.mag = 1;
					input.dir = 270;
					if(keyList.contains("Z")){									//if up is also being pressed, then set this as well.
						input.lmag = 0;
						input.rmag = 1;
					}else if(keyList.contains("S")){							//if down is also being pressed, then set this as well.
						input.lmag = 1;
						input.rmag = 0;
					}else{
						input.lmag = 0;
						input.rmag = 0;
					}
					updateSpeeds();
					break;
				case NativeKeyEvent.VC_D:											// right 
					if(keyList.contains("D") == false)keyList.add("D");	//if the key is already part of keyList, don't add it again.
					input.mag = 1;
					input.dir = 90;
					if(keyList.contains("Z")){									//if up is also being pressed, then set this as well.
						input.lmag = 0;
						input.rmag = 1;
					}else if(keyList.contains("S")){							//if down is also being pressed, then set this as well.
						input.lmag = 1;
						input.rmag = 0;
					}else{
						input.lmag = 0;
						input.rmag = 0;
					}
					updateSpeeds();
					break;
				case NativeKeyEvent.VC_A:											// left (mid rotation)
					if(keyList.contains("A") == false)keyList.add("A");	//if the key is already part of keyList, don't add it again.
					input.midmag = 1;
					input.mag = 1;
					input.dir = 270;
					updateSpeeds();
					break;
				case NativeKeyEvent.VC_E:											// right (mid rotation)
					if(keyList.contains("E") == false)keyList.add("E");	//if the key is already part of keyList, don't add it again.
					input.midmag = 1;
					input.mag = 1;
					input.dir = 90;
					updateSpeeds();
					break;
				}	

			}

			//listens for keys being released.
			public void nativeKeyReleased(NativeKeyEvent e) {
				switch(e.getKeyCode()){												
				case NativeKeyEvent.VC_Z:									// up
					keyList.remove(getIndexByname("Z"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("S") == true){					//if down key is pressed when up is released, go backwards.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 0;
						input.lmag = 1;
						updateSpeeds();
					}
					break;			
				case NativeKeyEvent.VC_S:											// down 
					keyList.remove(getIndexByname("S"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("Z") == true){					//if up key is pressed when down is released, go forwards.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 1;
						input.lmag = 0;
						updateSpeeds();
					}
					break;	
				case NativeKeyEvent.VC_Q:											// left 
					keyList.remove(getIndexByname("Q"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("Z") == true){					//if up key is still pressed and left released, go forward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 1;
						input.lmag = 0;
						updateSpeeds();
					}else{														//if down key is still pressed and left released, go backward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 0;
						input.lmag = 1;
						updateSpeeds();
					}
					break;	
				case NativeKeyEvent.VC_D:											// right
					keyList.remove(getIndexByname("D"));					//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("Z") == false && keyList.contains("S") == false){	//if no up/down keys are pressed, reset all values.
						input.mag = 0;
						input.dir = 0;
						input.lmag = 0;
						input.rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("Z") == true){					//if up key is still pressed and left released, go forward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 1;
						input.lmag = 0;
						updateSpeeds();
					}else{														//if down key is still pressed and left released, go backward.
						input.mag = 0;
						input.dir = 0;
						input.rmag = 0;
						input.lmag = 1;
						updateSpeeds();
					}
					break;
				case NativeKeyEvent.VC_A:											// left (mid rotation)
					keyList.remove(getIndexByname("A"));					//if key is released, remove it from keylist (uses helper function)
					input.midmag = 0;
					input.mag = 0;
					input.dir = 0;
					updateSpeeds();
					break;
				case NativeKeyEvent.VC_E:											// right (mid rotation)
					keyList.remove(getIndexByname("E"));					//if key is released, remove it from keylist (uses helper function)
					input.midmag = 0;
					input.mag = 0;
					input.dir = 0;
					updateSpeeds();
					break;	
				}				
			}

			//listens for keys being typed (unused)
			public void nativeKeyTyped(NativeKeyEvent e) {
			}

		});

	}




	//Updates the left & right wheel power based on the input
	public void updateSpeeds(){		
		// if total outcome is forwards
		if((input.rmag - input.lmag) > 0){
			//right
			if(input.dir > 0 && input.dir < 180 && input.mag > 0){
				wheelSpeeds.lwheel = Math.round((input.rmag - input.lmag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = Math.round(((input.rmag - input.lmag) - (input.rmag - input.lmag)*input.mag) * 100.0) / 100.0;
				//left
			}else if (input.dir > 180 && input.dir < 359 && input.mag > 0){
				wheelSpeeds.lwheel = Math.round(((input.rmag - input.lmag) - (input.rmag - input.lmag)*input.mag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = Math.round((input.rmag - input.lmag) * 100.0) / 100.0; ;
				//forwards
			}else{
				wheelSpeeds.lwheel = Math.round((input.rmag - input.lmag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = Math.round((input.rmag - input.lmag) * 100.0) / 100.0;
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

			// if total outcome is backwards
		}else if(input.rmag - input.lmag < 0){					
			//right
			if(input.dir > 0 && input.dir < 180 && input.mag > 0){
				wheelSpeeds.lwheel = -Math.round((input.lmag - input.rmag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = -Math.round(((input.lmag - input.rmag) - (input.lmag - input.rmag)*input.mag) * 100.0) / 100.0;
				//left
			}else if (input.dir > 180 && input.dir < 359 && input.mag > 0){
				wheelSpeeds.lwheel = -Math.round(((input.lmag - input.rmag) - (input.lmag - input.rmag)*input.mag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = -Math.round((input.lmag - input.rmag) * 100.0) / 100.0; ;
				//backwards
			}else{
				wheelSpeeds.lwheel = -Math.round((input.lmag - input.rmag) * 100.0) / 100.0;
				wheelSpeeds.rwheel = -Math.round((input.lmag - input.rmag) * 100.0) / 100.0;
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
			if(input.dir > 0 && input.dir < 180 && input.mag > 0){
				wheelSpeeds.rwheel = -0.5;
				wheelSpeeds.lwheel = 0.5;
				//Rotating on it's midpoint to the right
			}else if (input.dir > 180 && input.dir < 359 && input.mag > 0){
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




