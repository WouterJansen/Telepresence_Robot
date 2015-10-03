import ch.aplu.xboxcontroller.*;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

public class InputReader{
	
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
	
	//Method to create the GUI
	public static void WindowCreate(){
    	frame = new JFrame("Teleprecence Robot");
        line1 = new JLabel("Left Analog Magnitude:0.0\n");
        line2 = new JLabel("Left Analog Direction:0.0\n");
        line3 = new JLabel("Left Trigger Magnitude:0.0\n");
        line4 = new JLabel("Right Trigger Magnitude:0.0\n");
        line5 = new JLabel("Left Wheel Power:0.0\n");
        line6 = new JLabel("Right Wheel Power:0.0\n");
        box.add(line1);
        box.add(line2);
        box.add(line3);
        box.add(line4);
        box.add(line5);
        box.add(line6);
        frame.add(box);
        frame.setSize(new Dimension(300,150));
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
				lwheel = Math.round((rmag - lmag) * 100.0) / 100.0;;
				rwheel = Math.round((rmag - lmag) * 100.0) / 100.0; ;
			}
	    // if total outcome is backwards
		}else{					
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
				lwheel = -Math.round((lmag - rmag) * 100.0) / 100.0;;
				rwheel = -Math.round((lmag - rmag) * 100.0) / 100.0; ;
			}
		}	
	}
	
	
	
	//Method to read in the Xbox Controller input
    public InputReader(){
    	
    	xc = new XboxController();	   
    	// Left thumb deadzone to mask small input by moving controller.
    	xc.setLeftThumbDeadZone(0.2);
    	
    	//Listener for input of controller
    	xc.addXboxControllerListener(new XboxControllerAdapter(){
    		
    		//listens to changes in the left thumbs magnitude reading
    		public void leftThumbMagnitude(double magnitude){
    			mag = Math.round(magnitude * 100.0) / 100.0;
    			line1.setText("Left Analog magnitude: " + mag + "\n");
    			updateSpeeds();
    			line5.setText("Left Wheel Power: " + lwheel + "\n");
    			line6.setText("Right Wheel Power: " + rwheel + "\n");
    		}
    		
    		//listens to changes in the left thumbs direction reading
    		public void leftThumbDirection(double direction){
    			dir = Math.round(direction * 100.0) / 100.0;
    			line2.setText("Left Analog Direction: " + dir + "\n");
    			updateSpeeds();
    			line5.setText("Left Wheel Power: " + lwheel + "\n");
    			line6.setText("Right Wheel Power: " + rwheel + "\n");
    		}   
    		
    		//listens to changes in the left triggers magnitude reading
    		public void leftTrigger(double lmagnitude){
    			lmag = Math.round(lmagnitude * 100.0) / 100.0;
    			line3.setText("Left Trigger Magnitude: " + lmag + "\n");
    			updateSpeeds();
    			line5.setText("Left Wheel Power: " + lwheel + "\n");
    			line6.setText("Right Wheel Power: " + rwheel + "\n");
    		}

    		//listens to changes in the right triggers magnitude reading
    		public void rightTrigger(double rmagnitude){
    			rmag = Math.round(rmagnitude * 100.0) / 100.0;
    			line4.setText("Right Trigger Magnitude: " + rmag + "\n");
    			updateSpeeds();
    			line5.setText("Left Wheel Power: " + lwheel + "\n");
    			line6.setText("Right Wheel Power: " + rwheel + "\n");
    		}
    		
    		//listens to changes in the connection of the controller and displays messages on loss of connection or connecting restored.
    		public void isConnected(boolean connected){
    	        if (!connected)
    	        	JOptionPane.showMessageDialog(null,"Xbox controller not connected.","Connection Lost",JOptionPane.ERROR_MESSAGE);
    	        else
    	        	JOptionPane.showMessageDialog(null,"Xbox controller connected.","Connected",JOptionPane.INFORMATION_MESSAGE);
    	    }    		
    	});
	}
    
    public static void main(String[] args){
    	new InputReader();
    	WindowCreate();
    }
}
    
  
    	
    	
