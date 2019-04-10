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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JokeServer {
	
		//serverMode is DEFAULTED to joke.
		public static String serverMode = "Joke";
		public static Map<String, String> jokeHistory = new HashMap<String, String >();
		public static Map<String, String> proverbHistory = new HashMap<String, String >();
		public static int resetJoke = 0;
		public static int resetProv = 0;
		
		public static void main(String a[]) throws IOException {
			
			
			//6 is how many simultaneous connections our server socket will handle
			int q_len = 6;
			//secondary port not yet implemented: port set to 4545 per specs
			int port = 4545 ;
			Socket sock;
			
			System.out.println("Katie Hrabik's JokeServer is starting up at port " + port);
			System.out.println("Secondary server was not implemented. No odd behavior detected otherwise");
			
			//the admin looper starts right when the program begins 
			//it listens patiently for a connection from the jokeClientAdmin
			//This schema borrowed from Professor Elliott's notes on the Client Admin
			
			AdminLooper AL = new AdminLooper();
			//Creating a new thread for the admin looper
			Thread t = new Thread(AL);
			//starting the thread
			t.start();
			
			//This server socket is listening at the standard port
			//This is where the server listens for the clients
			ServerSocket servsock = new ServerSocket(port, q_len);
			
			
			// while true ie pretty much forever until the server connection is closed
			while(true) {
				//wait to accept a connection
				sock = servsock.accept();
				//when there's a connection to a client, start a worker
				new Worker(sock).start();
				
			}
		}
	}


	class AdminLooper implements Runnable {
		
		  public static boolean adminControlSwitch = true;

		  public void run(){ //This functions very similarly to above
			  //It sits waiting for a ClientAdmin connection
			  
		    System.out.println("Server is listening patiently for an admin connection");
		    
		    int q_len = 6; //It uses the same q_len as above aka simultaneous connection
		    //threshold
		    int port = 5050; //5050 is the port for the clientAdmin
		    
		    Socket sock;

		    try{
		    	//creating a new server socket for the admin connection
		      ServerSocket servsock = new ServerSocket(port, q_len);
		      while (adminControlSwitch) {
			// When a connection is found, accept the connection 
			sock = servsock.accept();
			// create a new MODEWORKER thread - this is responsible for changing mode.
			new ModeWorker (sock).start(); 
		      }
		      //catching exception
		    }catch (IOException ioe) {System.out.println(ioe);}
		  }

		}

	
	class Worker extends Thread {
		//creating new socket connection for regular client
		Socket sock;
		Worker(Socket s) { sock = s;}
		
		public void run() {
			//we have a printstream to send data back to the client
			PrintStream out = null;
			//but then we have an object stream for reading in the object sent from the client
			ObjectInputStream in = null;
			
			try {
				//setting up connection to receive client object
				in = new ObjectInputStream(sock.getInputStream());
				//setting up connection to send strings back to client
				out = new PrintStream(sock.getOutputStream());
				try {
					//reading in the client object from the JavaClient
					JokeClient client = (JokeClient) in.readObject();		

					System.out.println("Client ID: " + client.clientID);
					
					//This is a failsafe to ensure that the first time a client
					//is encountered, their histories are set to NNNN (the default value)
					
					if(JokeServer.jokeHistory.get(client.clientID) == null) {
						JokeServer.jokeHistory.put(client.clientID, "NNNN");
					}
					if(JokeServer.proverbHistory.get(client.clientID) == null) {
						JokeServer.proverbHistory.put(client.clientID, "NNNN");
					}
					
					//we kick off the functionality portion of the thread with randomizer function
					//(explained below)
					randomizer(client, out);
					
					
					//Toggling flags - This is how resetting is handled when cycle is completed
					if(JokeServer.resetJoke == 1) {
						JokeServer.resetJoke = 0;
					}
					
					if(JokeServer.resetProv == 1) {
						JokeServer.resetProv = 0;
					}
					
						
					
					
				}
				catch(ClassNotFoundException x) {
					//This catches errors when there's an error reading in the object from client
					System.out.println("Server Error");
					x.printStackTrace();
				}
				//close socket after interaction
				sock.close();
			}
			//This exception is for the outer try block - catches IO exceptions
			catch(IOException ioe) {System.out.println(ioe);}
		}
		
		static void randomizer(JokeClient client, PrintStream out) {			
			
			String clientLineup;
			if (JokeServer.serverMode.equals("Joke")) {
				//When the server is in joke mode, we retrieve the joke history for the client
				clientLineup = JokeServer.jokeHistory.get(client.clientID);
				//randhelper is called to help us pick a random joke to send back out of the
				//ones we haven't seen yet
				randHelper(client, clientLineup, out);
				if(JokeServer.resetJoke == 0) {
					//we generate our joke only when our reset flag isn't set
				jokeGenerator(client, out); }
			}
			else{
				//when the server's in proverb mode, we retrieve the proverb history for this client
				//(held in hashmap above)
				clientLineup = JokeServer.proverbHistory.get(client.clientID);
				//randhelper called to pick random proverb to send back out of the ones that haven't
				//been seen yet
				randHelper(client, clientLineup, out);
				if(JokeServer.resetProv == 0) {
					//generates proverb only when reset flag isn't set
				proverbGenerator(client, out); }
				
			
				
				
			
			
			}
			
		}
		
		static void randHelper(JokeClient client, String clientLineup, PrintStream out) {
			//create a new random generator
			Random randomGen = new Random();
			List<Integer> randomHolder = new ArrayList<Integer>();
			
			System.out.println("Picking random number for client " + client.userName);
			//iterates through the given history, and if we find ones we haven't seen yet
			// (i.e. if history is YNNN, it means we saw the first but not the rest)
			//they're added to randomholder for later use to generate the random
			for (int i = 0; i < clientLineup.length(); i++) {			
				if(clientLineup.charAt(i) == 'N') {				
					randomHolder.add(i);
				}
				
			}
			if (!randomHolder.isEmpty()) {
				//whenever we have values of 'N' aka when there's jokes/proverbs that haven't
				//been seen yet
				int randInd = randomGen.nextInt(randomHolder.size());
				//giving the random value to the client
				client.randomVal = randomHolder.get(randInd); 
				
				
				}
			else if (randomHolder.isEmpty()) {
				//if the randomHolder is empty, aka when we've seen all the jokes/proverbs
				//print to console that the cycle is completed
				out.println(JokeServer.serverMode + " cycle completed.");
				//reset history method resets the history for either joke or proverb
				//It works depending on the MODE the server is in.
				//Therefore, if we run out of jokes and then switch to proverb mode,
				//It won't reset joke history until we're back in joke mode.
				resetHistory(client);
			}
			
		}
		

		
	static void jokeGenerator(JokeClient client, PrintStream out) {
			
			//based on the random value given from randomizer (which is an index of 0-3 representing placements of jokes in NNNN format)
			//joke is printed. Else, an error is given.
			if (client.randomVal == 0) { out.println("JA " + client.userName + ": Why do you never see elephants hiding in trees? Because they're so good at it.");}
			else if (client.randomVal == 1) { out.println("JB " + client.userName + ": Did you hear about the restaurant on the moon? Great food, no atmosphere.");}
			else if (client.randomVal == 2) { out.println("JC " + client.userName + ": What do you call an elephant that doesn't matter? An irrelephant.");}
			else if (client.randomVal == 3) { out.println("JD " + client.userName + ": Want to hear a joke about construction? I'm still working on it.");}
			else {out.println("There has been an error. " + client.randomVal);}
			//update history is called so we can update our joke history to reflect the joke we just saw
			updateHistory(client.clientID, client.randomVal);
		}
		
	static void proverbGenerator(JokeClient client, PrintStream out) {
			
			//same as above: random number given from randomizer (represents placement of letter in NNYN format)
			//based on number given, a proverb is generated
			if ( client.randomVal == 0) { out.println("PA " + client.userName + ": A journey of a thousand miles begins with a single step.");}
			else if (client.randomVal == 1) { out.println("PB " + client.userName + ": A fool and his money are soon parted.");}
			else if (client.randomVal == 2) { out.println("PC " + client.userName + ": People who live in glass houses should not throw stones.");}
			else if (client.randomVal == 3) { out.println("PD " + client.userName + ": The early bird gets the worm.");}
			else {out.println("There has been an error.");}
			//update history is called so we can update proverb history to reflect the proverb that we just saw
			updateHistory(client.clientID, client.randomVal);

		}
		
		static void updateHistory(String clientID, int chosenInd) {
			ArrayList<Character> charHolder = new ArrayList<Character>();
			//new string and new array initialized
			String newLineup = "";
			if(JokeServer.serverMode.equals("Joke")) {
					//when we're in joke mode, we use the joke history from our hash maps (above at top)
					String jokeLineup = JokeServer.jokeHistory.get(clientID);
					for(int i = 0; i<jokeLineup.length(); i++) {
						charHolder.add(jokeLineup.charAt(i));
					}
					//we change the index of the given joke to Y to reflect that we've seen it
					charHolder.set(chosenInd, 'Y');
					//stringhelper is for formatting of charHolder array (see below for more)
					newLineup = stringHelper(charHolder);
					//this updates the joke history for the current client 
					JokeServer.jokeHistory.put(clientID, newLineup);
				
			}
			else  {
				//when we're in proverb mode, we get our prior history for proverbs
				String provLineup = JokeServer.proverbHistory.get(clientID);
				for (int i = 0; i < provLineup.length(); i++) {
					charHolder.add(provLineup.charAt(i));
					//read them into an array
					
				}
				//update the value for the proverb we just saw to Y
				charHolder.set(chosenInd, 'Y');
				//reformat into string with string helper
				newLineup = stringHelper(charHolder);
				//and update the proverb history for the current client
				JokeServer.proverbHistory.put(clientID, newLineup);
			}
		}
			
		static void resetHistory(JokeClient client){
			if(JokeServer.serverMode.equals("Joke")) {
				//this resets the joke history back to default NNNN
				JokeServer.jokeHistory.put(client.clientID, "NNNN");
				//resets the 'random val" (used by randomizer)
				client.randomVal = 0;
				//and sets the 'reset flag' to be used above
				JokeServer.resetJoke = 1;
			}
			else{
				//this resets proverb history back to default NNNN
				JokeServer.proverbHistory.put(client.clientID, "NNNN");
				//resets the random val (used by randomizer)
				client.randomVal = 0;
				//and sets the 'reset flag' to be used above
				JokeServer.resetProv = 1;
				
				
			}
			
		}
		
		
		static String stringHelper(ArrayList<Character> charHolder) {
			//takes in the list of characters
			StringBuilder builder = new StringBuilder(charHolder.size());
			for(Character c : charHolder) {
				//uses string builder to build a string out of them
				builder.append(c);
			}
			//returns the string
			return builder.toString();
		}
		
	}
	
	
	class ModeWorker extends Thread{
		
		//modeworker is similar to worker. It handles communicating the new
		//mode from the clientAdmin

		Socket sock;
		//a socket is set up again for communication to the admin
		ModeWorker (Socket s) {sock = s;}
		public void run() {
			//printstream and reader initialized
			PrintStream out = null;
			BufferedReader in = null;
			
			try { 
				//connecting the input and output streams from the client.
				in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				out = new PrintStream(sock.getOutputStream());
			try {
				//mode sent in via 'in' reader is either 'joke' or 'proverb'
				String mode;
				mode = in.readLine ();
				//print statement so server prints that the mode has changed
				System.out.println("Your mode is now: " + mode);
				//running set mode to actually change the mode server side
				setMode(mode, out);		
			} catch(IOException x) {
				//catching exceptions from inner try block
				System.out.println("Server Error");
				x.printStackTrace();
			}
			//closing after each connection
			sock.close();
			//catching exceptions from outer try block
			} catch (IOException ioe) {System.out.println(ioe);}
		}
		
		
		//changes server mode
		static void setMode(String mode, PrintStream out) {
			//prints to client that the current mode has changed
			 out.println("CURRENT MODE: " + mode + "...");
			 //changes the mode server side
			 JokeServer.serverMode = mode;
		
	}
		
	}
	

	
	