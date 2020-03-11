1. 컴파일
Eclipse 에서 컴파일
	or
javac HalyImpl.java
javac HalyServer.java
javac HalyClient.java

2. 동작 과정
start  rmiregistry
java -Djavax.net.debug=SSL,handshake HalyServer 127.0.0.1 8888 5
java -Djavax.net.debug=SSL,handshake HalyClient 127.0.0.1 8888 5