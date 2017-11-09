import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.text.*;

public class Console {
	static boolean debug = false;
	static DataServerInterface registry;
	static LinkedList<Job> jobs = new LinkedList<Job>();
	static ConsoleWorker worker = null;
	static Hashtable<String, Menu> menuTable = new Hashtable<String, Menu>();
	static int port;
	static String reference;

	public static void main(String args[]) {
		String[] menuStart = { "Membros", "Faculdades / Departamentos", "Eleicoes" };
		String[] menuStartTypes = { "Person", "Zone", "Election" };
		menuTable.put("Start", new Menu(menuStart, menuStartTypes));

		String[] menuPerson = { "Registar membro" };
		String[] menuPersonTypes = { "Register" };
		menuTable.put("Person", new Menu(menuPerson, menuPersonTypes));

		String[] menuRegister = { "Estudante", "Docente", "Funcionario" };
		String[] menuRegisterTypes = { "Student", "Teacher", "Employee" };
		menuTable.put("Register", new Menu(menuRegister, menuRegisterTypes));

		String[] menuZone = { "Faculdades", "Departamentos" };
		String[] menuZoneTypes = { "Faculty", "Department" };
		menuTable.put("Zone", new Menu(menuZone, menuZoneTypes));

		String[] menuElection = { "Eleicao Geral", "Eleicao Nucleo de Estudantes" };
		String[] menuElectionTypes = { "General", "Nucleus" };
		menuTable.put("Election", new Menu(menuElection, menuElectionTypes));

		String[] menuNucleus = { "Criar", "Listas", "Mesas de Voto", "Resultados" };
		String[] menuNucleusTypes = { "Add", "List", "VotingTable", "Results" };
		menuTable.put("Nucleus", new Menu(menuNucleus, menuNucleusTypes));

		String[] menuGeneral = { "Estudante", "Docente", "Funcionario" };
		String[] menuGeneralTypes = { "Student-Election", "Teacher-Election", "Employee-Election" };
		menuTable.put("General", new Menu(menuGeneral, menuGeneralTypes));

		menuTable.put("Student-Election", new Menu(menuNucleus, menuNucleusTypes));
		menuTable.put("Teacher-Election", new Menu(menuNucleus, menuNucleusTypes));
		menuTable.put("Employee-Election", new Menu(menuNucleus, menuNucleusTypes));

		String[] menuList = { "Candidatos", "Criar", "Remover" };
		String[] menuListTypes = { "Candidate", "Create", "Remove" };
		menuTable.put("List", new Menu(menuList, menuListTypes));

		String[] menuCandidate = { "Adicionar", "Remover", "Listar" };
		String[] menuCandidateTypes = { "Add", "Remove", "Log" };
		menuTable.put("Candidate", new Menu(menuCandidate, menuCandidateTypes));

		String[] menuVotingTable = { "Adicionar", "Remover" };
		String[] menuVotingTableTypes = { "Add", "Remove" };
		menuTable.put("VotingTable", new Menu(menuVotingTable, menuVotingTableTypes));

		String[] menuFaculty = { "Criar", "Editar", "Remover" };
		String[] menuFacultyTypes = { "Add", "Edit", "Remove" };
		menuTable.put("Faculty", new Menu(menuFaculty, menuFacultyTypes));
		menuTable.put("Department", new Menu(menuFaculty, menuFacultyTypes));
		
		setSecurityPolicies();
		port = getPort(args);
		reference = getReference(args);
		run();
	}
	
	public static void run() {
		String action;

		lookupRegistry(port, reference);

		if (worker == null) {
			worker = new ConsoleWorker(jobs, registry);
		} else {
			jobs.notify();
			worker.terminate();
			worker = new ConsoleWorker(jobs, registry);
		}
		System.out.println("Consola de Administracao");
		System.out.println("RMI => localhost:" + port + "\\" + reference);

		while(true) {
			action = menu("Start", "");
			System.out.println("Pre-action: " + action);
			executeAction(action);
		}
	}

	public static int getPort(String args[]) {
		try {
			return Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException aioobe) {
			return 7000;
		} catch (NumberFormatException nfe) {
			return 7000;
		}
	}

	public static String getReference(String args[]) {
		try {
			return args[2].toString();
		} catch (ArrayIndexOutOfBoundsException aiooBoundsException) {
			return "iVotas";
		}
	}

	public static void lookupRegistry(int port, String reference) {
		boolean failed = false;;
		
		try {
			registry = (DataServerInterface)LocateRegistry.getRegistry(port).lookup(reference);
		} catch (RemoteException re) {
			failed = true;
		} catch (NotBoundException notBoundException) {
			failed = true;
		}
		
		if (failed) {
			System.out.println("Remote failure. Trying to reconnect... " + reference + ":" + port);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
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
		String next = null;
		Menu menu = null;
		String line = null;
		String[] endings = {
			"Student", "Teacher", "Employee", "Add", "Edit", "Remove", "Create",
			"Log", "Results"
		};
		boolean failed;

		System.out.println("----------");
		if (debug) System.out.println("TYPE: " + type);

		if (Arrays.asList(endings).contains(type)) {
			if (debug) System.out.println("FLOW: " + flow);
			return flow.substring(0, flow.length()-1);
		}

		menu = menuTable.get(type);

		for (String s: menu.menu) {
			System.out.println("[" + i + "] " + s);
			++i;
		}

		System.out.print("Opcao: ");
		
		line = System.console().readLine();		

		try {
			opcao = Integer.parseInt(line);
			next = menu.types[opcao];
			flow = new String(flow + next + " ");
			return menu(next, flow);
		} catch (NumberFormatException numberFormatException) {
			failed = true;
		} catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
			failed = true;
		}
		
		if (failed) {
			System.out.println("Opcao invalida");
			return menu(type, flow);
		}
		
		return null;
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

		if (debug) System.out.println("ACTION " + action);
				
		if ("Person Register Student".equals(action)
			|| "Person Register Teacher".equals(action)
			|| "Person Register Employee".equals(action)
		) {
			data1 = buildPerson(actions[2]);
		}
		else if ("Zone Faculty Add".equals(action)) {
			data1 = buildFaculty();
		}
		else if ("Zone Faculty Edit".equals(action)) {
			data1 = pickFaculty();
			faculty = (Faculty)data1;
			data2 = editFaculty(faculty);
		}
		else if ("Zone Faculty Edit".equals(action)) {
			data1 = pickFaculty();
		}
		else if ("Zone Faculty Remove".equals(action)) {
			data1 = buildDepartment(pickFaculty());
		}
		else if ("Zone Department Add".equals(action)) {
			data1 = pickDepartment(pickFaculty());
			department = (Department) data1;
			data2 = editDepartment(department);
		}
		else if ("Zone Department Remove".equals(action)) {
			data1 = pickDepartment(pickFaculty());
		}
		else if ("Election General Student-Election Add".equals(action)
			|| "Election General Teacher-Election Add".equals(action)
			|| "Election General Employee-Election Add".equals(action)
			|| "Election Nucleus Add".equals(action)
		) {
			if (actions.length == 4) {
				subtype = actions[2].split("-")[0];
			} else {
				subtype = pickDepartment(pickFaculty()).name;
			}
			data1 = buildElection(actions[1], subtype);
		}
		else if ("Election General Student-Election List Candidate Add".equals(action)
			|| "Election General Teacher-Election List Candidate Add".equals(action)
			|| "Election General Employee-Election List Candidate Add".equals(action)
			|| "Election General Student-Election List Candidate Remove".equals(action)
			|| "Election General Teacher-Election List Candidate Remove".equals(action)
			|| "Election General Employee-Election List Candidate Remove".equals(action)
			|| "Election Nucleus Candidate Add".equals(action)
			|| "Election Nucleus Candidate Remove".equals(action)
		) {
			if (actions.length == 6) {
				subtype = actions[2].split("-")[0];
			}
			data1 = pickList(pickElection(actions[1], subtype));
			list = (List) data1;
			data2 = pickCandidate(list);
		}
		else if ("Election General Student-Election List Candidate Log".equals(action)
			|| "Election General Teacher-Election List Candidate Log".equals(action)
			|| "Election General Employee-Election List Candidate Log".equals(action)
			|| "Election Nucleus Candidate Log".equals(action)
		) {
				if (actions.length == 6) {
					subtype = actions[2].split("-")[0];
				}
				list = pickList(pickElection(actions[1], subtype));
				listCandidates(list);
				return;
		}
		else if ("Election General Student-Election Results".equals(action)
			|| "Election General Teacher-Election Results".equals(action)
			|| "Election General Employee-Election Results".equals(action)
			|| "Election Nucleus Results".equals(action)
		) {
				if (actions.length == 4) {
					subtype = actions[2].split("-")[0];
				}
				election = pickElection(actions[1], subtype);
				listResults(election);
				return;
		}
		else if ("Election General Student-Election VotingTable Add".equals(action)
			|| "Election General Teacher-Election VotingTable Add".equals(action)
			|| "Election General Employee-Election VotingTable Add".equals(action)
			|| "Election Nucleus VotingTable Add".equals(action)
		) {
			if (actions.length == 5) {
				subtype = actions[2].split("-")[0];
			}
			data1 = buildVotingTable(pickElection(actions[1], subtype), pickDepartment(pickFaculty()));
		}
		else if ("Election General Student-Election VotingTable Remove".equals(action)
			|| "Election General Teacher-Election VotingTable Remove".equals(action)
			|| "Election General Employee-Election VotingTable Remove".equals(action)
			|| "Election Nucleus VotingTable Remove".equals(action)
		) {
			if (actions.length == 5) {
				subtype = actions[2].split("-")[0];
			}

			data1 = pickVotingTable(pickElection(actions[1], subtype));
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

	public static void listResults(Election election) {
		Hashtable<String, Integer> result = null;

		try {
			result = registry.getResults(election);
			for (String key : result.keySet()) {
				System.out.println(key + ": " + result.get(key));
			}
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}
			listResults(election);
			return;
		}
	}

	public static VotingTable pickVotingTable(Election election) {
		ArrayList<VotingTable> votingTables = listVotingTables(election);
		VotingTable ret;
		
		int i = 0;
		int opcao;
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
		line = System.console().readLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickVotingTable(election);
		}

		ret = votingTables.get(opcao);
		if (ret == null) {
			if (opcao == votingTables.size()) {
				return buildVotingTable(election, pickDepartment(pickFaculty()));
			}
			System.out.println("Opcao invalida");
			return pickVotingTable(election);
		} else {
			return ret;
		}
	}

	public static ArrayList<VotingTable> listVotingTables(Election election) {
		try {
			return registry.listVotingTables(election);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
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
		Person ret;

		int i = 0;
		int opcao;
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
		line = System.console().readLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickCandidate(list);
		}

		ret = candidates.get(opcao);
		if (ret == null) {
			if (opcao == candidates.size()) {
				if (list.election.subtype != null)
					return buildPerson(list.election.subtype);

				subtype = menu("Register", "");
				return buildPerson(subtype);
			}
			System.out.println("Opcao invalida");
			return pickCandidate(list);
		} 
	
		return ret;
	}

	public static ArrayList<Person> listCandidates(List list) {
		try {
			return registry.listCandidates(list);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}
			return listCandidates(list);
		}
	}

	public static List pickList(Election election) {
		ArrayList<List> lists = listLists(election);
		List ret;
		
		int i = 0;
		int opcao;
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
		line = System.console().readLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickList(election);
		}

		ret = lists.get(opcao);
		if (ret == null) {
			if (opcao == lists.size()) {
				return buildList(election);
			}
			System.out.println("Opcao invalida");
			return pickList(election);
		}
		
		return ret;
	}

	public static ArrayList<List> listLists(Election election) {
		try {
			return registry.listLists(election);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}
			return listLists(election);
		}
	}

	public static List buildList(Election election) {
		String name;

		System.out.println("--------------------");
		System.out.println("Criar lista");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();
		
		return new List(election, name);
	}

	public static Election pickElection(String type, String subtype) {
		ArrayList<Election> elections = listElections(type, subtype);
		Election ret;
		
		int i = 0;
		int opcao;
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
		line = System.console().readLine();
		
		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickElection(type, subtype);
		}

		ret = elections.get(opcao);
		if (ret == null) {
			if (opcao == elections.size()) {
				return buildElection(type, subtype);
			}
			System.out.println("Opcao invalida");
			return pickElection(type, subtype);	
		}
		
		return ret;
	}

	public static ArrayList<Election> listElections(String type, String subtype) {
		try {
			return registry.listElections(type, subtype);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
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

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
		boolean pass;
		String line;

		System.out.println("--------------------");
		System.out.println("Criar eleicao ");
		System.out.println("--------------------");

		String[] menuElection = { "Eleicao Geral", "Eleicao Nucleo de Estudantes" };
		String[] menuElectionTypes = { "General", "Nucleus" };

		int index = Arrays.binarySearch(menuElectionTypes, type);

		name = new String(menuElection[index]);

		System.out.print("Descricao: ");
		description = System.console().readLine();

		do {
			try {
				System.out.print("Data inicio (Ex: \"10-01-2005 14:30\"): ");
				line = System.console().readLine();
				start = dateFormat.parse(line);
				pass = true;
	    } catch(ParseException pe) {
	      System.out.println("Data invalida");
				pass = false;
			}
		} while (!pass);

		do {
			try {
				System.out.print("Data fim (Ex: \"10-01-2005 14:30\"): ");
				line = System.console().readLine();
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
		
		return new Election(name, description, start, end, type, subtype);
	}

	public static Department editDepartment(Department department) {
		String line;
		String name = department.name;
		Faculty faculty = department.faculty;

		System.out.println("----------");
		System.out.println("Nome: " + department.name);
		System.out.print("Editar? [s/N]: ");

		line = System.console().readLine();
		if (line.equals("s")) {
			System.out.print("Novo nome: ");
			name = System.console().readLine();
		}

		System.out.println("Faculdade: " + department.faculty.name);
		System.out.print("Editar? [s/N]: ");

		line = System.console().readLine();
		if (line.equals("s")) {
			faculty = editFaculty(department.faculty);
		}
		
		return new Department(faculty, name);
	}

	public static Faculty editFaculty(Faculty faculty) {
		String line;
		String name = faculty.name;

		System.out.println("----------");
		System.out.println("Nome: " + faculty.name);
		System.out.print("Editar? [s/N]: ");

		line = System.console().readLine();
		if (line.equals("s")) {
			System.out.print("Novo nome: ");
			name = System.console().readLine();
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
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		boolean pass;
		String line;

		System.out.println("--------------------");
		System.out.println("Criar membro");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();

		do {
			try {
				System.out.print("Numero de cartao: ");
				line = System.console().readLine();
				number = Integer.parseInt(line);
				pass = true;
			} catch (NumberFormatException nfe) {
				System.out.println("Numero invalido");
				System.out.println("--------------------");
				pass = false;
			}
		} while (!pass);

		System.out.print("Password: ");
		password = System.console().readLine();

		System.out.print("Morada: ");
		address = System.console().readLine();

		do {
			try {
				System.out.print("Telefone: ");
				line = System.console().readLine();
				phone = Integer.parseInt(line);
				pass = true;
			} catch (NumberFormatException nfe) {
				System.out.println("Telefone invalido");
				System.out.println("--------------------");
				pass = false;
			}
		} while (!pass);

		do {
			try {
				System.out.print("Cartao cidadao: ");
				line = System.console().readLine();
				cc = Integer.parseInt(line);
				pass = true;
			} catch (NumberFormatException nfe) {
				System.out.println("Cartao do cidadao invalido");
				System.out.println("--------------------");
				pass = false;
			}
		} while (!pass);

		do {
			try {
				System.out.print("Data expiracao (Ex: \"10-01-2005\"): ");
				line = System.console().readLine();
				ccExpire = dateFormat.parse(line);
				pass = true;
	    } catch(ParseException pe) {
	      System.out.println("Data invalida");
				pass = false;
			}
		} while (!pass);

		department = pickDepartment(null);
		
		return new Person(type, name, number, password, department, phone, address, cc, ccExpire);
	}

	public static Department pickDepartment(Faculty faculty) {
		ArrayList<Department> departments = listDepartments(faculty);
		Department ret;
		
		int i = 0;
		int opcao;
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
		line = System.console().readLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickDepartment(faculty);
		}

		ret = departments.get(opcao);
		if (ret == null) {
			if (opcao == departments.size()) {
				return buildDepartment(faculty);
			}
			System.out.println("Opcao invalida");
			return pickDepartment(faculty);
		}
		
		return ret;
	}

	public static ArrayList<Department> listDepartments(Faculty faculty) {
		try {
			return registry.listDepartments(faculty);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}

			return listDepartments(faculty);
		}
	}

	public static Department buildDepartment(Faculty faculty) {
		String name;

		System.out.println("--------------------");
		System.out.println("Criar departmento\nFaculdade " + faculty.name);
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();

		return new Department(faculty, name);
	}

	public static ArrayList<Faculty> listFaculties() {
		try {
			return registry.listFaculties();
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}

			return listFaculties();
		}
	}

	public static Faculty pickFaculty() {
		ArrayList<Faculty> falculties = listFaculties();
		String line;
		Faculty ret;
		
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
		line = System.console().readLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickFaculty();
		}

		ret = falculties.get(opcao);
		if (ret == null) {
			if (opcao == falculties.size()) {
				return buildFaculty();
			}
			System.out.println("Opcao invalida");
			return pickFaculty();
		}
		return ret;
	}

	public static Faculty buildFaculty() {
		String name;

		System.out.println("--------------------");
		System.out.println("Criar faculdade");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();

		return new Faculty(name);
	}

}
