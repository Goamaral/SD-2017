class Server {
	private static int portRMI = 7000;
	//private static String ipRMI = "localhost";
	private static String referenceRMI = "iVotas";
	private static int portTCP = 7001;
	
	public static void main(String args[]) {
		setSecurityPolicies();
		getOptions(args);
		
		new ServerListener(portRMI, referenceRMI, /*ipRMI,*/ portTCP);
	}
	
	private static void setSecurityPolicies() {
		System.getProperties().put("java.security.policy", "policy.all");
		System.setSecurityManager(new SecurityManager());
	}

	private static void getOptions(String[] args) {
		for (int i=0; i<args.length; ++i) {
			switch (args[i]) {
				case "-rp":
				case "--rmiport":
					try {
						portRMI = Integer.parseInt(args[i + 1]);
					} catch (Exception e) {
						System.out.println("No RMI port provided, using default: 7000");
					}
					break;
				case "-ri":
				case "-rmiip":
					/*
					try {
						ipRMI = args[i + 1];
					} catch (Exception e) {
						System.out.println("No RMI ip provided, using default: localhost");
					}
					*/
					break;
				case "-rr":
				case "--rmireference":
					try {
						referenceRMI = args[i + 1];
					} catch (Exception e1) {
						System.out.println("No valid RMI reference provided, using default: iVotas");
					}
					break;
				case "-tp":
				case "-tcpport":
					try {
						portTCP = Integer.parseInt(args[i + 1]);
					} catch (Exception e2) {
						System.out.println("No valid TCP port provided, using default: 7001");
					}
					break;
				case "-h":
				case "--help":
					System.out.println("java Server -rp 7000 -ri localhost -rr iVotas -tp 7001");
					System.exit(0);
			}
		}
	}

}

