1. ������
Eclipse ���� ������
	or
javac HalyImpl.java
javac HalyServer.java
javac HalyClient.java

2. ���� ����
start  rmiregistry
java -Djavax.net.debug=SSL,handshake HalyServer 127.0.0.1 8888 5
java -Djavax.net.debug=SSL,handshake HalyClient 127.0.0.1 8888 5