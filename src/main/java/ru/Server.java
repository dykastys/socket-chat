package ru;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String,Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter the port for server");
        int port = ConsoleHelper.readInt();

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Server is running!");
            while (true) {
                Socket socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendBroadcastMessage(Message message) {
        for(Map.Entry<String,Connection> entry : connectionMap.entrySet()) {
            try {
                entry.getValue().send(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage("There is connection with remote address: " + socket.getRemoteSocketAddress());
            String userName = null;

            try(Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                ConsoleHelper.writeMessage(String.format("Client %s is connected (%s)", userName, socket.getRemoteSocketAddress()));
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                notifyThisUser(connection, userName);
                serverMainLoop(connection, userName);
            }catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Error when exchanging data with a remote server");
            }
            if(userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
            }
            ConsoleHelper.writeMessage(String.format("User %s (address: %s) leave the chat", userName, socket.getRemoteSocketAddress()));
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            connection.send(new Message(MessageType.NAME_REQUEST));
            Message message = connection.receive();

            if(message.getType() == MessageType.USER_NAME && !message.getText().isEmpty()) {
                if(!connectionMap.containsKey(message.getText())) {
                    connectionMap.put(message.getText(), connection);
                    connection.send(new Message(MessageType.NAME_ACCEPTED));
                }else return serverHandshake(connection);
            }else return serverHandshake(connection);

            return message.getText();
        }

        private void notifyThisUser(Connection connection, String userName) throws IOException {
            for(Map.Entry<String,Connection> entry : connectionMap.entrySet()) {
                if(!entry.getKey().equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if(message.getType() == MessageType.TEXT && !message.getText().isEmpty()) {
                    String mess = userName + ": " + message.getText();
                    sendBroadcastMessage(new Message(MessageType.TEXT, mess));
                }else{
                    ConsoleHelper.writeMessage(String.format("Error when %s send the message", userName));
                }
            }
        }
    }
}
