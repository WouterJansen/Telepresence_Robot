package VideoStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

// Class to start the videostream to the receiving client with the raspicam.
public class StartStream extends Thread{

    public static void main(String[] args) {
        (new StartStream()).start();
    }

    public void run() {

                String command = "./start_stream.sh";

                StringBuffer output = new StringBuffer();

                Process p;
                try {                	
                        p = Runtime.getRuntime().exec(command);
                        p.waitFor();
                        BufferedReader reader =
                            new BufferedReader(new InputStreamReader(p.getInputStream()));

                        String line = "";
                        System.out.println("\n VideoStream started!");
                        while ((line = reader.readLine())!= null) {
                                output.append(line + "\n");
                        }


                } catch (Exception e) {
                		System.out.println("\n Error loading VideoStream:");
                        e.printStackTrace();
                        
                }

        }

}

