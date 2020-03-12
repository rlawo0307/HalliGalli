Socket과 SSL을 통해 양방향 통신을 구현한 온라인 할리갈리 게임



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
