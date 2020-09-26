package server;

public class User {
    private String hostName;
    private int port ;
    private int token;
    public User(String gostName, int port,int Token) {
        this.hostName = gostName;
        this.port = port;
        this.token = Token;
    }
    public String toString(){
        return "User [ ip: " + this.hostName + " : port: " + this. port + " ; Token: " + + this.token + "]";
    }
}
