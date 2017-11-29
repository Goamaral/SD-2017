import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.text.*;

import com.sun.org.apache.bcel.internal.generic.RETURN;

public class Console {
	static boolean debug = true;
	static DataServerInterface registry;
	static Hashtable<String, Menu> menuTable = new Hashtable<String, Menu>();
	static int port;
	static String reference;
	static SimpleDateFormat electionDateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
	static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

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
			System.out.println("Falha remota. A tentar novamente... " + reference + ":" + port);
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
		String[] actions = action.split(" ");
		String subtype;
		int electionID, votingListID, personCC;

		if (debug) System.out.println("ACTION " + action);
				
		if ("Person Register Student".equals(action)
			|| "Person Register Teacher".equals(action)
			|| "Person Register Employee".equals(action)
		) {
			buildPerson(actions[2]);
		}
		else if ("Zone Faculty Add".equals(action)) {
			buildFaculty();
		}
		else if ("Zone Faculty Edit".equals(action)) {
			editFaculty(null);
		}
		else if ("Zone Faculty Remove".equals(action)) {
			removeFaculty(null);
		}
		else if ("Zone Department Add".equals(action)) {
			buildDepartment(null);
		}
		else if ("Zone Department Edit".equals(action)) {
			editDepartment(null);
		}
		else if ("Zone Department Remove".equals(action)) {
			removeDepartment(null);
		}
		else if ("Election General Student-Election Add".equals(action)
			|| "Election General Teacher-Election Add".equals(action)
			|| "Election General Employee-Election Add".equals(action)
			|| "Election Nucleus Add".equals(action)
		) {
			if (actions.length == 4) {
				subtype = actions[2].split("-")[0];
			} else subtype = pickDepartment(null);

			buildElection(actions[1], subtype);
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
			} else subtype = pickDepartment(null);
			
			electionID = pickElection(actions[1], subtype);
			votingListID = pickVotingList(electionID);
			personCC = pickPersonElection(electionID);
			
			addCandidate(votingListID, personCC);
		}
		else if ("Election General Student-Election List Create".equals(action)
			|| "Election General Teacher-Election List Create".equals(action)
			|| "Election General Employee-Election List Create".equals(action)
		) {
			subtype = actions[2].split("-")[0];
			data1 = buildList(pickElection(actions[1], subtype));
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
				return null;
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
				return null;
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
	}
	
	public static void addCandidate(int votingListID, int personCC) {
		try {
			if (registry.addCandidate(votingListID, personCC) == -1) {
				System.out.println("Candidato ja existente!");
			}
			return;
		} catch(RemoteException remoteException) {
			System.out.println("Adicao de candidato falhada\nA tentar novamente...");
		}
		
		addCandidate(votingListID, personCC);
	}
	
	public static void removeDepartment(String name) {
		if (name == null) {
			name = pickDepartment(null);
		}
		
		try {
			registry.removeDepartment(name);
			return;
		} catch(RemoteException remoteException) {
			System.out.println("Remocao de departamento falhada\nA tentar novamente...");
		}
		removeDepartment(name);
	}
	
	public static void removeFaculty(String name) {
		if (name == null) {
			name = pickFaculty();
		}
		
		try {
			registry.removeFaculty(name);
			return;
		} catch(RemoteException remoteException) {
			System.out.println("Remocao de faculdade falhada\nA tentar novamente...");
		}
		removeFaculty(name);
	}

	public static void listResults(int electionID) {
		ArrayList<Result> results = null;

		try {
			results = registry.getResults(electionID);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			listResults(electionID);
		}
		
		for (Result result : results) {
			System.out.println(result.votingListName + ": " + result.votes);
		}
	}

	public static int pickVotingTable(int electionID) {
		ArrayList<VotingTable> votingTables = listVotingTables(electionID);
		VotingTable ret;
		
		int i = 0;
		int opcao;
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher mesa de votacao");
		System.out.println("--------------------");

		for (VotingTable votingTable : votingTables) {
			System.out.println("[" + i + "] " + votingTable.departmentName);
			++i;
		}

		System.out.println("[" + i + "] Registar nova mesa votacao");

		System.out.print("Opcao: ");
		line = System.console().readLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickVotingTable(electionID);
		}

		ret = votingTables.get(opcao);
		if (ret == null) {
			if (opcao == votingTables.size()) {
				return buildVotingTable(electionID, pickDepartment(null));
			}
			System.out.println("Opcao invalida");
			return pickVotingTable(electionID);
		} else {
			return ret.id;
		}
	}

	public static ArrayList<VotingTable> listVotingTables(int electionID) {
		try {
			return registry.listVotingTables(electionID);
		} catch (RemoteException re) {
			System.out.println("Obtencao de mesas de voto falhada.\nA tentar novamente novamente...");
			return listVotingTables(electionID);
		}
	}

	public static int buildVotingTable(int electionID, String departmentName) {
		return createVotingTable(
		  new VotingTable(electionID, departmentName)
		);
	}
	
	public static int createVotingTable(VotingTable votingTable) {
		int ret = -1;
		
		try {
			ret = registry.createVotingTable(votingTable);
		} catch (RemoteException remoteException) {
			System.out.println("Criacao de mesa de voto falhada\nA tentar novamente...");
			createVotingTable(votingTable);
		}
		
		return ret;
	}

	public static int pickCandidate(int votingListID) {
		ArrayList<Person> candidates = listCandidates(votingListID);
		String subtype;
		Person ret;
		Election election;

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
			return pickCandidate(votingListID);
		}

		try {
			ret = candidates.get(opcao);
		} catch(IndexOutOfBoundsException indexOutOfBoundsException) {
			if (opcao == candidates.size()) {
				election = getElection(votingListID);
				if (election.subtype != "")
					return buildPerson(election.subtype);
	
				subtype = menu("Register", "");
				return buildPerson(subtype);
			} else {
				System.out.println("Opcao invalida");
				return pickCandidate(votingListID);
			}
		}
		
		return ret.cc;
	}
	
	public static Election getElection(int id) {
		Election ret = null;
		
		try {
			ret = registry.getElection(id);
		} catch(RemoteException remoteException) {
			System.out.println("Obtencao de eleicao falhada\nA tentar novamente...");
		}
		
		return ret;
	}

	public static ArrayList<Person> listCandidates(int votingListID) {
		try {
			return registry.listCandidates(votingListID);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}
			return listCandidates(votingListID);
		}
	}

	public static int pickVotingList(int electionID) {
		ArrayList<VotingList> votingLists = listVotingLists(electionID);
		
		int i = 0;
		int opcao;
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher lista");
		System.out.println("--------------------");

		for (VotingList votingList : votingLists) {
			System.out.println("[" + i + "] " + votingList.name);
			++i;
		}

		System.out.println("[" + i + "] Adicionar nova lista");

		System.out.print("Opcao: ");
		line = System.console().readLine();

		try {
			opcao = Integer.parseInt(line);
		} catch (NumberFormatException nfe) {
			System.out.println("Opcao invalida");
			return pickVotingList(electionID);
		}

		try {
			return votingLists.get(opcao).id;
		} catch(IndexOutOfBoundsException indexOutOfBoundsException) {
			if (opcao == votingLists.size()) {
				return buildVotingList(electionID);
			} else {
				System.out.println("Opcao invalida");
				return pickVotingList(electionID);
			}
		}		
	}

	public static ArrayList<VotingList> listVotingLists(int electionID) {
		try {
			return registry.listVotingLists(electionID);
		} catch (RemoteException re) {
			System.out.println("Obtencao de listas falhada.\nA tentar novamente novamente...");
			return listVotingLists(electionID);
		}
	}

	public static int buildVotingList(int electionID) {
		String name;

		System.out.println("--------------------");
		System.out.println("Criar lista");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();
				
		return createVotingList(
		  new VotingList(electionID, name)
		);
	}
	
	public static int createVotingList(VotingList votingList) {
		int ret = -1;
		
		try {
			ret = registry.createVotingList(votingList);
		} catch (RemoteException remoteException) {
			System.out.println("Criacao de lista falhada\nA tentar novamente...");
			createVotingList(votingList);
		}
		
		return ret;
	}

	public static int pickElection(String type, String subtype) {
		ArrayList<Election> elections = listElections(type, subtype);
				
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

		try {
			return elections.get(opcao).id;
		} catch (IndexOutOfBoundsException iOfBoundsException) {
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
		} catch (RemoteException re) {
			System.out.println("Obtencao de eleicoes falhada.\nA tentar novamente novamente...");
		}
		return listElections(type, subtype);
	}

	public static int buildElection(String type, String subtype) {
		String name;
		String description;
		Date start = null;
		Date end = null;

		boolean pass;
		String line;

		System.out.println("--------------------");
		System.out.println("Criar eleicao ");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();

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
		
		return createElection(
		  new Election(
		    name,
			description,
			type, 
			subtype, 
			electionDateFormat.format(start), 
			electionDateFormat.format(end)
		  )
		);
	}
	
	public static int createElection(Election election) {		
		try {
			return registry.createElection(election);
		} catch (RemoteException remoteException) {
			System.out.println("Criacao de eleicao falhada\nA tentar novamente...");
		}
		
		return createElection(election);
	}

	public static Department editDepartment(Department department) {
		String line;
		String name = department.name;
		String facultyName = department.facultyName;

		System.out.println("----------");
		System.out.println("Nome: " + department.name);
		System.out.print("Editar? [s/N]: ");

		line = System.console().readLine();
		if (line.equals("s")) {
			System.out.print("Novo nome: ");
			name = System.console().readLine();
		}

		System.out.println("Faculdade: " + department.facultyName);
		System.out.print("Editar? [s/N]: ");

		line = System.console().readLine();
		if (line.equals("s")) {
			facultyName = pickFaculty();
		}
		
		return new Department(name, facultyName);
	}

	public static String editFaculty(String name) {
		if (name == null) {
			name = pickFaculty();
		}
		
		String line;
		String newName = name;

		System.out.println("----------");
		System.out.println("Nome: " + name);
		System.out.print("Editar? [s/N]: ");

		line = System.console().readLine();
		if (line.equals("s")) {
			System.out.print("Novo nome: ");
			newName = System.console().readLine();
		}
		
		return updateFaculty(
			new Faculty(name),
			new Faculty(newName)
		);
	}
	
	public static String updateFaculty(Faculty faculty, Faculty newFaculty) {
		try {
			return registry.updateFaculty(faculty, newFaculty);
		} catch(RemoteException remoteException) {
			System.out.println("Atualizacao da faculdade falhada\nA tentar novamente...");
		}
		return updateFaculty(faculty, newFaculty);
	}

	public static int buildPerson(String type) {
		String name;
		int number = -1;
		String password;
		String departmentName;
		int phone = -1;
		String address;
		int cc = -1;
		Date ccExpire = null;
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

		departmentName = pickDepartment(null);
				
		return createPerson(
			new Person(
			  cc, type, name, password, address,
			  number, phone, dateFormat.format(ccExpire), departmentName
			)
		);
	}
	
	public static int createPerson(Person person) {
		int ret = -1;
		
		try {
			ret = registry.createPerson(person);
		} catch(RemoteException remoteException) {
			System.out.println("Insercao de pessoa falhada\nA tentar novamente...");
			createPerson(person);
		}
		
		return ret;
	}

	public static String pickDepartment(String facultyName) {
		ArrayList<Department> departments = listDepartments(facultyName);
		Department ret;
		
		int i = 0;
		int opcao;
		String line;

		System.out.println("--------------------");
		System.out.println("Escolher departmento");
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
			return pickDepartment(facultyName);
		}
		
		try {
			ret = departments.get(opcao);
		} catch(IndexOutOfBoundsException ioobBoundsException) {
			if (opcao == departments.size()) {
				return buildDepartment(facultyName);
			}
			System.out.println("Opcao invalida");
			return pickDepartment(facultyName);
		}
		
		return ret.name;
	}

	public static ArrayList<Department> listDepartments(String facultyName) {
		if (facultyName == null) {
			facultyName = pickFaculty();
		}
				
		try {
			 return registry.listDepartments(facultyName);
		} catch (RemoteException re) {
			System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				System.exit(0);
			}

			return listDepartments(facultyName);
		}
	}

	public static String buildDepartment(String facultyName) {
		String name;
		
		if (facultyName == null) {
			facultyName = pickFaculty();
		}

		System.out.println("--------------------");
		System.out.println("Criar departmento\nFaculdade " + facultyName);
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();
		
		return createDepartment(new Department(name, facultyName));		
	}
	
	public static String createDepartment(Department department) {
		try {
			return registry.createDepartment(department);
		} catch (RemoteException remoteException) {
			System.out.println("Criacao de departamento falhada\nA tentar novamente...");
		}
		
		return createDepartment(department);
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

	public static String pickFaculty() {
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
		
		try {
			ret = falculties.get(opcao);
		} catch (IndexOutOfBoundsException iOfBoundsException) {
			if (opcao == falculties.size()) {
				return buildFaculty();
			}
			System.out.println("Opcao invalida");
			return pickFaculty();
		}

		return ret.name;
	}

	public static String buildFaculty() {
		String name;

		System.out.println("--------------------");
		System.out.println("Criar faculdade");
		System.out.println("--------------------");

		System.out.print("Nome: ");
		name = System.console().readLine();
		
		return createFaculty(
		  new Faculty(name)
		);
	}
	
	public static String createFaculty(Faculty faculty) {
		String ret = null;
		
		try {
			ret = registry.createFaculty(faculty);
		} catch (RemoteException remoteException) {
			System.out.println("Criacao de faculdade falhada\nA tentar novamente...");
		}
		
		return ret;
	}

}
