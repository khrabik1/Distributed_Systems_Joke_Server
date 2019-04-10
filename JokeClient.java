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
import java.util.*;

public class JokeClient implements Serializable{	
	//Implements serializable so i can pass the client object to the server 
	
	//Used to construct JokeClient object
	String clientID;
	String userName;
	int randomVal;
	
	public JokeClient(String clientID, String userName) {
		//constructor for client object
		this.clientID = clientID;
		this.userName = userName;
		//random val is left uninitialized on purpose, it's controlled in server.
		//could alternately probably have set it to 0
		int randomVal;
		
	}
	
	static String getUserName() {
		//uses a scanner to get the name from the user via the console.
		Scanner sc = new Scanner(System.in);
		System.out.print("Enter your name: ");
		String client = sc.nextLine();		
		return client;
		
	}
	
	static String getUserID() {
		//This creates a random ID. The ID is cast to a string from a prior implementation
		//Since it's being passed within an object now, it could have been left as an int.
		Random randomID = new Random();
		int num = randomID.nextInt(100000) + 1;
		Integer intNum = new Integer(num);
		String n = intNum.toString();
		return n;		
	}
	
	static void connectToServer(JokeClient client, String serverName,
								int port) {
		Socket sock;
		BufferedReader fromServer;
		ObjectOutputStream toServer;
		String textFromServer;
		
		try {
			//creating a new socket using the default server and port
			//As noted, I did not implement secondary server
			sock = new Socket(serverName, port);
			//fromServer opens a reader to read in any input from the server
			fromServer = new BufferedReader(new
					InputStreamReader(sock.getInputStream()));
			//This is what enables the object to be passed back to the server
			//rather than using a regular output stream, I'm using an object output stream.
			toServer = new ObjectOutputStream(sock.getOutputStream());
			//again, this is changed to handle an object - we write the entire object back to
			//the server
			toServer.writeObject(client);
			//flushing to safeguard no leaks etc.
			toServer.flush();
			//reads strings sent from server
			textFromServer = fromServer.readLine();
			
			if(textFromServer != null) {
				//as long as input from server isn't blank, print to console for client
				//this is where the jokes will print.
				System.out.println(textFromServer);
			}
			//close socket after each interaction
			sock.close();
		}
		catch(IOException x) {
			//catching exceptions on input/output from try block above
			System.out.println("Socket error.");
			x.printStackTrace();
		}
	}
	
	
	public static void  main(String args[]) {
		//port set per documentation
		//again, secondary server not implemented 
		int port = 4545;
		String serverName;
		
		System.out.print("Katie Hrabik's JokeClient, 1.8 \n");
		
		//creating a client object. This prompts getuserid and getusername to both run
		//it assigns a random ID, and gets the user name from the client and then uses
		//them to create the object
		JokeClient client = new JokeClient(getUserID(), getUserName());
		
		if(args.length<1) {
			//localhost as default if no IP specified
			serverName = "localhost";
		}
		else {
			//can also pass in a specific IP (tested- seems to function well)
			serverName = args[0];
		}
		
		//for reading in from console (either enter button or "quit")
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			String input;
			do {
				//Note this prints every cycle
				System.out.println("Hit enter to begin, or type "
						+ "(quit) to exit");
				System.out.flush();
				//reads the console input (scans for quit)
				input = in.readLine();
				
				if(input.indexOf("quit") < 0) {
					//creates server connection as long as the user didn't request to quit
					connectToServer(client, serverName, port);
				}
			}
			//the entire do statement above continues while "quit" is not found
			while(input.indexOf("quit")< 0);
			//when "quit" is found, this prints
			System.out.println("Cancelled by User Request");
				
			}
		catch(IOException x) {
			//catches any IO exceptions from try block
			System.out.println("Error");
			x.printStackTrace();
		}
	}
}
