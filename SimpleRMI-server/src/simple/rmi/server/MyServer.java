package simple.rmi.server;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import simple.rmi.common.ComputePi;
import simple.rmi.common.Pi;

/**
 * Simple "server" wrapper that calls the Pi computation in-process.
 * 
 * @author Jonathan Engelsma (http://themobilemontage.com)
 *
 */
public class MyServer extends UnicastRemoteObject implements ComputePi {

	protected MyServer() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	public BigDecimal computePi(Pi val) throws RemoteException {
		return val.execute();
	}
	
	public static void main(String[] args) {
		try {
			Registry r = LocateRegistry.getRegistry();
			r.bind("myserver", new MyServer());
			System.out.println("The PI Computer is ready for action!");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
