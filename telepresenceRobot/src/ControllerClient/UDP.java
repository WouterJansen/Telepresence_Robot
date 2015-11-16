package ControllerClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDP {

	public WheelSpeeds wheelSpeeds;
	public int connections;
	public String address = "localhost";
	
	public UDP(WheelSpeeds wheelSpeeds, String address,int connections){
		this.wheelSpeeds = wheelSpeeds;
		this.address = address;
		this.connections = connections;
	}
	
	//sends the wheel speeds to the Raspberry Pi over UDP
	public int UDPSend() throws IOException{
		//Both wheel-speeds combined in 1 String separated by a ","
		String speeds = wheelSpeeds.lwheel + "," + wheelSpeeds.rwheel;
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
        return connections;
	}
}
