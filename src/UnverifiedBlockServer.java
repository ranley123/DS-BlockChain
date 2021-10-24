import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class UnverifiedBlockServer implements Runnable{
    private ServerSocket serverSocket;
    private int port;

    public UnverifiedBlockServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
        while(true){
            try{
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();

                new UnverifiedBlockServerWorker(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}