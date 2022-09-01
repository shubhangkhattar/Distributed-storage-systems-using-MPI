import java.io.File;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface APIInterface extends Remote {
    public String listFiles() throws RemoteException;
    public void upload(String fileName, byte[] buffer) throws RemoteException, IOException;
    public void deleteFile(String url) throws RemoteException;
    public String downloadFile(String url) throws RemoteException;
}
