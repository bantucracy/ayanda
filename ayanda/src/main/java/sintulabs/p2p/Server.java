package sintulabs.p2p;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Server class for accepting client connections. Must be ran in a thread
 */

public class Server {
    private ServerSocket serverSocket;
    private Socket socket;
    private int localPort;
    //List of Model.News Items
    private ArrayList<String> newsItems;
    private PrintWriter out;
    BufferedReader in;

    public Server(int port) throws IOException {
        if (port == 0) {
            serverSocket = new ServerSocket(0);
        } else {
            serverSocket = new ServerSocket(port);
        }
        localPort = serverSocket.getLocalPort();
        accept();
    }

    private void accept() throws IOException {
        while (true) {
            socket = serverSocket.accept();


            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String msg = "Sending JSON to client: " + socket.getInetAddress().toString();
                // msg += "\n " + responseArray;
                Message message = Message.obtain();
                message.obj = msg;
                Log.d("server_debug", msg);
                out.println("hi");

            } catch (IOException e) {
                Log.d("wifi_debug", "server error: " + e.toString());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ioe) {
                    }
                }
            }
        }

    }

    /**
     * Returns the localPort if set. Otherwise returns 0
     * @return Returns localPort or 0
     */
    public int getLocalPort() {
        return localPort;
    }
}

