/*--------------------------------------------------------

1. Name / Date: Katheryn Hrabik, January 27 2019

2. Java 1.8.0_152

3. Precise command-line compilation examples / instructions:

Client: javac JokeClient.java
Server: javac JokeServer.java
AdminClient: javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:


In three separate shell windows:

java JokeClient
java JokeServer
java JokeClientAdmin

To run server and client on separate machines, pass IP of server machine
to Client as argument.

5. List of files needed for running the program.

Files needed:
a. HrabikInetClient.class
b. HrabikInetServer.class
c. Worker.class

5. Notes:


Second attempt -randomization fixed, secondary server omitted
(couldn't get it running).

I believe it meets all specified conventions.

Randomization seems to work, I have not found any odd behavior.

Secondary server was not implemented.

Preservation of state:
Client identifies itself with a randomized ID, and holds an initial state as a string.
Server holds a hashmap for both joke history and proverb history, matching client ID to
a string for its jokes/proverbs.

JokeAdminClient:
Can be started or stopped at any point. Hitting enter toggles between joke and
proverb mode. Type "quit" to quit.

----------------------------------------------------------*/
import java.io.*;
import java.net.*;

public class JokeClientAdmin {
	
	
	
	
	public static void main(String args[]) {
		int primaryPort = 5050;
		//Not used: secondary port not yet implemented
		int secondaryPort = 5050;
		String serverName;
		//tracker keeps track of mode toggle
		int tracker = 0;
		//assign a string representation of mode to this
		//don't need to personalize mode for each client:
		//when proverb is changed for one it's changed for all
		String myMode; 
		
		
		
		
		if(args.length<1) {
			//defaulting to local host
			serverName = "localhost";
		}
		else {
			//if a server is specified, we use this instead of default
			serverName = args[0];
		}
		
		System.out.println("Katie Hrabik's JokeClientAdmin, 1.8 \n");
		
		
		//Buffered reader to read in console input (enter or 'quit')
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			String input;
			do {
				//line jokeClient, user is prompted to hit enter to toggle again
				System.out.print("Hit enter to toggle modes," + "Or type (quit) to end: ");
				//flush for safety, although most likely not needed for string as
				//it usually auto-flushes
				System.out.flush();
				
				//assigning input from console to variable
				input = in.readLine();
				
				
				if(input.indexOf("quit")<0) {
					//getMode decides mode based on tracker value
					myMode = getMode(tracker);
					//get from server is the communication method to talk
					//to the server aka send it the new mode
					getFromServer(myMode, serverName, primaryPort);
					//we toggle the tracker manually after receipt to make sure it
					//switches. When it's even, it's a proverb: odd, a joke.
					tracker++;
					
				}}
			while(input.indexOf("quit")<0);
			//cancels when the word quit is found in input
			System.out.println("Cancelled by User Request");
		
		}
			catch(IOException x) {
				//catches any random input/output errors
				System.out.println("Error");
				x.printStackTrace();
			}
	
}
	
	static void getFromServer(String state, String serverName, int primaryPort) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try {
			//This is again very similar to joke client
			sock = new Socket(serverName, primaryPort);
			//we have a buffered reader to read in from the server
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			//and a printstream to send data back out
			toServer = new PrintStream(sock.getOutputStream());
			//sends state (aka mode) back to the server
			toServer.println(state);
			//flush again just a safeguard 			
			toServer.flush();
			//this holds anything we get back from the server			
			textFromServer = fromServer.readLine();
			
			if(textFromServer != null) {
				//as long as we get an input from the server, it's printed here
				System.out.println(textFromServer);
			}
		//close socket after each interaction
		sock.close();
		
	}
		catch(IOException x) {
			//catches input/output errors
			System.out.println("Socket error.");
			//prints stack trace to trace error
			x.printStackTrace();
		}
		}
	

	static String getMode(int counter) {
		
		//This gives us a string representation of the mode
		//Tracker increments by one with each connection
		//Evens are proverbs, odds are jokes.
		String mode = "";
		if(counter%2 == 0) {
			mode = "Proverb";
			
		}
		else if (counter%2 != 0) {
			mode = "Joke";
		}
		return mode;
	}
	
}
	
