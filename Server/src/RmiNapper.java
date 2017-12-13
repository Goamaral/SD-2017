import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import Core.*;

class RmiNapper extends Thread {
	private int tries;
	private boolean end = false;
	private final Object lock = new Object();
	private ServerListener serverListener;

	public void run() {
		while(true) {
			synchronized (this.lock) {
				if (this.end) {
					return;
				}
			}
		}
	}

	synchronized public void nap() {

		try {
			this.serverListener.registry = (DataServerInterface) LocateRegistry
					.getRegistry(this.serverListener.portRMI)
					.lookup(this.serverListener.referenceRMI);
			return;
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

		this.tries = this.tries + 1;
		System.out.println("Trying to connect to RMI server, " + this.tries);

		int timeout = 30;
		if (this.tries == timeout) {
			System.out.println("Ligacao com o servidor principal nao pode ser estabelecida");

			//TODO connect to secondary rmi server

			synchronized (this.lock) {
				this.terminate();
			}
		}

		try {
			this.wait(1000);
		} catch (InterruptedException interruptedException) {
			System.exit(0);
		}
	}

	/*
	public void awake() {
		this.tries = 0;
	}
	*/

	private void terminate() {
		synchronized (this.lock) {
			this.end = true;
		}
	}

	RmiNapper(ServerListener serverListener) {
		this.serverListener = serverListener;

		this.start();
	}
}
