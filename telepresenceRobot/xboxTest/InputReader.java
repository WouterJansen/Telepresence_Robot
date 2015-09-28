import ch.aplu.xboxcontroller.*;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.WindowEvent;

public class InputReader{
	
	static JFrame frame;
	static Box box = Box.createVerticalBox();
	static JLabel line1;
	static JLabel line2;
	static JLabel line3;
	static JLabel line4;
	static XboxController xc;
    double mag = 0;
	double dir = 0;
    double lmag = 0;
	double rmag = 0;
	
	public static void WindowCreate(){
    	frame = new JFrame("Teleprecence Robot v1.0");
        line1 = new JLabel("Left Analog Magnitude:\n");
        line2 = new JLabel("Left Analog Direction:\n");
        line3 = new JLabel("Left Trigger Magnitude:\n");
        line4 = new JLabel("Right Trigger Magnitude:\n");
        box.add(line1);
        box.add(line2);
        box.add(line3);
        box.add(line4);
        frame.add(box);
        frame.setSize(new Dimension(320,120));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
    }
	
    public InputReader(){
    	xc = new XboxController();	        
    	
    	if (!xc.isConnected()){
    		JOptionPane.showMessageDialog(null,"Xbox controller not connected.","Fatal error",JOptionPane.ERROR_MESSAGE);
    		xc.release();
    		return;
    	}          	
    	xc.setLeftThumbDeadZone(0.2);
    	
    	xc.addXboxControllerListener(new XboxControllerAdapter(){
    		
    		public void leftThumbMagnitude(double magnitude){
    			mag = magnitude;
    			line1.setText("Left Analog magnitude: " + mag + "\n");
    		}

    		public void leftThumbDirection(double direction){
    			dir = direction;
    			line2.setText("Left Analog Direction: " + dir + "\n");
    		}   
    		
    		public void leftTrigger(double lmagnitude){
    			lmag = lmagnitude;
    			line3.setText("Left Trigger Magnitude: " + lmag + "\n");
    		}

    		public void rightTrigger(double rmagnitude){
    			rmag = rmagnitude;
    			line4.setText("Right Trigger Magnitude: " + rmag + "\n");
    		}
    		
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
    	
    	frame.addWindowListener(new java.awt.event.WindowAdapter() {
    		@Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(frame,
                        "Are you sure to close?",
                        "Exit Program?", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == JOptionPane.YES_OPTION) {
            		xc.release();
                    System.exit(1);
                }
            }
        });

    }
}
    
  
    	
    	
