package simple.rmi.common;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Abstract interface definition for the server.
 * 
 * @author J. Engelsma (http://themobilemontage.com)
 *
 */
public interface ComputePi extends Remote{
	BigDecimal computePi(Pi val) throws RemoteException;
}
