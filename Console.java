import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.lang.Thread.State;
import java.text.*;

/*
  Create worker class -> thread
*/

public class Console {
	static boolean TEST = false;
	static DataServerConsoleInterface registry;
	static LinkedList<Job> jobs = new LinkedList<Job>();
	static Worker worker = null;

	static String[] menuStart = { "Membros", "Faculdades / Departamentos", "Eleicoes" };
	static String[] menuStartTypes = { "Person", "Zone", "Election" };

	static String[] menuPerson = { "Registar membro" };
	static String[] menuPersonTypes = { "Register" };

	static String[] menuRegister = { "Estudante", "Docente", "Funcionario" };
	static String[] menuRegisterTypes = { "Student", "Teacher", "Employee" };

	public static void run(int port, String reference, int delay) {
		String action;

		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			registry = (DataServerConsoleInterface) lookupRegistry(port, reference);

			if (!TEST) {
				if (worker == null) {
					worker = new Worker(jobs, registry);
				} else {
					jobs.notify();
					worker.terminate();
					worker = new Worker(jobs, registry);
				}
				System.out.println("Admin Console ready");
				while(true) {
					action = menu("Start", "");
					executeAction(action);
				}
			} else {
				try {
					ArrayList<Faculty> faculties = registry.listFaculties();
					System.out.println(faculties);
				} catch (Exception e) {
					System.out.println("Error" + e);
				}
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

	public static String menu(String type, String flow) {
		int i = 0;
		int opcao;
		int max = 0;
		boolean exit = false;
		String next = null;
		String[] types;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("----------");

		switch (type) {
			default:
				return null;

			case "Start":
				for (String s: menuStart) {
					System.out.println("[" + i + "] " + s);
					++i;
				}
				types = menuStartTypes;
				break;

			case "Person":
				for (String s: menuPerson) {
					System.out.println("[" + i + "] " + s);
					++i;
				}
				types = menuPersonTypes;
				break;

			case "Register":
				for (String s: menuRegister) {
					System.out.println("[" + i + "] " + s);
					++i;
				}
				types = menuRegisterTypes;
				exit = true;
				break;
		}

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
			next = types[opcao];
			flow = new String(flow + next + " ");
			if (!exit) {
				 return menu(next, flow);
			}
			return flow;
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return menu(type, flow);
		}
	}

	public static void executeAction(String action) {
		Job job = null;
		String[] actions = action.split(" ");

		switch (actions[0]) {
			case "Person":
				switch (actions[1]) {
					case "Register":
						Person person = buildPerson(actions[2]);
						job = new Job("createPerson", person);
						break;
				}
				break;
		}

		if (job != null) {
			synchronized (jobs) {
				jobs.addFirst(job);
				if (worker.getState() == Thread.State.WAITING) {
					jobs.notify();
					System.out.println("WORKER NOTIFIED");
				}
			}
		}
	}

	public static Person buildPerson(String type) {
	  String name;
	  int number = -1;
	  String password;
	  Department department;
	  int phone = -1;
	  String address;
	  int cc = -1;
	  Date ccExpire = null;
		String ccExpireString;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Scanner scanner = new Scanner(System.in);
		boolean pass;

		String line;
		System.out.println("--------------------");
		System.out.println("Criar membro");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = scanner.nextLine();

		do {
			try {
				System.out.print("Numero de cartao: ");
				line = scanner.nextLine();
				number = Integer.parseInt(line);
				pass = true;
			} catch (Exception e) {
				System.out.println("Numero invalido");
				System.out.println("--------------------");
				pass = false;
			}
		} while (!pass);

		System.out.print("Password: ");
		password = scanner.nextLine();

		System.out.print("Morada: ");
		address = scanner.nextLine();

		do {
			try {
				System.out.print("Telefone: ");
				line = scanner.nextLine();
				phone = Integer.parseInt(line);
				pass = true;
			} catch (Exception e) {
				System.out.println("Telefone invalido");
				System.out.println("--------------------");
				pass = false;
			}
		} while (!pass);

		do {
			try {
				System.out.print("Cartao cidadao: ");
				line = scanner.nextLine();
				cc = Integer.parseInt(line);
				pass = true;
			} catch (Exception e) {
				System.out.println("Cartao do cidadao invalido");
				System.out.println("--------------------");
				pass = false;
			}
		} while (!pass);

		do {
			try {
				System.out.print("Data expiracao (Ex: \"10-01-2005\"): ");
				line = scanner.nextLine();
				ccExpire = dateFormat.parse(line);
				pass = true;
	    } catch(ParseException e) {
	      System.out.println("Data invalida.\nFormato: dia-mes-ano. Dois digitos para mes");
				pass = false;
			}
		} while (!pass);

		department = pickDepartment(null);

		return new Person(type, name, number, password, department, phone, address, cc, ccExpire);
	}

	public static Department pickDepartment(Faculty faculty) {
		if (faculty == null) {
			faculty = pickFaculty();
		}

		ArrayList<Department> departments = listDepartments(faculty);

		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher departmento\nFaculdade " + faculty.name);
		System.out.println("--------------------");

		for (Department department: departments) {
			System.out.println("[" + i + "] " + department.name);
			++i;
		}

		System.out.println("[" + i + "] Adicionar novo departmento");

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return pickDepartment(faculty);
		}

		try {
			return departments.get(opcao);
		} catch (Exception e) {
			if (opcao == departments.size()) {
				return buildDepartment(faculty);
			}
			System.out.println("Opcao invalida");
			return pickDepartment(faculty);
		}
	}

	public static ArrayList<Department> listDepartments(Faculty faculty) {
		try {
			return registry.listDepartments(faculty);
		} catch (RemoteException e1) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}

			return listDepartments(faculty);
		}
	}

	public static Department buildDepartment(Faculty faculty) {
		String name;
		Scanner scanner = new Scanner(System.in);

		System.out.println("--------------------");
		System.out.println("Criar departmento\nFaculdade " + faculty.name);
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = scanner.nextLine();

		return new Department(faculty, name);
	}

	public static ArrayList<Faculty> listFaculties() {
		ArrayList<Faculty> faculties = null;

		try {
			faculties = registry.listFaculties();
		} catch (Exception e) {
			System.out.println("listFaculties" + e + faculties.getClass().getName());
			return listFaculties();
		}

		return faculties;
	}

	public static Faculty pickFaculty() {
		ArrayList<Faculty> falculties = listFaculties();
		String line;

		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);

		System.out.println("--------------------");
		System.out.println("Escolher faculdade");
		System.out.println("--------------------");

		for (Faculty faculty: falculties) {
			System.out.println("[" + i + "] " + faculty.name);
			++i;
		}

		System.out.println("[" + i + "] Adicionar nova faculdade");

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return pickFaculty();
		}

		try {
			return falculties.get(opcao);
		} catch (Exception e) {
			if (opcao == falculties.size()) {
				return buildFaculty();
			}
			System.out.println("Opcao invalida");
			return pickFaculty();
		}
	}

	public static Faculty buildFaculty() {
		String name;
		Scanner scanner = new Scanner(System.in);

		System.out.println("--------------------");
		System.out.println("Criar faculdade");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = scanner.nextLine();

		return new Faculty(name);
	}

	public static void main(String args[]) {
		setSecurityPolicies();
		int port = getPort(args);;
		String reference = getReference(args);
		run(port, reference, 0);
	}

}
