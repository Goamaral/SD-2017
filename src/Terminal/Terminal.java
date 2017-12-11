import java.net.*;
import java.io.*;
import java.util.*;

public class Terminal {
	static int portServer = 7001;
	static String addressServer = "localhost";
	static Socket socket;
	static DataInputStream in;
	static DataOutputStream out;
	static String username;
	static String password;

	public static void main(String args[]) {
		getOptions(args);
		connectSocket();
		createStreams();

		System.out.println("Terminal de voto ativo");

		while(true) {
			cycle();
		}
	}
	
	public static void cycle() {
		HashMap<String, String> response = null;
		int size;
		ArrayList<String> list = new ArrayList<String>();
		int opcao;
		
		while(response == null) {
			response = waitForRequest();
			
			if (response.get("type").equals("status")) {
				if (response.get("login").equals("required")) {
					System.out.print("Numero estudante: ");
					username = System.console().readLine();

					System.out.print("Password: ");
					password = System.console().readLine();
					
					writeSocket("type|login;username|" + username + ";password|" + password);
				} else response = null;
			} else response = null;
		}
		
		response = null;
		
		while (response == null) {
			response = waitForRequest();
			if (response.get("type").equals("status")) {
				if (response.get("login").equals("sucessful")) {
					writeSocket("type|request;datatype|list");
				} else if (response.get("login").equals("failed")) {
					System.out.println("Credenciais invalidas");
					return;
				} else response = null;
			} else response = null;
		}
		
		response = null;
		
		while (response == null) {
			response = waitForRequest();
			
			if (response.get("type").equals("item_list")) {
				if (response.get("datatype").equals("list")) {
					list.clear();
					size = Integer.parseInt(response.get("item_count"));

					for (int i = 0; i<size; ++i) {
						list.add(i, response.get("item_" + i));
					}

					opcao = selector(list, "Vote numa lista");

					writeSocket("type|vote;list|" + list.get(opcao));
				} else response = null;
			} else response = null;
		}
	}

	public static int selector(ArrayList<String> list, String title) {
		int i = 0;
		int opcao;
		String line;

		System.out.println("--------------------");
		System.out.println(title);
		System.out.println("--------------------");

		for (String item : list) {
			System.out.println("[" + i + "] " + item);
			++i;
		}

		System.out.print("Opcao: ");
		line = System.console().readLine();
		
		try {
			opcao = Integer.parseInt(line);
		} catch (Exception e) {
			System.out.println("Opcao invalida");
			return selector(list, title);
		}

		if (opcao >= i && opcao < 0) {
			System.out.println("Opcao invalida");
			return selector(list, title);
		}

		return opcao;
	}

	public static HashMap<String, String> waitForRequest() {
		try {
			while(in.available() == 0);
			
			return parseResponse(readSocket());

		} catch (IOException ioe) {
			System.out.println("Falha na ligacao a mesa de voto");
		}

		return null;
	}

	public static void createStreams() {
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Falha na criacao de streams");
			System.exit(0);
		}
	}

	public static void connectSocket() {
		try {
			socket = new Socket(addressServer, portServer);
		} catch (UnknownHostException uhe) {
			System.out.println("Host desconhecido");
			System.exit(0);
		}
		catch (IOException ioe) {
			System.out.println("Falha na ligacao a mesa de voto");
			System.exit(0);
		}
	}

	public static void nap() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			System.exit(0);
		}
	}

	public static void writeSocket(String query) {
		try {
			out.writeUTF(query);
		} catch (IOException ioe) {
			System.out.println("Falha na ligacao a mesa de voto");
			System.exit(0);
		}
	}

	public static String readSocket() {
		try {
			return in.readUTF();
		} catch (IOException e) {
			return null;
		}
	}

	public static HashMap<String, String> parseResponse(String response) {
		HashMap<String, String> out = new HashMap<String, String>();
		String[] pairs = response.split(";");
		String[] pairParts;

		for (String pair : pairs) {
			pairParts = pair.split("\\|");
			out.put(pairParts[0], pairParts[1]);
		}

		return out;
	}

	public static void getOptions(String[] args) {
		for (int i=0; i<args.length; ++i) {
			if (args[i].equals("-sp")
				|| args[i].equals("--serverport")
			) {
				try {
					portServer = Integer.parseInt(args[i+1]);
				} catch (Exception e) {
					System.out.println("No valid server port provided, using default: 7001");
				}	
			}
			else if (args[i].equals("-sa")
				|| args[i].equals("--serveraddress")
			) {
				try {
					addressServer = args[i+1];
				} catch (Exception e) {
					System.out.println("No valid server address provided, using default: localhost");
				}
			}
			else if (args[i].equals("-h")
				|| args[i].equals("--help")
			) {
				System.out.println("java Terminal -lp 3000 -sa localhost -sp 7001");
				System.exit(0);
			}
		}
	}
}
