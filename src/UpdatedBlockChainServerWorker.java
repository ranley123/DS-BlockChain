import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class UpdatedBlockChainServerWorker extends Thread{
    Socket socket;

    public UpdatedBlockChainServerWorker(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Gson gson = new Gson();
            StringBuffer sb = new StringBuffer();
            String line = "";

            while((line = reader.readLine()) != null){
                sb.append(line);
            }

            BlockRecord block = gson.fromJson(sb.toString(), BlockRecord.class);
            if(!Blockchain.hasDuplicateBlock(block)){
                Blockchain.blockchain.add(0, block);
            }

            if(Blockchain.processID == 0)
                Utils.writeToJSON();

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}