package simple.im.client;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Vector;

import simple.im.common.PresenceService;
import simple.im.common.RegistrationInfo;

/**
 * Simple client that prompts the user for registration information.
 * 
 * @author Niko Solihin
 *
 */
public class MyClient extends Thread {
	private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	private String username = null;
	private String hostname = null;
	private int port = 0;
    private Socket clientSocket = null;
    private DataOutputStream streamOut = null;
    private PresenceService theServer = null;
    private RegistrationInfo theUser = null;
	
    public MyClient(String username, String hostname, int port) {
		this.username = username;
		this.hostname = hostname;
		this.port = port;
	}

	public boolean rmiConnect() {
		boolean result = false;
    	System.out.println("Registering user to Presence Service...");
		System.out.println("\tUsername: " + this.username);
		System.out.println("\tHost: " + this.hostname);
		System.out.println("\tPort: " + this.port);
		try {
			System.setSecurityManager(new RMISecurityManager());
			theServer = (PresenceService) Naming.lookup("rmi://"+this.hostname+":"+this.port+"/myserver");
			theUser = new RegistrationInfo(this.username, this.hostname, this.port, true);	
			result = theServer.register(theUser);
			System.out.println("\nPresence Registration Successful...");
		} catch (Exception e) {
			System.out.println("Error registering user through RMI.");
			e.printStackTrace();
		}
		return result;
    }
	
	public void run() {
    	boolean done = false;
		while (!done) {
			try {
				System.out.println("\n-------------------------------------------------------------------");
				System.out.println( "\nAvailable Commands - friends, talk {username} {message}, " +
									"broadcast {message}, busy, available, exit");
				System.out.print("Enter Command: ");
				String command = reader.readLine();
				
				if(command==null || command.equalsIgnoreCase("exit")) {
					done = true;
				} else { 
					if (command.equalsIgnoreCase("friends")) {
		            	Vector<RegistrationInfo> onlineUsers = new Vector<RegistrationInfo>(theServer.listRegisteredUsers());
		            	System.out.println("\n" + onlineUsers.size() + " users are currently online:");
		            	for (RegistrationInfo user : onlineUsers) {
		            		String userStatus = (user.getStatus() == true) ? "Available" : "Busy"; 
		            		System.out.println(user.getUserName() + " - " + userStatus);	
		            	}
					} 
					else if (command.startsWith("talk")) {
						String[] parameter = command.split(" ", 3);
						RegistrationInfo receiver = theServer.lookup(parameter[1]); 
						if ( receiver.getStatus() ) {
							talk(receiver.getHost(), receiver.getLocalPort(), parameter[2]);
						} else {
							System.out.println("\nThat user is currently busy");
						}
			        }
					else if (command.equalsIgnoreCase("busy")) {
						if (theUser.getStatus()) {
							theUser.setStatus(false);
							if ( theServer.updateRegistrationInfo(theUser) ) {
								System.out.println("\nYou are now not available!");
							} else {
								System.out.println("\nError Changing Status");
							}
						} else { 
							System.out.println("\nYou are already not available!"); 
						}
			        } 
					else if (command.equalsIgnoreCase("available")) {
						if (!theUser.getStatus()) {
							theUser.setStatus(true);
							if ( theServer.updateRegistrationInfo(theUser) ) {
								System.out.println("\nYou are now available!");	
							} else {
								System.out.println("\nError Changing Status");
							}
						} else { 
							System.out.println("\nYou are already available!"); 
						}					            
			        } 
					else if (command.startsWith("broadcast")) {
						String[] parameter = command.split(" ", 2);
						Vector<RegistrationInfo> onlineUsers = new Vector<RegistrationInfo>(theServer.listRegisteredUsers());	
						
						for (RegistrationInfo user : onlineUsers) {
					    	if( user.getStatus() == true && !user.getUserName().equals(this.username) ) {
					    		talk(user.getHost(), user.getLocalPort(), parameter[1]);
					    	}
					    }
			        }
		        }
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.out.println("Not a valid command. Please try again.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    public void talk (String hostname, int localPort, String line) {
    	try {  
    		clientSocket = new Socket(hostname, localPort);
    		streamOut = new DataOutputStream(clientSocket.getOutputStream());
    	}
    	catch(UnknownHostException uhe) {  
    		System.out.println("Host unknown: " + uhe.getMessage());
    	}
    	catch(IOException ioe) {  
    		System.out.println("Unexpected exception: " + ioe.getMessage());
    	}
		
    	try {  
			streamOut.writeUTF(line);
			streamOut.flush();
		}
		catch (IOException ioe) {  
			System.out.println("Sending error: " + ioe.getMessage());
		}
    	
    	try {  
    		if (streamOut != null)  streamOut.close();
    		if (clientSocket != null)  clientSocket.close();
    	}
    	catch(IOException ioe) {  
    		System.out.println("Error closing ..." + ioe.getMessage());
    	}
    }   
    
	public static void main(String[] args) {
		
	    MyClient client = new MyClient(	args[0], 
	    								args[1].substring(0, args[1].indexOf(":")), 
	    								Integer.parseInt(args[1].substring( (args[1].indexOf(":")+1), args[1].length() ))   );
	    
	    if( client.rmiConnect() ) {
	    	TalkServer clientTalkServer = new TalkServer();
	    	
	    	try {
	    		client.theUser.setLocalPort(clientTalkServer.getLocalPort());
				client.theServer.updateRegistrationInfo(client.theUser);
			} catch (RemoteException e1) {
				System.out.println("Talk Port Update Error: " + e1.getMessage());
			}
	    	
	    	Thread talkThread = new Thread(clientTalkServer);
	    	Thread inputThread = new Thread(client);
	    	talkThread.start();
	    	inputThread.start();
		}
	}
}
