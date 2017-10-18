import java.rmi.*;
import java.rmi.registry.LocateRegistry;

/*
  TCP SERVER - RMI + TCP
  STATUS: WORKING
  NOTES: MISSING TCP CONNECTIONS
*/

public class Server {

	public static void main(String args[]) {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new SecurityManager());

		try {
			RegistryInterface reg = (RegistryInterface) LocateRegistry.getRegistry(7000).lookup("iVotas");
      System.out.println("TCP Server ready");

			while(true);
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
