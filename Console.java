import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.lang.Thread.State;

/*
  Create worker class -> thread
	Job (string instruction, object data)
*/

public class Console {
	static DataServerConsoleInterface registry;
	static Scanner scanner = new Scanner(System.in);
	synchronized static LinkedList<Job> jobs = new LinkedList<Job>();

	static String[] menuStart = { "Membros", "Faculdades / Departamentos", "Eleicoes" };
	static String[] menuStartTypes = { "Person", "Zone", "Election" };

	static String[] menuPerson = { "Registar membro" };
	static String[] menuPersonTypes = { "Register" };

	static String[] menuRegister = { "Estudante", "Docente", "Funcionario" };

	public static void run(int port, String reference, int delay) {
		String action;

		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			registry = (DataServerConsoleInterface) lookupRegistry(port, reference);
			System.out.println("Admin Console ready");
			while(true) {
				action = menu("Start", "");
				executeAction(action);
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

		System.out.println("----------");

		switch (type) {
			case "Start":
				max = menuStart.length;
				for (String s: menuStart) {
					System.out.println("[" + i + "] " + s);
					++i;
				}
				types = menuStartTypes;
				break;
			case "Person":
				max = menuPerson.length;
				for (String s: menuPerson) {
					System.out.println("[" + i + "] " + s);
					++i;
				}
				types = menuPersonTypes;
				break;
			case "Register":
				max = menuRegister.length;
				for (String s: menuRegister) {
					System.out.println("[" + i + "] " + s);
					++i;
				}
				types = menuRegister;
				exit = true;
				break;
			default:
				return null;
		}

		System.out.print("Opcao: ");
		opcao = scanner.nextInt();

		if (opcao >= 0 && opcao <= max) {
			next = types[opcao];
			flow = new String(flow + " " + next);
			if (!exit) {
				 return menu(next, flow);
			}
			return flow;
		} else {
			System.out.println("Opcao invalida");
			return menu(type, flow);
		}
	}

	public static void executeAction(String action) {
		Job job;
		System.out.println(action);
		switch (action) {
			default:
				return;
			case "Person Register Estudante":
				Person person = buildPerson();
				job = new Job("createPerson", person);
				break;
		}

		synchronized (jobs) {
			jobs.addFirst(job);
			System.out.println(worker.getState());
			if (worker.getState() == Thread.State.BLOCKED) {
				worker.sem.notify();
			}
		}
	}

	public static Person buildPerson(String type) {
	  String name;
	  int id;
	  String password;
	  Department department;
	  int phone;
	  String address;
	  int cc;
	  Date ccExpire;
		String ccExpireString;
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

		System.out.println("--------------------");
		System.out.println("Criar membro");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = scanner.nextLine();

		System.out.print("Id: ");
		id = scanner.nextInt();

		System.out.print("Password: ");
		password = scanner.nextLine();

		System.out.print("Morada: ");
		address = scanner.nextLine();

		System.out.print("Telefone: ");
		phone = scanner.nextInt();

		System.out.print("Cartao cidadao: ");
		cc = scanner.nextInt();

		while(true)
			System.out.print("Data expiracao (Ex: "10-01-2005"): ");
			ccExpireString = scanner.nextInt();

			try {
	     	ccExpire = dateFormat.parse(ccExpireString);
	    } catch(ParseException e) {
	      System.out.println("Data invalida.\nFormato: dia-mes-ano. Dois digitos para mes");
			}
		}

		department = pickDepartment();

		return new Person(type, name, id, password, department, phone, address, cc, ccExpire);
	}

	public static Department pickDepartment(Faculty faculty) {
		if (faculty == null) {
			faculty = pickFaculty();
		}

		Department[] departments = registry.listDepartments(faculty);
		int i = 0;
		int opcao;

		System.out.println("--------------------");
		System.out.println("Escolher departmento\nFaculdade " + faculty.name);
		System.out.println("--------------------");

		for (Department department: departments) {
			System.out.println("[" + i + "] " + department.name);
			++i;
		}

		System.out.println("[" + i + "] Adicionar novo departmento");

		System.out.print("Opcao: ");
		opcao = scanner.nextInt();

		if (opcao >= 0 && opcao <= departments.length) {
			return falculties[opcao];
		} else if (opcao == departments.length + 1) {
			return buildDepartment(faculty);
		} else {
			System.out.println("Opcao invalida");
			return pickDepartment(faculty);
		}
	}

	public static Department buildDepartment(Faculty faculty) {
		String name;

		System.out.println("--------------------");
		System.out.println("Criar departmento\nFaculdade " + faculty.name);
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = scanner.nextLine();

		return new Department(faculty, name);
	}

	public static Faculty pickFaculty() {
		Faculty[] falculties = registry.listFaculties();
		int i = 0;
		int opcao;

		System.out.println("--------------------");
		System.out.println("Escolher faculdade");
		System.out.println("--------------------");

		for (Faculty faculty: falculties) {
			System.out.println("[" + i + "] " + faculty.name);
			++i;
		}

		System.out.println("[" + i + "] Adicionar nova faculdade");

		System.out.print("Opcao: ");
		opcao = scanner.nextInt();

		if (opcao >= 0 && opcao <= falculties.length) {
			return falculties[opcao];
		} else if (opcao == falculties.length + 1) {
			return buildFaculty();
		} else {
			System.out.println("Opcao invalida");
			return pickFaculty();
		}
	}

	public static Faculty buildFaculty() {
		String name;

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
