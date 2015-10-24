package ControllerClient;
import ch.aplu.xboxcontroller.*;
import javax.swing.*;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.OvrQuaternionf;
import com.oculusvr.capi.OvrVector3f;
import com.oculusvr.capi.TrackingState;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class EmbeddedController{
	
	//Gui Elements
	static JFrame frame;
	static Box box = Box.createVerticalBox();
	static JLabel magLabel,dirLabel,lmagLabel,rmagLabel,lwheelLabel,rwheelLabel,keyListLabel,xboxTitle,keyboardTitle,oculusTitle,outputTitle,oculusPosLabel,oculusRotLabel;
	//Xbox Controller Elements
	static XboxController xc;
    static double mag = 0, dir = 0,lmag = 0,rmag = 0,lwheel = 0,rwheel = 0,oldLwheel = 0,oldRwheel = 0;
	static int direction = 0,connections = 0;
	//Oculus Elements
	static Hmd oculus;
	//RaspberryPi IP-address
	static String address = "192.168.1.201";
	//list that keeps all current pressed keyboard buttons
	static ArrayList<String> keyList = new ArrayList<String>();
	
	//Constructor
    public EmbeddedController() throws InterruptedException{
    	createGUI();
    	System.out.println("Telepresence Controller - University of Antwerp");
    	System.out.println("--------------------------------------------------------");
    	System.out.println("Xbox 360 Controller for Java Library - By Aegidius Plüss");
    	System.out.println("--------------------------------------------------------");
    	xc = new XboxController();
    	System.out.println("--------------------------------------------------------");
    	// Left thumb deadzone to ignore small input by moving controller.
    	xc.setLeftThumbDeadZone(0.2);
    	//setup Oculus Rift Controller
    	xboxControllerListener();    	
    	keyboardListener();
    	System.out.println("Oculus Rift for Java Library - By Brad Davis");
    	System.out.println("--------------------------------------------------------");
    	OculusListener();

	}
    

	
	//create the GUI
	public void createGUI(){
    	frame = new JFrame("Telepresence Robot");
        magLabel = new JLabel("Left Analog Magnitude:0.0\n");
        dirLabel = new JLabel("Left Analog Direction:0.0\n");
        lmagLabel = new JLabel("Left Trigger Magnitude:0.0\n");
        rmagLabel = new JLabel("Right Trigger Magnitude:0.0\n");
        keyListLabel = new JLabel("Pressed Keys: None\n");
        lwheelLabel = new JLabel("Left Wheel Power:0.0\n");
        rwheelLabel = new JLabel("Right Wheel Power:0.0\n");
        xboxTitle = new JLabel("Xbox Input\n");
        keyboardTitle = new JLabel("Keyboard Input\n");
        oculusTitle = new JLabel("Oculus Input\n");
        outputTitle = new JLabel("Wheel Speed Output\n");
        oculusPosLabel = new JLabel("Oculus Position Output: 0 0 0\n");
        oculusRotLabel = new JLabel("Oculus Rotation Output: 0 0 0\n");
        xboxTitle.setFont(xboxTitle.getFont().deriveFont(xboxTitle.getFont().getStyle() | Font.ITALIC));
        keyboardTitle.setFont(keyboardTitle.getFont().deriveFont(keyboardTitle.getFont().getStyle() | Font.ITALIC));
        oculusTitle.setFont(oculusTitle.getFont().deriveFont(oculusTitle.getFont().getStyle() | Font.ITALIC));
        outputTitle.setFont(outputTitle.getFont().deriveFont(outputTitle.getFont().getStyle() | Font.ITALIC));
        box.add(xboxTitle);
        box.add(magLabel);
        box.add(dirLabel);
        box.add(lmagLabel);
        box.add(rmagLabel);
        box.add(new JLabel(" "));
        box.add(keyboardTitle);
        box.add(keyListLabel);
        box.add(new JLabel(" "));
        box.add(oculusTitle);
        box.add(oculusPosLabel);
        box.add(oculusRotLabel);
        box.add(new JLabel(" "));
        box.add(outputTitle);
        box.add(lwheelLabel);
        box.add(rwheelLabel);
        frame.add(box);
        frame.setSize(new Dimension(400,320));
        frame.setLocationRelativeTo(null);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage("./lib/xbox.png"));
        frame.setVisible(true);        

        //displays cancel window on closing program
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
    		@Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(frame,
                        "Are you sure you want to close?",
                        "Exit Program?", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == JOptionPane.YES_OPTION) {
            		xc.release();
                    System.exit(1);
                }
            }
        });        
    }	
	
	//Listener for input of Xbox controller
	public void xboxControllerListener(){
			xc.addXboxControllerListener(new XboxControllerAdapter(){
    		
    		//listens to changes in the back button. This resets the oculus tracking.
    		public void back(boolean pressed){
    			oculus.recenterPose();
    		}
    		
    		//listens to changes in the left thumbs magnitude reading
    		public void leftThumbMagnitude(double magnitude){
    			mag = Math.round(magnitude * 100.0) / 100.0;
    			magLabel.setText("Left Analog magnitude: " + mag + "\n");
    			updateSpeeds();
    		}
    		
    		//listens to changes in the left thumbs direction reading
    		public void leftThumbDirection(double direction){
    			dir = Math.round(direction * 100.0) / 100.0;
    			dirLabel.setText("Left Analog Direction: " + dir + "\n");
    			updateSpeeds();
    		}   
    		
    		//listens to changes in the left triggers magnitude reading
    		public void leftTrigger(double lmagnitude){
    			lmag = Math.round(lmagnitude * 100.0) / 100.0;
    			lmagLabel.setText("Left Trigger Magnitude: " + lmag + "\n");
    			updateSpeeds();
    		}

    		//listens to changes in the right triggers magnitude reading
    		public void rightTrigger(double rmagnitude){
    			rmag = Math.round(rmagnitude * 100.0) / 100.0;
    			rmagLabel.setText("Right Trigger Magnitude: " + rmag + "\n");
    			updateSpeeds();
    		}
    		
    		//listens to changes in the connection of the controller and displays messages on loss of connection or connecting restored.
    		public void isConnected(boolean connected){
    	        if (!connected)
    	        {
    	        	System.out.println("Xbox controller not connected.");
    	        	JOptionPane.showMessageDialog(null,"Xbox controller not connected.","Connection Lost",JOptionPane.ERROR_MESSAGE);
    	        }else{
    	        	xc.vibrate(50000, 50000, 300);
    	        }
    		}
    	});
	}
	
	//Listener for input of Oculus Positional Tracking
    public void OculusListener() throws IllegalStateException{
        try{
        	//initialize the Oculus
        	Hmd.initialize();
        	oculus = Hmd.create();
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
              oculusPosLabel.setText("Oculus Position Output: " + (int)position.x + ", " + (int)position.y + " " + (int)position.z + "\n");
              oculusRotLabel.setText("Oculus Rotation Output: " + (int)rotation.x + ", " + (int)rotation.y + " " + (int)rotation.z + "\n");
            }
        //error for when oculus is not found or SDK Runtime hasn't been launched
        }catch (IllegalStateException l){
        	System.out.println("no Oculus SDK detected!");
        	JOptionPane pane = new JOptionPane("Oculus not connected or SDK not started.",JOptionPane.DEFAULT_OPTION,JOptionPane.INFORMATION_MESSAGE, null, new Object[]{}, null);
        	final JDialog dialog = pane.createDialog("Oculus Error");
        	new Thread(new Runnable()
            {
              public void run()
              {
                try
                {
                  Thread.sleep(3000);
                  dialog.dispose();
                }
                catch ( Throwable th )
                {
                }
              }
            }).start();
        	dialog.setVisible(true);    
        	System.out.println("----------------------------------------------------");
        }
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
	
	//keyboard input listener
	public void keyboardListener(){
		
		frame.addKeyListener(new KeyListener(){
			
			//listens for keys being pressed
			public void keyPressed(KeyEvent e) {
				switch(e.getKeyCode()){
				case KeyEvent.VK_F12:											//recenter oculus
					oculus.recenterPose();
					break;
				case KeyEvent.VK_UP:  											// up arrow key
					if(keyList.contains("up") == false)keyList.add("up");		//if the key is already part of keyList, don't add it again.
					if(keyList.contains("left")){								//if left is also being pressed, then set direction as well.
					    mag = 1;
						dir = 270;
						lmag = 0;
						rmag = 1;
					}else if(keyList.contains("down") == true){					// if both up and down are pressed, nothing happens
						rmag = 0;
						lmag = 0;
					}else if(keyList.contains("right")){						//if right is also being pressed, then set direction as well.
					    mag = 1;
						dir = 90;
						lmag = 0;
						rmag = 1;
					}else{
					    mag = 0;
						dir = 0;
						lmag = 0;
						rmag = 1;
					}
					updateSpeeds();
					break;			
				case KeyEvent.VK_DOWN:											// down arrow key
					if(keyList.contains("down") == false)keyList.add("down");	//if the key is already part of keyList, don't add it again.
					if(keyList.contains("left")){								//if left is also being pressed, then set direction as well.
					    mag = 1;
						dir = 270;
						lmag = 1;
						rmag = 0;
					}else if(keyList.contains("up") == true){					//if both up and down are pressed, nothing happens.
						rmag = 0;
						lmag = 0;
					}else if(keyList.contains("right")){						//if right is also being pressed, then set direction as well.
					    mag = 1;
						dir = 90;
						lmag = 1;
						rmag = 0;
					}else{
					    mag = 0;
						dir = 0;
						lmag = 1;
						rmag = 0;
					}
					updateSpeeds();
					break;
				case KeyEvent.VK_LEFT:											// left arrow key
					if(keyList.contains("left") == false)keyList.add("left");	//if the key is already part of keyList, don't add it again.
				    mag = 1;
					dir = 270;
					if(keyList.contains("up")){									//if up is also being pressed, then set this as well.
					    lmag = 0;
						rmag = 1;
					}else if(keyList.contains("down")){							//if down is also being pressed, then set this as well.
					    lmag = 1;
						rmag = 0;
					}else{
						lmag = 0;
						rmag = 0;
					}
					updateSpeeds();
					break;
				case KeyEvent.VK_RIGHT:											// right arrow key
					if(keyList.contains("right") == false)keyList.add("right");	//if the key is already part of keyList, don't add it again.
				    mag = 1;
					dir = 90;
					if(keyList.contains("up")){									//if up is also being pressed, then set this as well.
					    lmag = 0;
						rmag = 1;
					}else if(keyList.contains("down")){							//if down is also being pressed, then set this as well.
					    lmag = 1;
						rmag = 0;
					}else{
						lmag = 0;
						rmag = 0;
					}
					updateSpeeds();
					break;
				}				
			}

			//listens for keys being released.
			public void keyReleased(KeyEvent e) {
				switch(e.getKeyCode()){												
				case KeyEvent.VK_UP:											// up arrow key
					keyList.remove(getIndexByname("up"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("up") == false && keyList.contains("down") == false){	//if no up/down keys are pressed, reset all values.
						mag = 0;
						dir = 0;
						lmag = 0;
						rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("down") == true){					//if down key is pressed when up is released, go backwards.
						mag = 0;
						dir = 0;
						rmag = 0;
						lmag = 1;
						updateSpeeds();
					}
					break;			
				case KeyEvent.VK_DOWN:											// down arrow key
					keyList.remove(getIndexByname("down"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("up") == false && keyList.contains("down") == false){	//if no up/down keys are pressed, reset all values.
						mag = 0;
						dir = 0;
						lmag = 0;
						rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("up") == true){					//if up key is pressed when down is released, go forwards.
						mag = 0;
						dir = 0;
						rmag = 1;
						lmag = 0;
						updateSpeeds();
					}
					break;	
				case KeyEvent.VK_LEFT:											// left arrow key
					keyList.remove(getIndexByname("left"));						//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("up") == false && keyList.contains("down") == false){	//if no up/down keys are pressed, reset all values.
						mag = 0;
						dir = 0;
						lmag = 0;
						rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("up") == true){					//if up key is still pressed and left released, go forward.
						mag = 0;
						dir = 0;
						rmag = 1;
						lmag = 0;
						updateSpeeds();
					}else{														//if down key is still pressed and left released, go backward.
						mag = 0;
						dir = 0;
						rmag = 0;
						lmag = 1;
						updateSpeeds();
					}
					break;	
				case KeyEvent.VK_RIGHT:											// right arrow key
					keyList.remove(getIndexByname("right"));					//if key is released, remove it from keylist (uses helper function)
					if(keyList.contains("up") == false && keyList.contains("down") == false){	//if no up/down keys are pressed, reset all values.
						mag = 0;
						dir = 0;
						lmag = 0;
						rmag = 0;
						updateSpeeds();
					}else if(keyList.contains("up") == true){					//if up key is still pressed and left released, go forward.
						mag = 0;
						dir = 0;
						rmag = 1;
						lmag = 0;
						updateSpeeds();
					}else{														//if down key is still pressed and left released, go backward.
						mag = 0;
						dir = 0;
						rmag = 0;
						lmag = 1;
						updateSpeeds();
					}
					break;	
				}				
			}
			
			//listens for keys being typed (unused)
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}        	
        });
		
	}
	
	//Updates the left & right wheel power based on the input
	public void updateSpeeds(){		
		// if total outcome is forwards
		if((rmag - lmag) > 0){
			//right
			if(dir > 0 && dir < 180 && mag > 0){
				lwheel = Math.round((rmag - lmag) * 100.0) / 100.0;
				rwheel = Math.round(((rmag - lmag) - (rmag - lmag)*mag) * 100.0) / 100.0;
			//left
			}else if (dir > 180 && dir < 359 && mag > 0){
				lwheel = Math.round(((rmag - lmag) - (rmag - lmag)*mag) * 100.0) / 100.0;
				rwheel = Math.round((rmag - lmag) * 100.0) / 100.0; ;
			//forwards
			}else{
				lwheel = Math.round((rmag - lmag) * 100.0) / 100.0;
				rwheel = Math.round((rmag - lmag) * 100.0) / 100.0;
			}
			if(oldLwheel != lwheel || oldRwheel != rwheel){
				try {
					UDPSend();
					//TCPSend();
				} catch (IOException l) {
					// TODO Auto-generated catch block
					l.printStackTrace();
				}
			}
			oldLwheel = lwheel;
			oldRwheel = rwheel;
	    // if total outcome is backwards
		}else if(rmag - lmag < 0){					
			//right
			if(dir > 0 && dir < 180 && mag > 0){
				lwheel = -Math.round((lmag - rmag) * 100.0) / 100.0;
				rwheel = -Math.round(((lmag - rmag) - (lmag - rmag)*mag) * 100.0) / 100.0;
			//left
			}else if (dir > 180 && dir < 359 && mag > 0){
				lwheel = -Math.round(((lmag - rmag) - (lmag - rmag)*mag) * 100.0) / 100.0;
				rwheel = -Math.round((lmag - rmag) * 100.0) / 100.0; ;
		    //backwards
			}else{
				lwheel = -Math.round((lmag - rmag) * 100.0) / 100.0;
				rwheel = -Math.round((lmag - rmag) * 100.0) / 100.0;
			}
			if(oldLwheel != lwheel || oldRwheel != rwheel){
				try {
					UDPSend();
					//TCPSend();
				} catch (IOException l) {
					// TODO Auto-generated catch block
					l.printStackTrace();
				}
			}
			oldLwheel = lwheel;
			oldRwheel = rwheel;
		// no acceleration but still using analog
		}else{
			lwheel = 0;
			rwheel = 0;
			if(oldLwheel != lwheel || oldRwheel != rwheel){
				try {
					UDPSend();
					//TCPSend();
				} catch (IOException l) {
					// TODO Auto-generated catch block
					l.printStackTrace();
				}
			}
			oldLwheel = lwheel;
			oldRwheel = rwheel;
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
		keyListLabel.setText("Pressed Keys: " + keyListString + "\n");
		lwheelLabel.setText("Left Wheel Power: " + lwheel + "\n");
		rwheelLabel.setText("Right Wheel Power: " + rwheel + "\n");
	}
	
	//sends the wheel speeds to the Raspberry Pi over UDP
	public void UDPSend() throws IOException{
		//Both wheel-speeds combined in 1 String separated by a ","
		String speeds = lwheel + "," + rwheel;
		//Socket gets initialized
		DatagramSocket clientSocket = new DatagramSocket();
		//Getting Internet-address from IP-Address
        InetAddress IPAddress = InetAddress.getByName(address);
        byte[] sendData = new byte[1024];
        sendData = speeds.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
        clientSocket.send(sendPacket);
        //For debugging purposes we want to see how many times we sent data
      	System.out.println("Send " + connections +": " + speeds);
      	connections = connections + 1;
        //closing UDP Socket
        clientSocket.close();
	}
	
    public static void main(String[] args) throws InterruptedException{
    	new EmbeddedController();    	
    }
}
    
  
    	
    	
