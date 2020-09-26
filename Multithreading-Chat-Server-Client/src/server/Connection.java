package server;

import client.Packet;

import java.io.*;
import java.net.Socket;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Connection {
    private ObjectInputStream is;
    private ObjectOutputStream os;
    Socket socket;
    private   Consumer<Packet> listener ;


    public Connection(Socket socket, Consumer<Packet> listener ) throws IOException{
        this.socket = socket;
        is = new ObjectInputStream( socket.getInputStream());
        os = new ObjectOutputStream( socket.getOutputStream());
        this.listener = listener;

    }
    public void startReading() throws IOException, ClassNotFoundException {
        Object o;
        while(true) {
            while ((o = is.readObject()) != null) {
                System.out.println(((Packet) o).toString());
                Packet packet = (Packet) o;
                listener.accept(packet);
            }
        }
    }

    public ObjectOutputStream getOs() {
        return os;
    }

    public void send(Packet packet) throws IOException {
        os.writeObject(packet);
        os.flush();
    }
}

