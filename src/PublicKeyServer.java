import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PublicKeyServer implements Runnable{
    private int port;

    public PublicKeyServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
            try(ServerSocket serverSocket = new ServerSocket(port)){
                while(true) {
                    Socket socket = serverSocket.accept();

                    new PublicKeyServerWorker(socket).start();
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
    }
}