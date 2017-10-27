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
		String action;

		String[] menuStart = { "Membros", "Faculdades / Departamentos", "Eleicoes" };
		String[] menuStartTypes = { "Person", "Zone", "Election" };
		menus.put("Start", new Menu(menuStart, menuStartTypes));

		String[] menuPerson = { "Registar membro" };
		String[] menuPersonTypes = { "Register" };
		menus.put("Person", new Menu(menuPerson, menuPersonTypes));

		String[] menuRegister = { "Estudante", "Docente", "Funcionario" };
		String[] menuRegisterTypes = { "Student", "Teacher", "Employee" };
		menus.put("Register", new Menu(menuRegister, menuRegisterTypes));

		String[] menuZone = { "Faculdades", "Departamentos" };
		String[] menuZoneTypes = { "Faculty", "Department" };
		menus.put("Zone", new Menu(menuZone, menuZoneTypes));

		String[] menuElection = { "Eleicao Geral", "Eleicao Nucleo de Estudantes" };
		String[] menuElectionTypes = { "General", "Nucleus" };
		menus.put("Election", new Menu(menuElection, menuElectionTypes));

		String[] menuNucleus = { "Criar", "Listas", "Mesas de Voto" };
		String[] menuNucleusTypes = { "Add", "List", "VotingTable" };
		menus.put("Nucleus", new Menu(menuNucleus, menuNucleusTypes));

		String[] menuGeneral = { "Estudante", "Docente", "Funcionario" };
		String[] menuGeneralTypes = { "Student-Election", "Teacher-Election", "Employee-Election" };
		menus.put("General", new Menu(menuGeneral, menuGeneralTypes));

		menus.put("Student-Election", new Menu(menuNucleus, menuNucleusTypes));
		menus.put("Teacher-Election", new Menu(menuNucleus, menuNucleusTypes));
		menus.put("Employee-Election", new Menu(menuNucleus, menuNucleusTypes));

		String[] menuList = { "Candidatos", "Criar", "Remover" };
		String[] menuListTypes = { "Candidate", "Create", "Remove" };
		menus.put("List", new Menu(menuList, menuListTypes));

		String[] menuCandidate = { "Adicionar", "Remover", "Listar" };
		String[] menuCandidateTypes = { "Add", "Remove", "Log" };
		menus.put("Candidate", new Menu(menuCandidate, menuCandidateTypes));

		String[] menuVotingTable = { "Adicionar", "Remover" };
		String[] menuVotingTableTypes = { "Add", "Remove" };
		menus.put("VotingTable", new Menu(menuVotingTable, menuVotingTableTypes));

		String[] menuFaculty = { "Criar", "Editar", "Remover" };
		String[] menuFacultyTypes = { "Add", "Edit", "Remove" };
		menus.put("Faculty", new Menu(menuFaculty, menuFacultyTypes));
		menus.put("Department", new Menu(menuFaculty, menuFacultyTypes));

		setSecurityPolicies();
		int port = getPort(args);
		String reference = getReference(args);
		lookupRegistry(port, reference);

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

	public static void lookupRegistry(int port, String reference) {
		try {
			registry = (DataServerConsoleInterface)LocateRegistry.getRegistry(port).lookup(reference);
		} catch (Exception e1) {
			System.out.println("Remote failure. Trying to reconnect...");
			try {
				Thread.sleep(1000);
			} catch (Exception e2) {
				System.exit(0);
			}
			lookupRegistry(port, reference);
		}
	}

	public static void setSecurityPolicies() {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new SecurityManager());
	}

	public static String menu(String type, String flow) {
		int i = 0;
		int opcao = -1;
		int max = 0;
		String next = null;
		Menu menu = null;
		Scanner scanner = new Scanner(System.in);
		String line;
		String[] endings = {
			"Student", "Teacher", "Employee", "Add", "Edit", "Remove", "Create",
			"Log"
		};

		System.out.println("----------");
		if (debug) System.out.println("TYPE: " + type);

		if (Arrays.asList(endings).contains(type)) {
			flow = new String(flow + type);
			if (debug) System.out.println("FLOW: " + flow);
			return flow;
		}

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
			flow = new String(flow + next + " ");
			return menu(next, flow);
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
		String subtype = null;
		List list;
		Election election;
		ArrayList<Person> candidates;

		if (debug) System.out.println(action);

		switch (action) {
			case "Person Register Student":
			case "Person Register Teacher":
			case "Person Register Employee":
				data1 = buildPerson(actions[2]);
				break;

			case "Zone Faculty Add":
				data1 = buildFaculty();
				break;
			case "Zone Faculty Edit":
				data1 = pickFaculty();
				faculty = (Faculty)data1;
				data2 = editFaculty(faculty);
				break;
			case "Zone Faculty Remove":
				data1 = pickFaculty();
				break;

			case "Zone Department Add":
				data1 = buildDepartment(pickFaculty());
				break;
			case "Zone Department Edit":
				data1 = pickDepartment(pickFaculty());
				department = (Department) data1;
				data2 = editDepartment(department);
				break;
			case "Zone Department Remove":
				data1 = pickDepartment(pickFaculty());
				break;

			case "Election General Student-Election Add":
			case "Election General Teacher-Election Add":
			case "Election General Employee-Election Add":
			case "Election Nucleus Add":
				if (actions.length == 4) {
					subtype = actions[2].split("-")[0];
				}
				data1 = buildElection(actions[1], subtype);
				break;
			case "Election General Student-Election List Candidate Add":
			case "Election General Teacher-Election List Candidate Add":
			case "Election General Employee-Election List Candidate Add":
			case "Election General Student-Election List Candidate Remove":
			case "Election General Teacher-Election List Candidate Remove":
			case "Election General Employee-Election List Candidate Remove":
			case "Election Nucleus Candidate Add":
			case "Election Nucleus Candidate Remove":
				if (actions.length == 6) {
					subtype = actions[2].split("-")[0];
				}
				data1 = pickList(pickElection(actions[1], subtype));
				list = (List) data1;
				data2 = pickCandidate(list);
				break;
			case "Election General Student-Election List Candidate Log":
			case "Election General Teacher-Election List Candidate Log":
			case "Election General Employee-Election List Candidate Log":
			case "Election Nucleus Candidate Log":
				if (actions.length == 6) {
					subtype = actions[2].split("-")[0];
				}
				list = pickList(pickElection(actions[1], subtype));
				candidates = listCandidates(list);
				return;

			case "Election General Student-Election VotingTable Add":
			case "Election General Teacher-Election VotingTable Add":
			case "Election General Employee-Election VotingTable Add":
			case "Election Nucleus VotingTable Add":
				if (actions.length == 5) {
					subtype = actions[2].split("-")[0];
				}
				data1 = buildVotingTable(pickElection(actions[1], subtype), pickDepartment(pickFaculty()));
				break;
			case "Election General Student-Election VotingTable Remove":
			case "Election General Teacher-Election VotingTable Remove":
			case "Election General Employee-Election VotingTable Remove":
			case "Election Nucleus VotingTable Remove":
				if (actions.length == 5) {
					subtype = actions[2].split("-")[0];
				}

				data1 = pickVotingTable(pickElection(actions[1], subtype));
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

	public static VotingTable pickVotingTable(Election election) {
		ArrayList<VotingTable> votingTables = listVotingTables(election);

		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher mesa de votacao");
		System.out.println("--------------------");

		for (VotingTable votingTable : votingTables) {
			System.out.println("[" + i + "] " + votingTable.department.name);
			++i;
		}

		System.out.println("[" + i + "] Registar nova mesa votacao");

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return pickVotingTable(election);
		}

		try {
			return votingTables.get(opcao);
		} catch (Exception e) {
			if (opcao == votingTables.size()) {
				return buildVotingTable(election, pickDepartment(pickFaculty()));
			}
			System.out.println("Opcao invalida");
			return pickVotingTable(election);
		}
	}

	public static ArrayList<VotingTable> listVotingTables(Election election) {
		try {
			return registry.listVotingTables(election);
		} catch (RemoteException e1) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
			return listVotingTables(election);
		}
	}

	public static VotingTable buildVotingTable(Election election, Department department) {
		return new VotingTable(election, department);
	}

	public static Person pickCandidate(List list) {
		ArrayList<Person> candidates = listCandidates(list);
		String subtype;

		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher membro");
		System.out.println("--------------------");

		for (Person candidate : candidates) {
			System.out.println("[" + i + "] " + candidate.name);
			++i;
		}

		System.out.println("[" + i + "] Registar novo membro");

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return pickCandidate(list);
		}

		try {
			return candidates.get(opcao);
		} catch (Exception e) {
			if (opcao == candidates.size()) {
				if (list.election.subtype != null)
					return buildPerson(list.election.subtype);

				subtype = menu("Register", "");
				return buildPerson(subtype);
			}
			System.out.println("Opcao invalida");
			return pickCandidate(list);
		}
	}

	public static ArrayList<Person> listCandidates(List list) {
		try {
			return registry.listCandidates(list);
		} catch (RemoteException e1) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
			return listCandidates(list);
		}
	}

	public static List pickList(Election election) {
		ArrayList<List> lists = listLists(election);

		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher lista");
		System.out.println("--------------------");

		for (List list : lists) {
			System.out.println("[" + i + "] " + list.name);
			++i;
		}

		System.out.println("[" + i + "] Adicionar nova lista");

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return pickList(election);
		}

		try {
			return lists.get(opcao);
		} catch (Exception e) {
			if (opcao == lists.size()) {
				return buildList(election);
			}
			System.out.println("Opcao invalida");
			return pickList(election);
		}
	}

	public static ArrayList<List> listLists(Election election) {
		try {
			return registry.listLists(election);
		} catch (RemoteException e1) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
			return listLists(election);
		}
	}

	public static List buildList(Election election) {
		String name;
		Scanner scanner = new Scanner(System.in);

		System.out.println("--------------------");
		System.out.println("Criar lista");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = scanner.nextLine();

		return new List(election, name);
	}

	public static Election pickElection(String type, String subtype) {
		ArrayList<Election> elections = listElections(type, subtype);

		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher eleicao");
		System.out.println("--------------------");

		for (Election election : elections) {
			System.out.println("[" + i + "] " + election.name);
			++i;
		}

		System.out.println("[" + i + "] Adicionar nova eleicao");

		System.out.print("Opcao: ");
		line = scanner.nextLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return pickElection(type, subtype);
		}

		try {
			return elections.get(opcao);
		} catch (Exception e) {
			if (opcao == elections.size()) {
				return buildElection(type, subtype);
			}
			System.out.println("Opcao invalida");
			return pickElection(type, subtype);
		}
	}

	public static ArrayList<Election> listElections(String type, String subtype) {
		try {
			return registry.listElections(type, subtype);
		} catch (RemoteException e1) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}
			return listElections(type, subtype);
		}
	}

	public static Election buildElection(String type, String subtype) {
		String name;
	  String description;
	  Date start = null;
	  Date end = null;
	  Department department;
		Faculty faculty;

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
		boolean pass;
		String line;
		Scanner scanner = new Scanner(System.in);

		System.out.println("--------------------");
		System.out.println("Criar eleicao ");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = scanner.nextLine();

		System.out.print("Descricao: ");
		description = scanner.nextLine();

		do {
			try {
				System.out.print("Data inicio (Ex: \"10-01-2005 14:30\"): ");
				line = scanner.nextLine();
				start = dateFormat.parse(line);
				pass = true;
	    } catch(ParseException e) {
	      System.out.println("Data invalida");
				pass = false;
			}
		} while (!pass);

		do {
			try {
				System.out.print("Data fim (Ex: \"10-01-2005 14:30\"): ");
				line = scanner.nextLine();
				end = dateFormat.parse(line);
				if (end.compareTo(start) <= 0) {
					System.out.println("A data tem que ser depois de " + dateFormat.format(start));
					pass = false;
				} else pass = true;
	    } catch(ParseException e) {
	      System.out.println("Data invalida");
				pass = false;
			}
		} while (!pass);

		faculty = pickFaculty();

		department = pickDepartment(faculty);

		return new Election(name, start, end, type, subtype);
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
		boolean pass;
		String line;
		Scanner scanner = new Scanner(System.in);

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
	      System.out.println("Data invalida");
				pass = false;
			}
		} while (!pass);

		department = pickDepartment(null);

		return new Person(type, name, number, password, department, phone, address, cc, ccExpire);
	}

	public static Department pickDepartment(Faculty faculty) {
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
		try {
			return registry.listFaculties();
		} catch (RemoteException e1) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}

			return listFaculties();
		}
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
