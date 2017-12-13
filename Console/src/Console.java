import Core.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

public class Console {
    private static DataServerInterface registry;
    private static Hashtable<String, Menu> menuTable = new Hashtable<>();
    private static int port;
    private static String reference;
    private static SimpleDateFormat electionDateFormat = new SimpleDateFormat("dd-MM-yyyy k:m");
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public static void main(String args[]) {
        String[] menuStart = {"Membros", "Faculdades / Departamentos", "Eleicoes"};
        String[] menuStartTypes = {"Core.DataServerInterface.Person", "Zone", "Core.Election"};
        menuTable.put("Start", new Menu(menuStart, menuStartTypes));

        String[] menuPerson = {"Registar membro"};
        String[] menuPersonTypes = {"Register"};
        menuTable.put("Core.DataServerInterface.Person", new Menu(menuPerson, menuPersonTypes));

        String[] menuRegister = {"Estudante", "Docente", "Funcionario"};
        String[] menuRegisterTypes = {"Student", "Teacher", "Employee"};
        menuTable.put("Register", new Menu(menuRegister, menuRegisterTypes));

        String[] menuZone = {"Faculdades", "Departamentos"};
        String[] menuZoneTypes = {"Core.Faculty", "Core.Department"};
        menuTable.put("Zone", new Menu(menuZone, menuZoneTypes));

        String[] menuElection = {"Eleicao Geral", "Eleicao Nucleo de Estudantes"};
        String[] menuElectionTypes = {"General", "Nucleus"};
        menuTable.put("Core.Election", new Menu(menuElection, menuElectionTypes));

        String[] menuNucleus = {"Criar", "Listas", "Mesas de Voto", "Resultados"};
        String[] menuNucleusTypes = {"Add", "Core.VotingList", "Core.VotingTable", "Results"};
        menuTable.put("Nucleus", new Menu(menuNucleus, menuNucleusTypes));

        String[] menuGeneral = {"Estudante", "Docente", "Funcionario"};
        String[] menuGeneralTypes = {"Student-Core.Election", "Teacher-Core.Election", "Employee-Core.Election"};
        menuTable.put("General", new Menu(menuGeneral, menuGeneralTypes));

        menuTable.put("Student-Core.Election", new Menu(menuNucleus, menuNucleusTypes));
        menuTable.put("Teacher-Core.Election", new Menu(menuNucleus, menuNucleusTypes));
        menuTable.put("Employee-Core.Election", new Menu(menuNucleus, menuNucleusTypes));

        String[] menuVotingList = {"Candidatos", "Criar", "Remover"};
        String[] menuVotingListTypes = {"Candidate", "Create", "Remove"};
        menuTable.put("Core.VotingList", new Menu(menuVotingList, menuVotingListTypes));

        String[] menuCandidate = {"Adicionar", "Remover", "Listar"};
        String[] menuCandidateTypes = {"Add", "Remove", "Log"};
        menuTable.put("Candidate", new Menu(menuCandidate, menuCandidateTypes));

        String[] menuVotingTable = {"Adicionar", "Remover"};
        String[] menuVotingTableTypes = {"Add", "Remove"};
        menuTable.put("Core.VotingTable", new Menu(menuVotingTable, menuVotingTableTypes));

        String[] menuFaculty = {"Criar", "Editar", "Remover"};
        String[] menuFacultyTypes = {"Add", "Edit", "Remove"};
        menuTable.put("Core.Faculty", new Menu(menuFaculty, menuFacultyTypes));
        menuTable.put("Core.Department", new Menu(menuFaculty, menuFacultyTypes));

        setSecurityPolicies();
        port = getPort(args);
        reference = getReference(args);
        run();
    }

    private static void run() {
        String action;

        lookupRegistry(port, reference);

        System.out.println("Consola de Administracao");
        System.out.println("RMI => localhost:" + port + "\\" + reference);

        //noinspection InfiniteLoopStatement
        while (true) {
            action = menu("Start", "");
            if (action != null) {
                executeAction(action);
            }
        }
    }

    private static int getPort(String args[]) {
        try {
            return Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return 7000;
        }
    }

    private static String getReference(String args[]) {
        try {
            return args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            return "iVotas";
        }
    }

    private static void lookupRegistry(int port, String reference) {
        boolean failed = false;

        try {
            registry = (DataServerInterface) LocateRegistry.getRegistry(port).lookup(reference);
        } catch (RemoteException | NotBoundException e) {
            failed = true;
        }

        if (failed) {
            System.out.println("Falha remota. A tentar novamente... " + reference + ":" + port);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }

            lookupRegistry(port, reference);
        }
    }

    private static void setSecurityPolicies() {
        System.getProperties().put("java.security.policy", "policy.all");
        System.setSecurityManager(new SecurityManager());
    }

    private static String menu(String type, String flow) throws NumberFormatException, ArrayIndexOutOfBoundsException {
        int i = 0;
        int opcao;
        String next;
        Menu menu;
        String line;
        String[] endings = {
                "Student", "Teacher", "Employee", "Add", "Edit", "Remove", "Create",
                "Log", "Results"
        };

        System.out.println("----------");

        if (Arrays.asList(endings).contains(type)) {
            return flow.substring(0, flow.length() - 1);
        }

        menu = menuTable.get(type);

        for (String s : menu.menu) {
            System.out.println("[" + i + "] " + s);
            ++i;
        }

        System.out.print("Opcao: ");

        line = System.console().readLine();

        opcao = Integer.parseInt(line);
        next = menu.types[opcao];
        flow += next + " ";
        return menu(next, flow);
    }

    private static void executeAction(String action) {
        String[] actions = action.split(" ");
        String subtype, departmentName;
        int votingListID, personCC, electionID;

        switch (action) {
            case "Core.DataServerInterface.Person Register Student":
            case "Core.DataServerInterface.Person Register Teacher":
            case "Core.DataServerInterface.Person Register Employee":
                buildPerson(actions[2]);
                break;
            case "Zone Core.Faculty Add":
                buildFaculty();
                break;
            case "Zone Core.Faculty Edit":
                editFaculty();
                break;
            case "Zone Core.Faculty Remove":
                removeFaculty(null);
                break;
            case "Zone Core.Department Add":
                buildDepartment(null);
                break;
            case "Zone Core.Department Edit":
                editDepartment();
                break;
            case "Zone Core.Department Remove":
                removeDepartment(null);
                break;
            case "Core.Election General Student-Core.Election Add":
            case "Core.Election General Teacher-Core.Election Add":
            case "Core.Election General Employee-Core.Election Add":
            case "Core.Election Nucleus Add":
                if (actions.length == 4) {
                    subtype = actions[2].split("-")[0];
                } else subtype = pickDepartment(null);

                buildElection(actions[1], subtype);
                break;
            case "Core.Election General Student-Core.Election Core.VotingList Candidate Add":
            case "Core.Election General Teacher-Core.Election Core.VotingList Candidate Add":
            case "Core.Election General Employee-Core.Election Core.VotingList Candidate Add":
            case "Core.Election General Student-Core.Election Core.VotingList Candidate Remove":
            case "Core.Election General Teacher-Core.Election Core.VotingList Candidate Remove":
            case "Core.Election General Employee-Core.Election Core.VotingList Candidate Remove":
            case "Core.Election Nucleus Core.VotingList Candidate Add":
            case "Core.Election Nucleus Core.VotingList Candidate Remove":
                if (actions.length == 6) {
                    subtype = actions[2].split("-")[0];
                } else subtype = pickDepartment(null);

                votingListID = pickVotingList(pickElection(actions[1], subtype));

                if (actions[actions.length - 1].equals("Add")) {
                    if (actions.length == 6) {
                        personCC = pickPersonByType(subtype);
                    } else personCC = pickStudentFromDepartment(subtype);

                    addCandidate(votingListID, personCC);
                } else {
                    removeCandidate(votingListID, pickCandidate(votingListID));
                }
                break;
            case "Core.Election General Student-Core.Election Core.VotingList Create":
            case "Core.Election General Teacher-Core.Election Core.VotingList Create":
            case "Core.Election General Employee-Core.Election Core.VotingList Create":
            case "Core.Election General Student-Core.Election Core.VotingList Remove":
            case "Core.Election General Teacher-Core.Election Core.VotingList Remove":
            case "Core.Election General Employee-Core.Election Core.VotingList Remove":
            case "Core.Election Nucleus Core.VotingList Create":
            case "Core.Election Nucleus Core.VotingList Remove":
                if (actions.length == 5) {
                    subtype = actions[2].split("-")[0];
                } else subtype = pickDepartment(null);

                electionID = pickElection(actions[1], subtype);

                if (actions[actions.length - 1].equals("Create")) {
                    buildVotingList(electionID);
                } else {
                    removeVotingList(pickVotingList(electionID));
                }
                break;
            case "Core.Election General Student-Core.Election Core.VotingList Candidate Log":
            case "Core.Election General Teacher-Core.Election Core.VotingList Candidate Log":
            case "Core.Election General Employee-Core.Election Core.VotingList Candidate Log":
            case "Core.Election Nucleus Core.VotingList Candidate Log":
                if (actions.length == 6) {
                    subtype = actions[2].split("-")[0];
                } else subtype = pickDepartment(null);

                logCandidates(pickVotingList(pickElection(actions[1], subtype)));
                break;
            case "Core.Election General Student-Core.Election Results":
            case "Core.Election General Teacher-Core.Election Results":
            case "Core.Election General Employee-Core.Election Results":
            case "Core.Election Nucleus Results":
                if (actions.length == 4) {
                    subtype = actions[2].split("-")[0];
                } else subtype = pickDepartment(null);

                logResults(pickElection(actions[1], subtype));
                break;
            case "Core.Election General Student-Core.Election Core.VotingTable Add":
            case "Core.Election General Teacher-Core.Election Core.VotingTable Add":
            case "Core.Election General Employee-Core.Election Core.VotingTable Add":
            case "Core.Election Nucleus Core.VotingTable Add":
                if (actions.length == 5) {
                    subtype = actions[2].split("-")[0];
                    buildVotingTable(pickElection(actions[1], subtype), pickDepartment(null));
                } else {
                    departmentName = pickDepartment(null);
                    buildVotingTable(pickElection(actions[1], departmentName), departmentName);
                }
                break;
            case "Core.Election General Student-Core.Election Core.VotingTable Remove":
            case "Core.Election General Teacher-Core.Election Core.VotingTable Remove":
            case "Core.Election General Employee-Core.Election Core.VotingTable Remove":
            case "Core.Election Nucleus Core.VotingTable Remove":
                if (actions.length == 5) {
                    subtype = actions[2].split("-")[0];
                } else subtype = pickDepartment(null);

                removeVotingTable(pickVotingTable(pickElection(actions[1], subtype)));
                break;
        }
    }

    private static void logResults(int electionID) {
        ArrayList<Result> results = listResults(electionID);

        System.out.println("--------------------\nResultados da eleicao\n--------------------");
        for (Result result : results) {
            System.out.println(result.votingListName + " " + result.votes);
        }
    }

    private static void removeVotingList(int votingListID) {
        try {
            registry.removeVotingList(votingListID);
            return;
        } catch (RemoteException remoteException) {
            System.out.println("Remocao de lista falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        removeVotingList(votingListID);
    }

    private static void logCandidates(int votingListID) {
        ArrayList<Person> candidates = listCandidates(votingListID);

        System.out.println("--------------------\nCandidato | CC\n--------------------");
        for (Person candidate : candidates) {
            System.out.println(candidate.name + " | " + candidate.cc);
        }

        if (candidates.size() == 0) System.out.println("Lista vazia");
    }

    private static void removeVotingTable(int votingTableID) {
        try {
            registry.removeVotingTable(votingTableID);
            return;
        } catch (RemoteException remoteException) {
            System.out.println("Remocao de mesa de voto falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        removeVotingTable(votingTableID);
    }

    private static void removeCandidate(int votingListID, int personCC) {
        try {
            registry.removeCandidate(votingListID, personCC);
            return;
        } catch (RemoteException remoteException) {
            System.out.println("Remocao de candidato falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        removeCandidate(votingListID, personCC);
    }

    private static int pickStudentFromDepartment(String departmentName) {
        ArrayList<Person> students = listStudentsFromDepartment(departmentName);
        int i = 0, opcao;
        String line;
        Person ret;

        System.out.println("--------------------");
        System.out.println("Escolher estudante do departamento de " + departmentName);
        System.out.println("--------------------");

        for (Person student : students) {
            System.out.println("[" + i + "] " + student.name + " " + student.number);
            ++i;
        }

        System.out.println("[" + i + "] Registar novo estudante");

        System.out.print("Opcao: ");
        line = System.console().readLine();

        try {
            opcao = Integer.parseInt(line);
        } catch (NumberFormatException nfe) {
            System.out.println("Opcao invalida");
            return pickStudentFromDepartment(departmentName);
        }

        ret = students.get(opcao);
        if (ret == null) {
            if (opcao == students.size()) {
                return buildStudentFromDepartment(departmentName);
            }
            System.out.println("Opcao invalida");
            return pickStudentFromDepartment(departmentName);
        } else {
            return ret.cc;
        }
    }

    private static int buildStudentFromDepartment(String departmentName) {
        String name;
        int number;
        String password;
        int phone;
        String address;
        int cc;
        Date ccExpire;

        System.out.println("--------------------");
        System.out.println("Criar estudante do departamento de " + departmentName);
        System.out.println("--------------------");

        System.out.print("Nome: ");
        name = System.console().readLine();

        number = getCardNumber();

        System.out.print("Password: ");
        password = System.console().readLine();

        System.out.print("Morada: ");
        address = System.console().readLine();

        phone = getPhone();

        cc = getCC();

        ccExpire = getDate();

        return createPerson(
                new Person(
                        cc, "Student", name, password, address,
                        number, phone, dateFormat.format(ccExpire), departmentName
                )
        );
    }

    private static Date getDate() {
        String line;
        boolean pass;
        Date ccExpire = null;

        do {
            try {
                System.out.print("Data expiracao (Ex: \"10-01-2005\"): ");
                line = System.console().readLine();
                ccExpire = dateFormat.parse(line);
                pass = true;
            } catch (ParseException pe) {
                System.out.println("Data invalida");
                pass = false;
            }
        } while (!pass);

        return ccExpire;
    }

    private static int getCC() {
        String line;
        boolean pass;
        int cc = -1;

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

        return cc;
    }

    private static int getPhone() {
        String line;
        boolean pass;
        int phone = -1;

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

        return phone;
    }

    private static int getCardNumber() {
        String line;
        boolean pass;
        int number = -1;

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

        return number;
    }

    private static ArrayList<Person> listStudentsFromDepartment(String departmentName) {
        try {
            return registry.listStudentsFromDepartment(departmentName);
        } catch (RemoteException remoteException) {
            System.out.println("Obtencao de estudantes de um departamento falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listStudentsFromDepartment(departmentName);
    }

    private static int pickPersonByType(String type) {
        ArrayList<Person> people = listPeopleOfType(type);
        int i = 0, opcao;
        String line;

        System.out.println("--------------------");
        System.out.println("Escolher membro");
        System.out.println("--------------------");

        for (Person person : people) {
            System.out.println("[" + i + "] " + person.name + " " + person.number);
            ++i;
        }

        System.out.println("[" + i + "] Registar novo membro");

        System.out.print("Opcao: ");
        line = System.console().readLine();

        try {
            opcao = Integer.parseInt(line);
        } catch (NumberFormatException nfe) {
            System.out.println("Opcao invalida");
            return pickPersonByType(type);
        }

        try {
            return people.get(opcao).cc;
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            if (opcao == people.size()) {
                return buildPerson(type);
            }
            System.out.println("Opcao invalida");
            return pickPersonByType(type);
        }
    }

    private static ArrayList<Person> listPeopleOfType(String type) {
        try {
            return registry.listPeopleOfType(type);
        } catch (RemoteException remoteException) {
            System.out.println("Obtencao de um tipo de pessoas falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listPeopleOfType(type);
    }

    private static void addCandidate(int votingListID, int personCC) {
        try {
            if (registry.addCandidate(votingListID, personCC) == -1) {
                System.out.println("Candidato ja existente!");
            }
            return;
        } catch (RemoteException remoteException) {
            System.out.println("Adicao de candidato falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        addCandidate(votingListID, personCC);
    }

    private static void removeDepartment(String name) {
        if (name == null) {
            name = pickDepartment(null);
        }

        try {
            registry.removeDepartment(name);
            return;
        } catch (RemoteException remoteException) {
            System.out.println("Remocao de departamento falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        removeDepartment(name);
    }

    private static void removeFaculty(String name) {
        if (name == null) {
            name = pickFaculty();
        }

        try {
            registry.removeFaculty(name);
            return;
        } catch (RemoteException remoteException) {
            System.out.println("Remocao de faculdade falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        removeFaculty(name);
    }

    private static ArrayList<Result> listResults(int electionID) {
        try {
            return registry.getResults(electionID);
        } catch (RemoteException remoteException) {
            System.out.println("Falha na obtencao de resultados de eleicao.\nA tentar novamente novamente ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listResults(electionID);
    }

    private static int pickVotingTable(int electionID) {
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

    private static ArrayList<VotingTable> listVotingTables(int electionID) {
        try {
            return registry.listVotingTables(electionID);
        } catch (RemoteException re) {
            System.out.println("Obtencao de mesas de voto falhada.\nA tentar novamente novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listVotingTables(electionID);
    }

    private static int buildVotingTable(int electionID, String departmentName) {
        return createVotingTable(
                new VotingTable(electionID, departmentName)
        );
    }

    private static int createVotingTable(VotingTable votingTable) {
        try {
            return registry.createVotingTable(votingTable);
        } catch (RemoteException remoteException) {
            System.out.println("Criacao de mesa de voto falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return createVotingTable(votingTable);
    }

    private static int pickCandidate(int votingListID) {
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
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            if (opcao == candidates.size()) {
                election = getElection(votingListID);
                if (!election.subtype.equals(""))
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

    private static Election getElection(int id) {
        try {
            return registry.getElection(id);
        } catch (RemoteException remoteException) {
            System.out.println("Obtencao de eleicao falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return getElection(id);
    }

    private static ArrayList<Person> listCandidates(int votingListID) {
        try {
            return registry.listCandidates(votingListID);
        } catch (RemoteException re) {
            System.out.println("Falha na obtencao dos candidatos de uma lista.\nA tentar novamente novamente ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listCandidates(votingListID);
    }

    private static int pickVotingList(int electionID) {
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
        } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
            if (opcao == votingLists.size()) {
                return buildVotingList(electionID);
            } else {
                System.out.println("Opcao invalida");
                return pickVotingList(electionID);
            }
        }
    }

    private static ArrayList<VotingList> listVotingLists(int electionID) {
        try {
            return registry.listVotingLists(electionID);
        } catch (RemoteException re) {
            System.out.println("Obtencao de listas falhada.\nA tentar novamente novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listVotingLists(electionID);
    }

    private static int buildVotingList(int electionID) {
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

    private static int createVotingList(VotingList votingList) {
        try {
            return registry.createVotingList(votingList);
        } catch (RemoteException remoteException) {
            System.out.println("Criacao de lista falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return createVotingList(votingList);
    }

    private static int pickElection(String type, String subtype) {
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

    private static ArrayList<Election> listElections(String type, String subtype) {
        try {
            return registry.listElections(type, subtype);
        } catch (RemoteException re) {
            System.out.println("Obtencao de eleicoes falhada.\nA tentar novamente novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listElections(type, subtype);
    }

    private static int buildElection(String type, String subtype) {
        String name;
        String description;
        Date start = null;
        Date end;
        String started_at = null;
        String ended_at = null;

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
                start = electionDateFormat.parse(line);
                started_at = electionDateFormat.format(start);
                pass = true;
            } catch (ParseException pe) {
                System.out.println("Data invalida");
                pass = false;
            }
        } while (!pass);

        do {
            try {
                System.out.print("Data fim (Ex: \"10-01-2005 14:30\"): ");
                line = System.console().readLine();
                end = electionDateFormat.parse(line);
                if (end.compareTo(start) <= 0) {
                    System.out.println("A data tem que ser depois de " + electionDateFormat.format(start));
                    pass = false;
                } else pass = true;
                ended_at = electionDateFormat.format(end);
            } catch (ParseException e) {
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
                        started_at,
                        ended_at
                )
        );
    }

    private static int createElection(Election election) {
        try {
            return registry.createElection(election);
        } catch (RemoteException remoteException) {
            System.out.println("Criacao de eleicao falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return createElection(election);
    }

    private static void editDepartment() {
        String line, newName, name, facultyName, newFacultyName;
        facultyName = pickFaculty();
        newFacultyName = facultyName;
        name = pickDepartment(facultyName);
        newName = name;

        System.out.println("----------");
        System.out.println("Nome: " + name);
        System.out.print("Editar? [s/N]: ");

        line = System.console().readLine();
        if (line.equals("s")) {
            System.out.print("Novo nome: ");
            newName = System.console().readLine();
        }

        System.out.println("Faculdade: " + facultyName);
        System.out.print("Editar? [s/N]: ");

        line = System.console().readLine();
        if (line.equals("s")) {
            newFacultyName = pickFaculty();
        }

        updateDepartment(
                new Department(name, facultyName),
                new Department(newName, newFacultyName)
        );
    }

    private static void updateDepartment(Department department, Department newDepartment) {
        try {
            registry.updateDepartment(department, newDepartment);
            return;
        } catch (RemoteException remoteException) {
            System.out.println("Edicao de departamento falhada. A tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        updateDepartment(department, newDepartment);
    }

    private static void editFaculty() {
        String line;
        String newName = null;

        System.out.println("----------");
        System.out.println("Nome: " + null);
        System.out.print("Editar? [s/N]: ");

        line = System.console().readLine();
        if (line.equals("s")) {
            System.out.print("Novo nome: ");
            newName = System.console().readLine();
        }

        updateFaculty(
                new Faculty(null),
                new Faculty(newName)
        );
    }

    private static String updateFaculty(Faculty faculty, Faculty newFaculty) {
        try {
            return registry.updateFaculty(faculty, newFaculty);
        } catch (RemoteException remoteException) {
            System.out.println("Atualizacao da faculdade falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return updateFaculty(faculty, newFaculty);
    }

    private static int buildPerson(String type) {
        String name;
        int number;
        String password;
        String departmentName;
        int phone;
        String address;
        int cc;
        Date ccExpire;

        System.out.println("--------------------");
        System.out.println("Criar membro");
        System.out.println("--------------------");

        System.out.print("Nome: ");
        name = System.console().readLine();

        number = getCardNumber();

        System.out.print("Password: ");
        password = System.console().readLine();

        System.out.print("Morada: ");
        address = System.console().readLine();

        phone = getPhone();

        cc = getCC();

        ccExpire = getDate();

        departmentName = pickDepartment(null);

        return createPerson(
                new Person(
                        cc, type, name, password, address,
                        number, phone, dateFormat.format(ccExpire), departmentName
                )
        );
    }

    private static int createPerson(Person person) {
        try {
            return registry.createPerson(person);
        } catch (RemoteException remoteException) {
            System.out.println("Insercao de pessoa falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return createPerson(person);
    }

    private static String pickDepartment(String facultyName) {
        if (facultyName == null) facultyName = pickFaculty();

        ArrayList<Department> departments = listDepartments(facultyName);
        Department ret;

        int i = 0;
        int opcao;
        String line;

        System.out.println("--------------------");
        System.out.println("Escolher departmento");
        System.out.println("--------------------");

        for (Department department : departments) {
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
        } catch (IndexOutOfBoundsException ioobBoundsException) {
            if (opcao == departments.size()) {
                return buildDepartment(facultyName);
            }
            System.out.println("Opcao invalida");
            return pickDepartment(facultyName);
        }

        return ret.name;
    }

    private static ArrayList<Department> listDepartments(String facultyName) {
        try {
            return registry.listDepartments(facultyName);
        } catch (RemoteException re) {
            System.out.println("Obtencao de departamentos falhada.\nA tentar novamente novamente ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listDepartments(facultyName);
    }

    private static String buildDepartment(String facultyName) {
        String name;

        if (facultyName == null) {
            facultyName = pickFaculty();
        }

        System.out.println("--------------------");
        System.out.println("Criar departmento na faculdade " + facultyName);
        System.out.println("--------------------");

        System.out.print("Nome: ");
        name = System.console().readLine();

        return createDepartment(new Department(name, facultyName));
    }

    private static String createDepartment(Department department) {
        try {
            return registry.createDepartment(department);
        } catch (RemoteException remoteException) {
            System.out.println("Criacao de departamento falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return createDepartment(department);
    }

    private static ArrayList<Faculty> listFaculties() {
        try {
            return registry.listFaculties();
        } catch (RemoteException re) {
            System.out.println("Falha na ligacao ao servidor.\nA tentar novamente novamente ...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return listFaculties();
    }

    private static String pickFaculty() {
        ArrayList<Faculty> falculties = listFaculties();
        String line;
        Faculty ret;

        int i = 0;
        int opcao;

        System.out.println("--------------------");
        System.out.println("Escolher faculdade");
        System.out.println("--------------------");

        for (Faculty faculty : falculties) {
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

    private static String buildFaculty() {
        String name;

        System.out.println("--------------------");
        System.out.println("Criar faculdade");
        System.out.println("--------------------");

        System.out.print("Nome: ");
        name = System.console().readLine();

        return createFaculty(name);
    }

    private static String createFaculty(String name) {
        try {
            return registry.createFaculty(name);
        } catch (RemoteException remoteException) {
            System.out.println("Criacao de faculdade falhada\nA tentar novamente...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                System.exit(0);
            }
        }

        return createFaculty(name);
    }

}
