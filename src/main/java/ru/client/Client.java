package ru.client;

import ru.Connection;
import ru.ConsoleHelper;
import ru.Message;
import ru.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean isConnected = false;

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Interrupted exception");
            }
        }

        if(isConnected) {
            ConsoleHelper.writeMessage("Connection is true. For exit enter 'exit'");
        }else{
            ConsoleHelper.writeMessage("Error runtime client");
        }

        while (isConnected) {
            String mess = ConsoleHelper.readString();
            if(mess.equals("exit")) {
                break;
            }
            sendTextMessage(mess);
        }
    }

    protected void sendTextMessage(String message) {
        try {
            connection.send(new Message(MessageType.TEXT, message));
        }catch (IOException e) {
            isConnected = false;
            ConsoleHelper.writeMessage("Error when send message");
        }
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter server port");
        return ConsoleHelper.readInt();
    }

    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter server address");
        return ConsoleHelper.readString();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter user name");
        return ConsoleHelper.readString();
    }

    public class SocketThread extends Thread {
        @Override
        public void run() {
            String serverAddress = getServerAddress();
            int port = getServerPort();

            try(Socket socket = new Socket(serverAddress, port)) {
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            }catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                switch (message.getType()) {
                    case NAME_REQUEST:
                        connection.send(new Message(MessageType.USER_NAME,getUserName()));
                        break;
                    case NAME_ACCEPTED:
                        notifyConnectionStatusChanged(true);
                        return;
                    default: throw new IOException("Unexpected message type");
                }
            }
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            Client.this.isConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                switch (message.getType()) {
                    case TEXT:
                        processIncomingMessage(message.getText());
                        break;
                    case USER_ADDED:
                        informAboutAddingNewUser(message.getText());
                        break;
                    case USER_REMOVED:
                        informAboutRemovingUser(message.getText());
                        break;
                    default:
                        throw new IOException("Unexpected MessageType");
                }
            }
        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " joined the chat");
        }

        protected void informAboutRemovingUser(String userName) {
            ConsoleHelper.writeMessage(userName + " leaved the chat");
        }
    }
}
