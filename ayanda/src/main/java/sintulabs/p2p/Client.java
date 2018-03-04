package sintulabs.p2p;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client class
 */

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public Client(String serverAddress, int port) throws IOException {
        Log.d("client_debug", "Connecting as a client");
        socket = new Socket(serverAddress, port);
        Log.d("client_debug", "made socket request to: " + serverAddress + ":" + port);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        String message = in.readLine();
        /*
        Message m = Message.obtain();
        m.obj = message;
        uiHandler.sendMessage(m);
        */

        Log.d("client_debug", message);

    }
}
