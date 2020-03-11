
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HalyImpl extends UnicastRemoteObject implements Haly {
	public static int X_NUM = 0;
	public static int Y_NUM = 0;
	public static int Z_NUM = 0;
	private static final long serialVersionUID = 1L;

	public HalyImpl() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}
	public boolean checkCount() throws RemoteException{
		if (X_NUM == 5) 
			return true;
		if (Y_NUM == 5) 
			return true;
		if (Z_NUM == 5) 
			return true;
		return false;
		
	}
	
	public void setX(int x_num) throws RemoteException{
		X_NUM = x_num;
	}
	
	public void setY(int y_num) throws RemoteException{
		Y_NUM = y_num;
	}
	
	public void setZ(int z_num) throws RemoteException{
		Z_NUM = z_num;
	}

}
