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
		HashMap<String, String> response;
		boolean loginRequired;
		boolean loginSucessful;
		boolean loginFailed;
		boolean listListRecieved;
		int size;
		ArrayList<String> list = new ArrayList<String>();
		int opcao;

		getOptions(args);
		connectSocket();
		createStreams();

		System.out.println("Terminal de voto ativo");

		while (true) {
			if (newInput()) {
				response = parseResponse(readSocket());
				if (response != null) {
					loginRequired = response.get("type").equals("status")
														&& response.get("login").equals("required");

					loginSucessful = response.get("type").equals("status")
														&& response.get("login").equals("sucessful");

					loginFailed = response.get("type").equals("status")
														&& response.get("login").equals("failed");

					listListRecieved = response.get("type").equals("item_list")
																&& response.get("datatype").equals("list");

					if (loginRequired) {
						auth();
					} else if (loginSucessful) {
						writeSocket("type|request;datatype|list");
					} else if (loginFailed) {
						System.out.println("Credenciais invalidas");
						auth();
					} else if (listListRecieved) {
						list.clear();
						size = Integer.parseInt(response.get("item_count"));

						for (int i = 0; i<size; ++i) {
							list.add(i, response.get("item_" + i));
						}

						list.add("Nulo");
						list.add("Branco");

						opcao = selector(list, "Vote numa lista");

						writeSocket("type|vote;list|" + list.get(opcao));
					}
				} else {
					System.out.printf("NULL RESPONSE");
				}
			}
		}
  }

	public static int selector(ArrayList<String> list, String title) {
		int i = 0;
		int opcao;
		Scanner scanner = new Scanner(System.in);
		String line;

		System.out.println("--------------------");
		System.out.println(title);
		System.out.println("--------------------");

		for (String item : list) {
			System.out.println("[" + i + "] " + item);
			++i;
		}

		System.out.print("Opcao: ");
		line = scanner.nextLine();

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

	public static boolean newInput() {
		try {
			return in.available() != 0;
		} catch (IOException e) {
			System.out.println("Falha na ligacao a mesa de voto");
			System.exit(0);
			return false;
		}
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

	public static void auth() {
		Scanner scanner = new Scanner(System.in);

		System.out.print("Numero estudante: ");
		username = scanner.nextLine();

		System.out.print("Password: ");
		password = scanner.nextLine();

		writeSocket("type|login;username|" + username + ";password|" + password);
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

		System.out.println(response);

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
			switch (args[i]) {
				case "-sp":
				case "--serverport":
					try {
						portServer = Integer.parseInt(args[i+1]);
					} catch (Exception e) {
						System.out.println("No valid server port provided, using default: 7001");
					}
					break;
				case "-sa":
				case "--serveraddress":
					try {
						addressServer = args[i+1];
					} catch (Exception e) {
						System.out.println("No valid server address provided, using default: localhost");
					}
					break;
				case "-h":
				case "--help":
					System.out.println("java -lp 3000 -sa localhost -sp 7001");
					System.exit(0);
					break;
			}
		}
	}
}
