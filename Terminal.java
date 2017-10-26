import java.net.*;
import java.io.*;

public class Terminal {
  static int portLocal = 3000;
  static int portServer = 7001;
  static Socket socket;
  static DataInputStream inStream;
  static DataOutputStream outStream;

  public static main(String args[]) {
    getOptions();

    try {
      socket = new Socket(portLocal, portServer);
      inStream = new DataInputStream(socket.getInputStream());
	    outStream = new DataOutputStream(socket.getOutputStream());

      
    } catch (Exception e) {
      main(args);
    }
  }

  public static void getOptions(String[] args) {
		for (int i=0; i<args.length; ++i) {
			switch (args[i]) {
				case "-lp":
				case "--localport":
					try {
						portLocal = Integer.parseInt(args[i+1]);
					} catch (Exception e) {
						System.out.println("No valid local port provided, using default: 3000");
					}
					break;
        case "-sp":
				case "--serverport":
					try {
						portServer = Integer.parseInt(args[i+1]);
					} catch (Exception e) {
						System.out.println("No valid server port provided, using default: 7001");
					}
					break;
				case "-h":
				case "--help":
					System.out.println("java -lp 3000 -sp 7001");
					System.exit(0);
					break;
			}
		}
	}
}


public class TCPClient {
  public static void main(String args[]) {
    Socket s = null;
    int serversocket = 7001;
    try {
      // 1o passo
      s = new Socket("localhost", serversocket);

      System.out.println("SOCKET=" + s);
      // 2o passo
      DataInputStream in = new DataInputStream(s.getInputStream());
      DataOutputStream out = new DataOutputStream(s.getOutputStream());

      String texto = "";
      InputStreamReader input = new InputStreamReader(System.in);
      BufferedReader reader = new BufferedReader(input);
      System.out.println("Introduza texto:");

      // 3o passo
      while (true) {
      // READ STRING FROM KEYBOARD
      try {
          texto = reader.readLine();
      } catch (Exception e) {
    }

    // WRITE INTO THE SOCKET
    out.writeUTF(texto);

    // READ FROM SOCKET
    String data = in.readUTF();

    // DISPLAY WHAT WAS READ
    System.out.println("Received: " + data);
      }

  } catch (UnknownHostException e) {
      System.out.println("Sock:" + e.getMessage());
  } catch (EOFException e) {
      System.out.println("EOF:" + e.getMessage());
  } catch (IOException e) {
      System.out.println("IO:" + e.getMessage());
  } finally {
    if (s != null)
      try {
          s.close();
      } catch (IOException e) {
          System.out.println("close:" + e.getMessage());
      }
    }
  }
}
