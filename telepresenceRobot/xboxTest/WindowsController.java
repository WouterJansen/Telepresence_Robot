import ch.aplu.xboxcontroller.*;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class WindowsController{
	
	//Gui Elements
	static JFrame frame;
	static Box box = Box.createVerticalBox();
	static JLabel line1;
	static JLabel line2;
	static JLabel line3;
	static JLabel line4;
	static JLabel line5;
	static JLabel line6;
	//Xbox Controller Elements
	static XboxController xc;
    static double mag = 0;
	static double dir = 0;
    static double lmag = 0;
	static double rmag = 0;
    static double lwheel = 0;
	static double rwheel = 0;
	static int direction = 0;
	static int connections = 0;
	//RaspberryPi IP-address
	static String address = "192.168.1.201";
	//list that keeps all current pressed keyboard buttons
	ArrayList<String> keyList = new ArrayList<String>();
	
	//Constructor
    public WindowsController(){
    	createGUI();
    	xc = new XboxController();	   
    	// Left thumb deadzone to ignore small input by moving controller.
    	xc.setLeftThumbDeadZone(0.2);
    	xboxControllerListener();    	
    	keyboardListener();
	}
	
	//create the GUI
	public static void createGUI(){
    	frame = new JFrame("Teleprecence Robot");
        line1 = new JLabel("Left Analog Magnitude:0.0\n");
        line2 = new JLabel("Left Analog Direction:0.0\n");
        line3 = new JLabel("Left Trigger Magnitude:0.0\n");
        line4 = new JLabel("Right Trigger Magnitude:0.0\n");
        line5 = new JLabel("Left Wheel Power:0.0\n");
        line6 = new JLabel("Right Wheel Power:0.0\n");
        box.add(new JLabel("Xbox Input:"));
        box.add(line1);
        box.add(line2);
        box.add(line3);
        box.add(line4);
        box.add(new JLabel(" "));
        box.add(new JLabel("Wheel Speed Output:"));
        box.add(line5);
        box.add(line6);
        frame.add(box);
        frame.setSize(new Dimension(400,200));
        frame.setLocationRelativeTo(null);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage("xbox.png"));
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
	
	//Listener for input of controller
	public void xboxControllerListener(){
			xc.addXboxControllerListener(new XboxControllerAdapter(){
    		
    		//listens to changes in the left thumbs magnitude reading
    		public void leftThumbMagnitude(double magnitude){
    			mag = Math.round(magnitude * 100.0) / 100.0;
    			line1.setText("Left Analog magnitude: " + mag + "\n");
    			updateSpeeds();
    		}
    		
    		//listens to changes in the left thumbs direction reading
    		public void leftThumbDirection(double direction){
    			dir = Math.round(direction * 100.0) / 100.0;
    			line2.setText("Left Analog Direction: " + dir + "\n");
    			updateSpeeds();
    		}   
    		
    		//listens to changes in the left triggers magnitude reading
    		public void leftTrigger(double lmagnitude){
    			lmag = Math.round(lmagnitude * 100.0) / 100.0;
    			line3.setText("Left Trigger Magnitude: " + lmag + "\n");
    			updateSpeeds();
    		}

    		//listens to changes in the right triggers magnitude reading
    		public void rightTrigger(double rmagnitude){
    			rmag = Math.round(rmagnitude * 100.0) / 100.0;
    			line4.setText("Right Trigger Magnitude: " + rmag + "\n");
    			updateSpeeds();
    		}
    		
    		//listens to changes in the connection of the controller and displays messages on loss of connection or connecting restored.
    		public void isConnected(boolean connected){
    	        if (!connected)
    	        	JOptionPane.showMessageDialog(null,"Xbox controller not connected.","Connection Lost",JOptionPane.ERROR_MESSAGE);
    	        else{
    	        	xc.vibrate(32767, 32767, 300);
    	        }
    		}
    	});
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
				case KeyEvent.VK_UP:  											// up arrow key
					if(keyList.contains("up") == false)keyList.add("up");		//if the key is already part of keyList, don't add it again.
					if(keyList.contains("left")){								//if left is also being pressed, then set direction as well.
					    mag = 1;
						dir = 270;
					}else if(keyList.contains("right")){						//if right is also being pressed, then set direction as well.
					    mag = 1;
						dir = 90;
					}else{
					    mag = 0;
						dir = 0;
					}
				    lmag = 0;
					rmag = 1;
					updateSpeeds();
					break;			
				case KeyEvent.VK_DOWN:											// down arrow key
					if(keyList.contains("down") == false)keyList.add("down");	//if the key is already part of keyList, don't add it again.
					if(keyList.contains("left")){								//if left is also being pressed, then set direction as well.
					    mag = 1;
						dir = 270;
					}else if(keyList.contains("right")){						//if right is also being pressed, then set direction as well.
					    mag = 1;
						dir = 90;
					}else{
					    mag = 0;
						dir = 0;
					}
				    lmag = 1;
					rmag = 0;
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
						rmag = 1;
					}else{														//if down key is still pressed and left released, go backward.
						lmag = 1;
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
						rmag = 1;
						lmag = 0;
					}else{														//if down key is still pressed and left released, go backward.
						rmag = 0;
						lmag = 1;
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
	public static void updateSpeeds(){		
		// if total outcome is forwards
		if((rmag - lmag) > 0){
			//right
			if(dir > 0 && dir < 180 && mag > 0){
				lwheel = Math.round((rmag - lmag) * 100.0) / 100.0;
				rwheel = Math.round((1 -((rmag - lmag)*mag)) * 100.0) / 100.0;
			//left
			}else if (dir > 180 && dir < 359 && mag > 0){
				lwheel = Math.round((1 -((rmag - lmag)*mag)) * 100.0) / 100.0;
				rwheel = Math.round((rmag - lmag) * 100.0) / 100.0; ;
			//forwards
			}else{
				lwheel = Math.round((rmag - lmag) * 100.0) / 100.0;
				rwheel = Math.round((rmag - lmag) * 100.0) / 100.0;
			}
//			try {
//				TCPSend();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	    // if total outcome is backwards
		}else if(rmag - lmag < 0){					
			//right
			if(dir > 0 && dir < 180 && mag > 0){
				lwheel = -Math.round((lmag - rmag) * 100.0) / 100.0;
				rwheel = -Math.round((1 -((lmag - rmag)*mag)) * 100.0) / 100.0;
			//left
			}else if (dir > 180 && dir < 359 && mag > 0){
				lwheel = -Math.round((1 -((lmag - rmag)*mag)) * 100.0) / 100.0;
				rwheel = -Math.round((lmag - rmag) * 100.0) / 100.0; ;
		    //backwards
			}else{
				lwheel = -Math.round((lmag - rmag) * 100.0) / 100.0;
				rwheel = -Math.round((lmag - rmag) * 100.0) / 100.0;
			}
//			try {
//			TCPSend();
//			} catch (IOException l) {
//				// TODO Auto-generated catch block
//				l.printStackTrace();
//			}
		// no acceleration but still using analog
		}else{
			lwheel = 0;
			rwheel = 0;
//			try {
//			TCPSend();
//			} catch (IOException l) {
//				// TODO Auto-generated catch block
//				l.printStackTrace();
//			}
		}
		line5.setText("Left Wheel Power: " + lwheel + "\n");
		line6.setText("Right Wheel Power: " + rwheel + "\n");
	}
	
	//sends the wheel speeds to the Raspberry Pi 
	public static void TCPSend() throws UnknownHostException, IOException{
		
		String speeds;				
		Socket clientSocket = new Socket();
		
		clientSocket.connect(new InetSocketAddress(address, 6789));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());		
		speeds = lwheel + "," + rwheel;
		outToServer.writeBytes(speeds + '\n');
		System.out.println("Send:" + connections);
		connections = connections + 1;
		clientSocket.close();		
	}
    
    public static void main(String[] args){
    	new WindowsController();    	
    }
}
    
  
    	
    	
