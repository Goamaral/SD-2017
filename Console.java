import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

/*
  ADMIN CONSOLE - RMI + POLICY FILE
  STATUS: WORKING
  NOTES:
*/

public class Console {
	static String[] menuStart = { "Membros", "Faculdades / Departamentos", "Eleicoes" };
	static String[] menuStartTypes = { "Person", "Zone", "Election" };

	public static void run(int port, String reference, int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			DataServerConsoleInterface reg = (DataServerConsoleInterface) lookupRegistry(port, reference);
			System.out.println("Admin Console ready");
			while(true) {
				menu("start");
			}
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

	public static void menu(String type) {
		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		int max = 0;

		switch (type) {
			case "start":
				max = menuStart.length;
				for (String s: menuStart) {
					System.out.println("[" + i + "] " + s);
					++i;
				}
			break;
		}

		System.out.print("Opcao: ");
		opcao = scanner.nextInt();
		System.out.println("----------");

		if (opcao >= 0 && opcao <= max) {
			menu(menuStartTypes[opcao]);
		} else {
			menu(type);
		}
	}

	public static void main(String args[]) {
		setSecurityPolicies();
		int port = getPort(args);;
		String reference = getReference(args);
		run(port, reference, 0);
	}

}
