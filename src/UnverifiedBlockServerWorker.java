import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class UnverifiedBlockServerWorker extends Thread{
    Socket socket;

    public UnverifiedBlockServerWorker(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try {
            BufferedReader inputData = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            Gson gson = new Gson();
            StringBuffer sb = new StringBuffer();
            while ((line = inputData.readLine()) != null) {
                sb.append(line);
            }

            BlockRecord block = gson.fromJson(sb.toString(), BlockRecord.class);
            Blockchain.unverifiedBlockQueue.offer(block);
            socket.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }
}