import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class PublicKeyServerWorker extends Thread{
    Socket socket;

    public PublicKeyServerWorker(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            Gson g = new Gson();
            DataInputStream input = new DataInputStream(socket.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();

            PublicKeyMsg publicKeyMsg = g.fromJson(line, PublicKeyMsg.class);
            Blockchain.pkList.add(publicKeyMsg);

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
