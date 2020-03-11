import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Scanner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

public class HalyClient {
   
   static String eServer = "";
   static int ePort = 0000;
   static String eServiceName = "";
   static Socket halySocket = null;
   
   public static void main(String[] args) {

      if (args.length != 3) {
         System.out.println("Usage: IP Port ServiceName");
         System.exit(1);
      }
      
      eServer = args[0];
      ePort = Integer.parseInt(args[1]);
      eServiceName = args[2];
      
      SSLContext sc = null;
       SSLSocket halySocket = null;
       SSLSocketFactory f = null;
         
      try {
         //SSL Factory
            System.setProperty("javax.net.ssl.trustStore", "trustedcerts");
            System.setProperty("javax.net.ssl.trustStorePassword", "tmpkey");
           
            sc = SSLContext.getInstance("TLS");
            TrustManager[] tm = new TrustManager[]{
                  new X509TrustManager() {
                     public X509Certificate[] getAcceptedIssuers() { return null; }
                     public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException { return; }
                     public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException { return; }
                     }};
            sc.init(null, tm, new java.security.SecureRandom());
            f = sc.getSocketFactory();
            
            InetAddress inetaddr = InetAddress.getByName(eServer);
            halySocket = (SSLSocket)f.createSocket(inetaddr, ePort);
            String supported[] = { "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384" };
            halySocket.setEnabledCipherSuites(supported);
            halySocket.startHandshake();
         }
         catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
         catch (KeyManagementException e) { e.printStackTrace(); }
         catch (UnknownHostException e) { e.printStackTrace(); }
         catch (IOException e) { e.printStackTrace(); } 
         
         new Thread(new ClientReceiver(halySocket, eServer, eServiceName)).start();
         new Thread(new ClientSender(halySocket, eServer, eServiceName)).start();
      
         }
}

class ClientSender implements Runnable {
   private SSLSocket halySocket = null;
   private String eServer;
   private String eServiceName;
   ClientSender(SSLSocket socket, String eServer, String eServiceName){
      this.halySocket = socket;
      this.eServer = eServer;
      this.eServiceName = eServiceName;
   }
   public void run() {
      Scanner KeyIn = null;
      PrintWriter out = null;
      try {
         KeyIn = new Scanner(System.in);
         out = new PrintWriter(halySocket.getOutputStream(),true);
         
         String userInput = "";
         System.out.println("Your are "+halySocket.getLocalPort());
         System.out.println("===Type Message=== \ngo : show your cards \ncheck : If X, Y, Z is 5 \nbye : leave");
         while((userInput = KeyIn.nextLine()) != null) {
            //rmi
            Haly h = (Haly)Naming.lookup("rmi://" + eServer + "/" + eServiceName);
            //확인하는 부분
            if(userInput.contentEquals("check")) {
               if(h.checkCount()) {
                  System.out.println("\n\n========Victory=========\n\n");
                  out.println("victory");
                  out.flush();
               }
               else {
                  System.out.println("Lose");
                  out.println("lose");
                  out.flush();
                  break;
               }
               
            }
            else if(userInput.contentEquals("go")) {
               out.println(userInput);
               out.flush();
            }
            
//            out.println(userInput);
//            out.flush();
            else if(userInput.equalsIgnoreCase("Bye"))
               break;
            else
               System.out.println("input : go, check, Bye");
            
         }
         KeyIn.close();
         out.close();
         halySocket.close();
      }catch(IOException i) {
         try {
            if(out != null) out.close();
            if(KeyIn != null) KeyIn.close();
            if(halySocket != null) halySocket.close();
         } catch(IOException e) {
      }
      System.exit(1);
         } catch (NotBoundException e) { e.printStackTrace(); }
      }
   }

class ClientReceiver implements Runnable{
   private SSLSocket halySocket = null;
   private String eServer;
   private String eServiceName;
   ClientReceiver(SSLSocket socket, String eServer, String eServiceName){
      this.halySocket = socket;
      this.eServer = eServer;
      this.eServiceName = eServiceName;
   }
   public void run() {
      while(halySocket.isConnected()) {
         BufferedReader in = null;
         int tmp = 0;
         try {
            in = new BufferedReader(new InputStreamReader(halySocket.getInputStream()));
            String readSome = null;
            while((readSome = in.readLine()) != null) {
               
               Haly h = (Haly)Naming.lookup("rmi://" + eServer + "/" + eServiceName);
               
               int x_num = 0;
               int y_num = 0;
               int z_num = 0;
               for(int i = 0; i<readSome.length();i++) {
                  if(readSome.charAt(i) == 'X') x_num++;
                  if(readSome.charAt(i) == 'Y') y_num++;
                  if(readSome.charAt(i) == 'Z') z_num++;
               }
               h.setX(x_num);
               h.setY(y_num);
               h.setZ(z_num);
               System.out.println(readSome);
               //x y z 수를 readSome

            }
            in.close();
            halySocket.close();
         } catch(IOException i) {
            try {
               if(in != null) in.close();
               if(halySocket != null) halySocket.close();
            } catch (IOException e) { }
            System.out.println("Leave.");
            System.exit(1);
         } catch (NotBoundException e) { e.printStackTrace(); }
      }
   }

}