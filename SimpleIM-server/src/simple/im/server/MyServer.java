package simple.im.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import simple.im.common.PresenceService;
import simple.im.common.RegistrationInfo;

/**
 * Simple "server" wrapper that calls the Presence Service.
 * 
 * @author Niko Solihin
 *
 */
public class MyServer extends UnicastRemoteObject implements PresenceService {

	private Vector<RegistrationInfo> registeredUsers = new Vector<RegistrationInfo>();	
	
	protected MyServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}		
	
    /**
     * Register a client with the presence service.
     * @param reg The information that is to be registered about a client.
     * @return true if the user was successfully registered, or false if somebody
     * the given name already exists in the system.
     */	
	public boolean register(RegistrationInfo reg) throws RemoteException {
		return registeredUsers.add(reg);
	}

    /**
     * Updates the information of a currently registered client. 
     * @param reg The updated registration info. 
     * @return true if successful, or false if no user with the given
     * name is registered.
     * 
     */
	public boolean updateRegistrationInfo(RegistrationInfo reg) throws RemoteException {	
		RegistrationInfo result = lookup(reg.getUserName());
		result.setStatus(reg.getStatus());
		result.setLocalPort(reg.getLocalPort());
		int userIndex = registeredUsers.indexOf(result);
		registeredUsers.setElementAt(result, userIndex);
		return true;
	} 


    /**
     * Unregister a client from the presence service.  Client must call this
     * method when it terminates execution.
     * @param userName The name of the user to be unregistered.
     */
	public void unregister(String userName) throws RemoteException {
		registeredUsers.removeElement(this.lookup(userName));
	}

    /**
     * Lookup the registration information of another client.
     * @name The name of the client that is to be located.
     * @return The RegistrationInfo info for the client, or null if
     * no such client was found.
     */
	public RegistrationInfo lookup(String name) throws RemoteException {
		RegistrationInfo result = null;
		for (RegistrationInfo user : registeredUsers) {
	    	if( user.getUserName().equals(name) ) {
	    		result = user;
	    	}
	    }
		return result;
	}
	
    /**
     * Determine all users who are currently registered in the system.
     * @return An array of RegistrationInfo objects - one for each client
     * present in the system.
     */	
	public Vector<RegistrationInfo> listRegisteredUsers() throws RemoteException {
		return this.registeredUsers;
	}	
	
	public static void main(String[] args) {
		try {
			Registry r = LocateRegistry.getRegistry();
			r.bind("myserver", new MyServer());
			System.out.println("The Presence Service is ready!");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
