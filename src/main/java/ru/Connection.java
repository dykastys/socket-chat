package ru;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection implements Closeable {
    private final Socket socket;
    private final ObjectInputStream is;
    private final ObjectOutputStream os;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.os = new ObjectOutputStream(socket.getOutputStream());
        this.is = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException {
        synchronized (os) {
            os.writeObject(message);
            os.flush();
        }
    }

    public Message receive() throws IOException, ClassNotFoundException {
        Message message;
        synchronized (is) {
            message = (Message) is.readObject();
        }
        return message;
    }

    @Override
    public void close() throws IOException {
        is.close();
        os.close();
        socket.close();
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getIs() {
        return is;
    }

    public ObjectOutputStream getOs() {
        return os;
    }
}
