package ControllerClient;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

public class GUI {
	
	public JFrame frame;
	public Box box = Box.createVerticalBox();
	public JLabel jmagLabel,tmagLabel,rshoulderLabel,lshoulderLabel,lwheelLabel,rwheelLabel,keyListLabel,xboxTitle,keyboardTitle,oculusTitle,outputTitle,oculusEnableTitle,oculusPosLabel,oculusRotLabel;
	
	public GUI(){
		frame = new JFrame("Telepresence Robot");
        jmagLabel = new JLabel("Analog Joystick:0.0\n");
        tmagLabel = new JLabel("Trigger:0.0\n");
        lshoulderLabel = new JLabel("Left Shoulder Pressed: 0\n");
        rshoulderLabel = new JLabel("Right Shoulder Pressed: 0\n");
        keyListLabel = new JLabel("Pressed Keys: None\n");
        lwheelLabel = new JLabel("Left Wheel Power:0.0\n");
        rwheelLabel = new JLabel("Right Wheel Power:0.0\n");
        xboxTitle = new JLabel("Xbox Input\n");
        keyboardTitle = new JLabel("Keyboard Input\n");
        oculusTitle = new JLabel("Oculus Input\n");
        outputTitle = new JLabel("Wheel Speed Output\n");
        oculusEnableTitle = new JLabel("Oculus Enabled: false\n");
        oculusPosLabel = new JLabel("Oculus Position Output: 0 0 0\n");
        oculusRotLabel = new JLabel("Oculus Rotation Output: 0 0 0\n");
        xboxTitle.setFont(xboxTitle.getFont().deriveFont(xboxTitle.getFont().getStyle() | Font.ITALIC));
        keyboardTitle.setFont(keyboardTitle.getFont().deriveFont(keyboardTitle.getFont().getStyle() | Font.ITALIC));
        oculusTitle.setFont(oculusTitle.getFont().deriveFont(oculusTitle.getFont().getStyle() | Font.ITALIC));
        outputTitle.setFont(outputTitle.getFont().deriveFont(outputTitle.getFont().getStyle() | Font.ITALIC));
        box.add(xboxTitle);
        box.add(jmagLabel);
        box.add(tmagLabel);
        box.add(lshoulderLabel);
        box.add(rshoulderLabel);
        box.add(new JLabel(" "));
        box.add(keyboardTitle);
        box.add(keyListLabel);
        box.add(new JLabel(" "));
        box.add(oculusTitle);
        box.add(oculusEnableTitle);
        box.add(oculusPosLabel);
        box.add(oculusRotLabel);
        box.add(new JLabel(" "));
        box.add(outputTitle);
        box.add(lwheelLabel);
        box.add(rwheelLabel);
        frame.add(box);
        frame.setSize(new Dimension(400,350));
        frame.setLocationRelativeTo(null);
        ArrayList<Image> imageList = new ArrayList<Image>();
        imageList.add(Toolkit.getDefaultToolkit().getImage("./lib/icon16.png"));
        imageList.add(Toolkit.getDefaultToolkit().getImage("./lib/icon64.png"));
        imageList.add(Toolkit.getDefaultToolkit().getImage("./lib/icon128.png"));
        imageList.add(Toolkit.getDefaultToolkit().getImage("./lib/icon256.png"));
        frame.setIconImages(imageList);
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
                    System.exit(1);
                }
            }
        });     
	}
}
