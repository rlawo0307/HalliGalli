
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Haly extends Remote{
	public boolean checkCount() throws RemoteException;
	public void setX(int x_num) throws RemoteException;
	public void setY(int y_num) throws RemoteException;
	public void setZ(int z_num) throws RemoteException;
}
