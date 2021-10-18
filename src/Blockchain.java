import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.security.PublicKey;

public class Blockchain {

    public static void main(String[] args){
        int processID = 0;

        if(args.length < 1)
            processID = 0;
        else{
            try{
                int tmp = Integer.parseInt(args[0]);
                if(tmp < 0 || tmp > 3){
                    System.out.println("Invalid Process ID");
                    System.exit(0);
                }
                processID = tmp;
                BlockchainWorker mainWorker = new BlockchainWorker(processID);

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}

class BlockchainWorker{
    int processID = 0;

    public BlockchainWorker(int processID){
        this.processID = processID;
        PortGenerator.setPorts(processID);
    }

    public void run(){
//        new Thread
    }

}

class PublicKeyServer implements Runnable{
    private ServerSocket serverSocket;
    private int port;

    public PublicKeyServer(int port) throws IOException {
        this.port = port;
    }

    @Override
    public void run() {
        while(true){
            try{
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();


            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}

class PublicKeyServerWorker extends Thread{
    Socket socket;

    public PublicKeyServerWorker(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = reader.readLine();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class PublicKeyMsg{
    private int processID;
    private String publicKey;

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}


class BlockRecord{
    String BlockID;
    String VerificationProcessID;
    String PreviousHash; // We'll copy from previous block
    UUID uuid; // Just to show how JSON marshals this binary data.
    String Fname;
    String Lname;
    String SSNum;
    String DOB;
    String Diag;
    String Treat;
    String Rx;
    String RandomSeed; // Our guess. Ultimately our winning guess.
    String WinningHash;

    /* Examples of accessors for the BlockRecord fields: */
    public String getBlockID() {return BlockID;}
    public void setBlockID(String BID){this.BlockID = BID;}

    public String getVerificationProcessID() {return VerificationProcessID;}
    public void setVerificationProcessID(String VID){this.VerificationProcessID = VID;}

    public String getPreviousHash() {return this.PreviousHash;}
    public void setPreviousHash (String PH){this.PreviousHash = PH;}

    public UUID getUUID() {return uuid;} // Later will show how JSON marshals as a string. Compare to BlockID.
    public void setUUID (UUID ud){this.uuid = ud;}

    public String getLname() {return Lname;}
    public void setLname (String LN){this.Lname = LN;}

    public String getFname() {return Fname;}
    public void setFname (String FN){this.Fname = FN;}

    public String getSSNum() {return SSNum;}
    public void setSSNum (String SS){this.SSNum = SS;}

    public String getDOB() {return DOB;}
    public void setDOB (String RS){this.DOB = RS;}

    public String getDiag() {return Diag;}
    public void setDiag (String D){this.Diag = D;}

    public String getTreat() {return Treat;}
    public void setTreat (String Tr){this.Treat = Tr;}

    public String getRx() {return Rx;}
    public void setRx (String Rx){this.Rx = Rx;}

    public String getRandomSeed() {return RandomSeed;}
    public void setRandomSeed (String RS){this.RandomSeed = RS;}

    public String getWinningHash() {return WinningHash;}
    public void setWinningHash (String WH){this.WinningHash = WH;}

}

class PortGenerator{
    public static int mainBaseServer = 4600;
    public static int pkBaseServer = 4710;
    public static int ubBaseServer = 4820;
    public static int bcBaseServer = 4930;

    public static int mainServerPort = 0;
    public static int pkServerPort = 0;
    public static int ubServerPort = 0;
    public static int bcServerPort = 0;

    public static void setPorts(int processID){
        mainServerPort = mainBaseServer + processID;
        pkServerPort = pkBaseServer + processID;
        ubServerPort = ubBaseServer + processID;
        bcServerPort = bcBaseServer + processID;
    }
}
