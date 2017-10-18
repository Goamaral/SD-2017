import java.rmi.*;
import java.rmi.server.*;
import java.net.*;
import java.rmi.registry.*;

/*
  RMI SERVER - RMI + UDP
  STATUS: WORKING
*/

public class DataServer extends UnicastRemoteObject implements RegistryInterface {
	public static void run(Integer port, String reference, DataServer s, Integer delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			// TODO: Missing UDP connection

			Registry reg = (Registry) createAndBindRegistry(port, reference, s);

			System.out.println("Server ready Port: " + port + " Reference: " + reference);
			while(true);
		} catch (RemoteException e) {
			System.out.println("Remote failure. Trying to reconnect...");
			run(port, reference, s, 1000);
		}
	}

	public static Integer getPort(String args[]) {
		try {
			return Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return 7000;
		} catch (NumberFormatException e) {
			return 7000;
		}
	}

	public static String getReference(String args[]) {
		try {
			return args[2].toString();
		} catch (ArrayIndexOutOfBoundsException e) {
			return "iVotas";
		}
	}

	public static Registry createAndBindRegistry(Integer port, String reference, DataServer s) throws RemoteException {
		Registry reg = LocateRegistry.createRegistry(port);
		reg.rebind(reference, s);
		return reg;
	}

	public DataServer() throws RemoteException {
		super();
	}

	public static void main(String args[]) {
		DataServer s = null;

		try {
			s = new DataServer();
		} catch (RemoteException e) {
			System.out.println(e);
			System.exit(-1);
		}

		Integer port = getPort(args);;
		String reference = getReference(args);
		run(port, reference, s, 0);
	}

}
