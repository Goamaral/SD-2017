import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.lang.Thread.State;
import java.text.*;

public class Console {
	static boolean debug = true;
	static DataServerConsoleInterface registry;
	static LinkedList<Job> jobs = new LinkedList<Job>();
	static ConsoleWorker worker = null;
	static Hashtable<String, Menu> menus = new Hashtable<String, Menu>();

	public static void main(String args[]) {
		setSecurityPolicies();
		int port = getPort(args);;
		String reference = getReference(args);

		String[] menuStart = { "Membros", "Faculdades / Departamentos", "Eleicoes" };
		String[] menuStartTypes = { "Person", "Zone", "Election" };
		menus.put("Start", new Menu(menuStart, menuStartTypes));

		String[] menuPerson = { "Registar membro" };
		String[] menuPersonTypes = { "Register" };
		menus.put("Person", new Menu(menuPerson, menuPersonTypes));

		String[] menuRegister = { "Estudante", "Docente", "Funcionario" };
		String[] menuRegisterTypes = { "Student", "Teacher", "Employee" };
		menus.put("Register", new Menu(menuRegister, menuRegister));

		// DOING
		String[] menuZone = { "Faculdades", "Departamentos" };
		String[] menuZoneTypes = { "Faculty", "Department" };
		menus.put("Zone", new Menu(menuZone, menuZoneTypes));

		String[] menuFaculty = { "Adicionar", "Editar", "Remover" };
		String[] menuFacultyTypes = { "Add", "Edit", "Remove" };
		menus.put("Faculty", new Menu(menuFaculty, menuFacultyTypes));
		menus.put("Department", new Menu(menuFaculty, menuFacultyTypes));

		run(port, reference, 0);
	}

	public static void run(int port, String reference, int delay) {
		String action;

		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			System.exit(0);
		}

		try {
			registry = (DataServerConsoleInterface) lookupRegistry(port, reference);

			if (worker == null) {
				worker = new ConsoleWorker(jobs, registry);
			} else {
				jobs.notify();
				worker.terminate();
				worker = new ConsoleWorker(jobs, registry);
			}
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
		int opcao = -1;
		int max = 0;
		boolean exit = false;
		String next = null;
		Menu menu = null;
		Scanner scanner = new Scanner(System.in);
		String line;
		String[] endings = { "Register", "Faculty", "Department" };

		System.out.println("----------");
		if (debug) System.out.println("TYPE: " + type);

		exit = Arrays.asList(endings).contains(type);

		menu = menus.get(type);

		for (String s: menu.menu) {
			System.out.println("[" + i + "] " + s);
			++i;
		}

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
			next = menu.types[opcao];
			if (!exit) {
				flow = new String(flow + next + " ");
				return menu(next, flow);
			}
			flow = new String(flow + next);
			if (debug) System.out.println("END: " + next);
			return flow;
		} catch (Exception e) {
			if (debug) System.out.println(e);
			System.out.println("Opcao invalida");
			return menu(type, flow);
		}
	}

	public static void executeAction(String action) {
		Job job = null;
		String[] actions = action.split(" ");
		Object data1 = null;
		Object data2 = null;
		Faculty faculty;
		Department department;

		if (debug) System.out.println(action);

		switch (actions[0]) {
			case "Person":
				switch (actions[1]) {
					case "Register":
						data1 = buildPerson(actions[2]);
						break;
				}
				break;
			case "Zone":
				switch (actions[1]) {
					case "Faculty":
						switch (actions[2]) {
							case "Add":
								data1 = buildFaculty();
								break;
							case "Edit":
								faculty = pickFaculty();
								data1 = faculty;
								data2 = editFaculty(faculty);
								if (data2 == null) return;
								break;
							case "Remove":
								data1 = pickFaculty();
								break;
						}
					break;

					case "Department":
						switch (actions[2]) {
							case "Add":
								faculty = pickFaculty();
								data1 = buildDepartment(faculty);
								break;
							case "Edit":
								faculty = pickFaculty();
								department = pickDepartment(faculty);
								data1 = department;
								data2 = editDepartment(department);
								if (data2 == null) return;
								break;
							case "Remove":
								faculty = pickFaculty();
								data1 = pickDepartment(faculty);
								break;
						}
					break;
				}
				break;
		}

		if (data1 != null) {
			if (data2 == null) {
				job = new Job(action, data1);
			} else job = new Job(action, data1, data2);

			synchronized (jobs) {
				jobs.addFirst(job);
				if (worker.getState() == Thread.State.WAITING) {
					jobs.notify();
				}
			}
		}
	}

	public static Department editDepartment(Department department) {
		Scanner scanner = new Scanner(System.in);
		String line;
		String name = department.name;
		Faculty faculty = department.faculty;

		System.out.println("----------");
		System.out.println("Nome: " + department.name);
		System.out.print("Editar? [s/N]: ");

		line = scanner.nextLine();
		if (line.equals("s")) {
			System.out.print("Novo nome: ");
			name = scanner.nextLine();
		}

		System.out.println("Faculdade: " + department.faculty.name);
		System.out.print("Editar? [s/N]: ");

		line = scanner.nextLine();
		if (line.equals("s")) {
			faculty = editFaculty(department.faculty);
		}

		return new Department(faculty, name);
	}

	public static Faculty editFaculty(Faculty faculty) {
		Scanner scanner = new Scanner(System.in);
		String line;
		String name = faculty.name;

		System.out.println("----------");
		System.out.println("Nome: " + faculty.name);
		System.out.print("Editar? [s/N]: ");

		line = scanner.nextLine();
		if (line.equals("s")) {
			System.out.print("Novo nome: ");
			name = scanner.nextLine();
		}

		return new Faculty(name);
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

}
