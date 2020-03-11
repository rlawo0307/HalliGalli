import java.net.*;
import java.rmi.Naming;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import java.io.*; 


public class HalyServer implements Runnable {

   private HalyServerRunnable clients[] = new HalyServerRunnable[3];
   public int clientCount = 0;

   private int ePort = -1;
   private String eServer;
   private String eServiceName;

   public HalyServer(String eServer, int port, String eServiceName) {
	  this.eServer = eServer;
      this.ePort = port;
      this.eServiceName = eServiceName;
   }

   public void run() {
	  String keystore_dir = "C:/Users/KCH/eclipse-workspace/Project/bin/.keystore/SSLSocketServerKey";

	  char KeyStorePass[] = "keystore".toCharArray();
      char KeyPass[] = "keystore".toCharArray();
      KeyStore ks;
      SSLContext sc;
      SSLServerSocket serverSocket = null;
      SSLServerSocketFactory ssf = null;
      KeyManagerFactory kmf;
      try {
    	  ks = KeyStore.getInstance("JKS");
          ks.load(new FileInputStream(keystore_dir), KeyStorePass);
          kmf =  KeyManagerFactory.getInstance("SunX509");
          kmf.init(ks, KeyPass);
          sc = SSLContext.getInstance("TLS");
          sc.init(kmf.getKeyManagers(), null, null);
          ssf = sc.getServerSocketFactory();
          serverSocket = (SSLServerSocket)ssf.createServerSocket(ePort);
         System.out.println ("Server started: socket created on " + ePort);
         
         Haly h = new HalyImpl();
         Naming.rebind("rmi://" + eServer + ":1099/" + eServiceName, h);
         while (true) {
            addClient(serverSocket);
         }
         
      } catch (IOException i) { System.out.println(i); }
      catch (KeyStoreException e) { e.printStackTrace(); }
      catch (UnrecoverableKeyException e) { e.printStackTrace(); }
      catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
      catch (CertificateException e) { e.printStackTrace(); }
      catch (KeyManagementException e) { e.printStackTrace(); }
      finally {
         try {
            if (serverSocket != null)
               serverSocket.close(); 
         } catch (IOException i) { System.out.println(i); }
      }
   }
   //client에 번호 부여
   public int whoClient(int clientID) {
      for (int i = 0; i < clientCount; i++)
         if (clients[i].getClientID() == clientID)
            return i;
      return -1;
   }
   
   //해당클라이언트말고 다른 클라이언트에게 inputLine전송 chat할때 쓰이는 부분
   public void putClient(int clientID, String inputLine) {
      for (int i = 0; i < clientCount; i++)
         if (clients[i].getClientID() == clientID) {
            System.out.println("writer: "+clientID);
         } else {
            System.out.println("write: "+clients[i].getClientID());
            clients[i].out.println(inputLine);
         }
   }
   
   //전체 클라이언트에게 inputLine 전송
   public void putClient(String inputLine) {
	   for(int i = 0; i< clientCount; i++)
		   clients[i].out.println(inputLine);
   }
   //deck 출력
   public void putClientDeck(int clientID, String deck) {
	   for(int i = 0; i< clientCount; i++)
		   
		   clients[i].out.println(deck);
   }
   //모든 클라이언트의 덱을 만들어 주는 부분
   public String makeAllDeck(int clientID) {
	   String All_deck = "";
	   for(int i = 0; i < clientCount; i++) {
		   String tmp = clients[i].getClientDeck();
		   while(tmp.length() != 11)
			   tmp = tmp.concat("=");
		   All_deck = All_deck + tmp;
	   }
	   return All_deck;
   }
   //승리를 알리는 함수
   public void putClientVictory(int clientID, String inputLine) {
	   for (int i = 0; i < clientCount; i++) {
		   clients[i].out.println("\n\n========client"+clientID+" wins=======\n\n");
	   }
   }
   public void putClientLose(int clientID, String inputLine) {
	   for (int i = 0; i < clientCount; i++) {
		   clients[i].out.println("\n\n========client"+clientID+" loses=======\n\n");
	   }
   }
   public void putClient_name() {
	   for(int i = 0; i< clientCount; i++) {
		   for(int j = 0; j < clientCount ; j++) {
			   clients[i].out.print("clients"+clients[j].getClientID()+"");
		   }
	   		clients[i].out.print("\n");
	   }
   }
   
   public void addClient(ServerSocket serverSocket) {
      Socket clientSocket = null;
      
      if (clientCount < clients.length) { 
         try {
            clientSocket = serverSocket.accept();
            clientSocket.setSoTimeout(4000000); // 1000/sec
         } catch (IOException i) {
            System.out.println ("Accept() fail: "+i);
         }
         clients[clientCount] = new HalyServerRunnable(this, clientSocket);
         new Thread(clients[clientCount]).start();
         clientCount++;
         System.out.println ("Client connected: " + clientSocket.getPort()
               +", CurrentClient: " + clientCount);
      } else {
         try {
            Socket dummySocket = serverSocket.accept();
            HalyServerRunnable dummyRunnable = new HalyServerRunnable(this, dummySocket);
            new Thread(dummyRunnable);
            dummyRunnable.out.println(dummySocket.getPort()
                  + " < Sorry maximum user connected now");
            System.out.println("Client refused: maximum connection "
                  + clients.length + " reached.");
            dummyRunnable.close();
         } catch (IOException i) {
            System.out.println(i);
         }   
      }
   }
   //한판이 끝났을때 덱 원래대로 돌리기
   public void cleanClient_deck() {
	   for (int i = 0; i < clientCount; i++) {
		   clients[i].cleanDeck();
	   }
   }
   
   //클라이언트 삭제
   public synchronized void delClient(int clientID) {
      int pos = whoClient(clientID);
      HalyServerRunnable endClient = null;
         if (pos >= 0) {
             endClient = clients[pos];
            if (pos < clientCount-1)
               for (int i = pos+1; i < clientCount; i++)
                  clients[i-1] = clients[i];
            clientCount--;
            System.out.println("Client removed: " + clientID + " at clients[" + pos +"], CurrentClient: " + clientCount);
            endClient.close();
         }
   }
   
   public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Usage: IP Port ServiceName");
			System.exit(1);
		}
		
		String eServer = args[0];
		int ePort = Integer.parseInt(args[1]);
		String eServiceName = args[2];
		new Thread(new HalyServer(eServer, ePort, eServiceName)).start();
   }
}


class HalyServerRunnable implements Runnable {
   protected HalyServer halyServer = null;
   protected Socket clientSocket = null;
   protected PrintWriter out = null;
   protected BufferedReader in = null;
   public int clientID = -1;
   public String clientDeck = "";
   public HalyServerRunnable(HalyServer server, Socket socket) {
      this.halyServer = server;
      this.clientSocket = socket;
      clientID = clientSocket.getPort();
      try {
         out = new PrintWriter(clientSocket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      }catch(IOException i) {
      }
   }
   
   public void run() {
	   String[] deck = {"X", "XX", "XXX", "XXXX", "XXXXX", "Y", "YY", "YYY", "YYYY", "YYYYY", "Z", "ZZ", "ZZZ", "ZZZZ", "ZZZZZ"};
      try {
         String inputLine;
         
         //client로 메세지를 보내는 부분
         while((inputLine = in.readLine()) != null) {
            if(inputLine.equalsIgnoreCase("ready")) {
                halyServer.putClient("test 성공");
            }
            
            else if(inputLine.equalsIgnoreCase("go")) {
            	double randomValue = Math.random();
            	int k = (int)(randomValue*15);
            	clientDeck = deck[k];
            	String allDeck = halyServer.makeAllDeck(getClientID());
            	halyServer.putClient("\n\n=====Cards======");
            	halyServer.putClient_name();
            	halyServer.putClient(allDeck);
            }
            else if(inputLine.equals("victory")) {
            	halyServer.putClientVictory(getClientID(), inputLine);
            	halyServer.putClient("\n\n=======restart=======\n\n");
            	halyServer.putClient_name();
            	halyServer.cleanClient_deck();
            	String allDeck = halyServer.makeAllDeck(getClientID());
            	halyServer.putClient(allDeck);
            }
            else if(inputLine.equals("lose")) {
            	halyServer.putClientLose(getClientID(), inputLine);
            	halyServer.putClient("\n\n========restart========\n\n");
            }
            else {
//                halyServer.putClient(getClientID(), getClientID() +":"+inputLine);
            	halyServer.putClient(getClientID(), inputLine);
            }
            if(inputLine.equalsIgnoreCase("Bye"))
               break;
         }
         halyServer.delClient(getClientID());
      }catch(SocketTimeoutException ste) {
         System.out.println("Socket timeoyt Occured, force close() : "+getClientID());
         halyServer.delClient(getClientID());
      }catch (IOException e) {
         halyServer.delClient(getClientID());
      }
   }
   
   
   public int getClientID() {
      return clientID;
   }
   
   public String getClientDeck() {
	   
	   return clientDeck;
   }
   
   public void cleanDeck() {
	   clientDeck ="";
   }
   
   public void close() {
      try {
         if(in != null) in.close();
         if(out != null) out.close();
         if(clientSocket != null) clientSocket.close();
      }catch (IOException i) {
      }
   }
}