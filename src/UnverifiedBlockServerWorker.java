import java.net.Socket;

public class UnverifiedBlockServerWorker extends Thread{
    Socket socket;

    public UnverifiedBlockServerWorker(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){

    }
}