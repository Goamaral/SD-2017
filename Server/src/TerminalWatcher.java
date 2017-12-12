class TerminalWatcher extends Thread {
	int timeout = 30;
	Object timeoutLock = new Object();
	TerminalConnection terminalConnection;
	Object watcherLock;
	Object lock = new Object();
	Boolean end = false;

	public void run() {
		while (true) {
			synchronized (this.lock) {
				if(this.end) return;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				System.exit(0);
			}

			synchronized (this.timeoutLock) {
				this.timeout = this.timeout - 1;

				if (this.timeout == 0) {
					synchronized (this.watcherLock) {
						this.terminalConnection.watcherTimedout = true;
						return;
					}
				}
			}
		}

	}

	public void terminate() {
		synchronized (this.lock) {
			this.end = true;
		}
	}

	public TerminalWatcher(TerminalConnection terminalConnection, Object watcherLock) {
		this.terminalConnection = terminalConnection;
		this.watcherLock = watcherLock;

		this.start();
	}
}
