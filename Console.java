import java.rmi.*;
import java.rmi.registry.LocateRegistry;

/*
  ADMIN CONSOLE - RMI + POLICY FILE
  STATUS: WORKING
  NOTES:
*/

public class Console {

	public static void main(String args[]) {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new SecurityManager());

		try {
			RegistryInterface reg = (RegistryInterface) LocateRegistry.getRegistry(7000).lookup("iVotas");
      System.out.println("Admin console ready");

			while(true);
		} catch (Exception e) {
			System.out.println(e);
		}

	}

}
