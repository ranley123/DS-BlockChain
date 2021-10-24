import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class UpdatedBlockChainServer implements Runnable{
    private Socket socket;
    private int port;

    public UpdatedBlockChainServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
        try {
            // creating server socket object that takes in port number for bc server and queue length
            ServerSocket serverSocket = new ServerSocket(port, 6);
            while (true) {
                // accepts the incoming request
                socket = serverSocket.accept();
                // spawn a worker thread to process incoming request
                new UpdatedBlockChainServerWorker(socket).start();
            }
        } catch (IOException ioe) {
            // exception handling
            ioe.printStackTrace();
        }
    }
}