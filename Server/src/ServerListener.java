import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.ArrayList;

class ServerListener extends Thread {
	DataServerInterface registry;
	private ServerSocket listenSocket;
	int portRMI;
	String referenceRMI;
	private int portTCP;
	final ArrayList<TerminalConnection> terminals = new ArrayList<>();
	String departmentName;
	RmiNapper rmiNapper;
	//String ipRMI;

	ServerListener(int portRMI, String referenceRMI, /*String ipRMI,*/ int portTCP) {
		this.portRMI = portRMI;
		this.referenceRMI = referenceRMI;
		this.portTCP = portTCP;
		this.rmiNapper = new RmiNapper(this);

		this.start();
	}

	public void run() {
		int terminalID;
		Socket terminalSocket;
		lookupRegistry();
		createServerSocket();

		departmentName = getLocation();

		System.out.println("Mesa de voto");
		System.out.println("RMI => localhost:" + portRMI + "\\" + referenceRMI);
		System.out.println("TCP => localhost:" + portTCP);

		new VotingTableAutentication(this.rmiNapper, this);

		//noinspection InfiniteLoopStatement
		while(true) {
			terminalSocket = waitForRequest();

			synchronized (terminals) {
			  terminalID = terminals.size();
			  terminals.add(new TerminalConnection(this, terminalID, terminalSocket));
			  System.out.println("Novo terminal (" + terminalID + ") conectado");
			}
		}
	}

	private String getLocation() {
		ArrayList<Faculty> faculties = listFaculties();
		ArrayList<Department> departments;
		ArrayList<String> list = new ArrayList<>();
		int opcao;
		String facultyName;

		if (faculties == null) {
			System.out.println("Nao exitem faculdades registadas para"
				+ " indicar a localizacao da mesa de voto");
			System.exit(0);
		}

		for (Faculty auxFaculty : faculties) {
			list.add(auxFaculty.name);
		}

		opcao = Utility.selector(list, "Selecione a faculdade onde se situa");

		facultyName = faculties.get(opcao).name;

		departments = listDepartments(facultyName);

		list.clear();

		for (Department auxDepartment : departments) {
			list.add(auxDepartment.name);
		}

		opcao = Utility.selector(list, "Selecione o departmento onde se situa");

		return departments.get(opcao).name;

	}

	private ArrayList<Department> listDepartments(String facultyName) {
		try {
			return registry.listDepartments(facultyName);
		} catch (RemoteException remoteException) {
			System.out.println("Falha na obtencao de departamentos. A tentar novamente...");
			this.rmiNapper.nap();
		}

		return listDepartments(facultyName);
	}

	private ArrayList<Faculty> listFaculties() {
		try {
			return registry.listFaculties();
		} catch (RemoteException remoteException) {
			System.out.println("Falha na obtencao de faculdades. A tentar novamente...");
			this.rmiNapper.nap();
		}

		return listFaculties();
	}

	private Socket waitForRequest() {
		try {
			return listenSocket.accept();
		} catch (IOException ioe) {
			return waitForRequest();
		}
	}

	private void createServerSocket() {
		try {
			listenSocket = new ServerSocket(portTCP);
		} catch (IOException ioe) {
			System.out.println("Falha na criacao da socket de servidor");
		}
	}

	private void lookupRegistry() {
		rmiNapper.nap();
	}
}
