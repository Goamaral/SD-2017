import java.rmi.*;
import java.rmi.registry.LocateRegistry;

/*
  TCP SERVER - RMI + TCP
  STATUS: WORKING
  NOTES: MISSING TCP CONNECTIONS
*/

public class Server {
	public static void run(int port, String reference, int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			DataServerServerInterface reg = (DataServerServerInterface) lookupRegistry(port, reference);
			System.out.println("TCP Server ready");
			while(true);
		} catch (RemoteException e) {
			System.out.println("Remote failure. Trying to reconnect...");
			run(port, reference, 1000);
		} catch (NotBoundException e) {
			System.out.println("Remote failure. Trying to reconnect...");
			run(port, reference, 1000);
		}
	}

	public static int getPort(String args[]) {
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

	public static Remote lookupRegistry(int port, String reference) throws RemoteException, NotBoundException {
		return LocateRegistry.getRegistry(port).lookup(reference);
	}

	public static void setSecurityPolicies() {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new SecurityManager());
	}

	public static void main(String args[]) {
		setSecurityPolicies();
		int port = getPort(args);;
		String reference = getReference(args);
		run(port, reference, 0);
	}

}
