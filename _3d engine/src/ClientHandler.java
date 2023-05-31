import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String name;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.name = reader.readLine();
            clients.add(this);
            broadcastMessage("Server", name + " has joined the chat");
        } catch (IOException e) {
            e.printStackTrace();
            closeEverything();
        }
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            try {
                String messageFromClient = reader.readLine();
                if (messageFromClient != null) {
                    broadcastMessage(name, messageFromClient);
                } else {
                    closeEverything();
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything();
            }
        }
    }

    public void broadcastMessage(String sender, String message) {
        for (ClientHandler client : clients) {
            try {
                if (!client.name.equals(name)) {
                    client.writer.write(sender + ": " + message);
                    client.writer.newLine();
                    client.writer.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeClient() {
        clients.remove(this);
    }

    public void closeEverything() {
        removeClient();
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
