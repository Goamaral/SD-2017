package Core;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class Faculty implements Serializable {
	public String name;
	
	public static final long serialVersionUID = -3974198952227657032L;

	public Faculty(String name) {
	  this.name = name;
	}
}
