import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;

/*
  RMI SERVER - RMI + UDP
  STATUS: WORKING
  NOTES: MISSING UDP CONNECTION
*/
public class DataServer extends UnicastRemoteObject implements RegistryInterface {

	public DataServer() throws RemoteException {
		super();
	}

  public static void main(String args[]) {
    try {
      DataServer s = new DataServer();
      Registry reg = LocateRegistry.createRegistry(7000);
      reg.rebind("iVotas", s);
      System.out.println("Server ready");
    } catch (RemoteException e) {
      System.out.println("RemoteException" + e);
    }
  }

}
