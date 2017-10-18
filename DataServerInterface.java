import java.rmi.*;

public interface DataServerInterface extends Remote {
  public boolean registerPerson(Person person) throws RemoteException;
}
