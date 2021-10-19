import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Blockchain {
    public static int processNum = 0;
    public static int TOTALPROCESSES = 3;
    public static List<PublicKeyMsg> pkList = new LinkedList<>();
    public static KeyPair keyPair;
    public static int processID = 0;
    public static boolean processBegin = false;


    public static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
        rng.setSeed(seed);
        keyGenerator.initialize(1024, rng);

        return (keyGenerator.generateKeyPair());
    }

    public PublicKeyMsg initPK(){
        byte[] bytePK = keyPair.getPublic().getEncoded();
        String stringPK = Base64.getEncoder().encodeToString(bytePK);
        PublicKeyMsg msg = new PublicKeyMsg(processID, stringPK);
        return msg;
    }

    public void sendMultiPKs(){
        // init this process's public key
        PublicKeyMsg pkMsg = initPK();
        Gson g = new Gson();

        try{
            for(int i = 0; i < TOTALPROCESSES; i++){
                int curPort = PortGenerator.pkBaseServer + processID;
                Socket socket = new Socket("localhost", curPort);
                PrintWriter pw = new PrintWriter(socket.getOutputStream());
                String msg = g.toJson(pkMsg);
                pw.write(msg);
                pw.flush();

                socket.shutdownOutput();
                pw.close();
                socket.close();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Thread mainServer = new Thread(new MainServer(PortGenerator.mainServerPort));
        Thread pkServer = new Thread(new PublicKeyServer(PortGenerator.pkServerPort));
        Thread ubServer = new Thread(new UnverifiedBlockServer(PortGenerator.ubServerPort));
        Thread bcServer = new Thread(new BlockChainServer(PortGenerator.bcServerPort));

        mainServer.start();
        pkServer.start();
        ubServer.start();
        bcServer.start();

        try{
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(processID == 2){
            try {
                for (int i = 0; i < TOTALPROCESSES; i++) {
                    int curServerPort = PortGenerator.mainBaseServer + processID;
                    Socket socket = new Socket("localhost", curServerPort);
                    PrintWriter pw = new PrintWriter(socket.getOutputStream());
                    String msg = "ready";
                    pw.write(msg);
                    pw.flush();

                    socket.shutdownOutput();
                    pw.close();
                    socket.close();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try{
            KeyPair keyPair = generateKeyPair(999);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(!processBegin){
            try{
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        sendMultiPKs();



    }

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

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        PortGenerator.setPorts(processID);
        Blockchain main = new Blockchain();
        main.run();
    }

}

class MainServer implements Runnable{
    private ServerSocket serverSocket;
    private int port;

    public MainServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
        while(true){
            try{
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();

                DataInputStream input = new DataInputStream(socket.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line = reader.readLine();

                if(line.equals("ready"))
                    Blockchain.processBegin = true;
                socket.close();

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}

class PublicKeyServer implements Runnable{
    private ServerSocket serverSocket;
    private int port;

    public PublicKeyServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
        while(true){
            try{
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();

                new PublicKeyServerWorker(socket).start();
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

    @Override
    public void run(){
        try{
            Gson g = new Gson();
            DataInputStream input = new DataInputStream(socket.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String line = reader.readLine();

            PublicKeyMsg publicKeyMsg = g.fromJson(line, PublicKeyMsg.class);
            Blockchain.pkList.add(publicKeyMsg);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class PublicKeyMsg{
    private int processID;
    private String publicKey;

    public PublicKeyMsg(int processID, String publicKey){
        this.processID = processID;
        this.publicKey = publicKey;
    }

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

class UnverifiedBlockServer implements Runnable{
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

class UnverifiedBlockServerWorker extends Thread{
    Socket socket;

    public UnverifiedBlockServerWorker(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){

    }
}

class BlockChainServer implements Runnable{
    private ServerSocket serverSocket;
    private int port;

    public BlockChainServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
        while(true){
            try{
                serverSocket = new ServerSocket(port);
                Socket socket = serverSocket.accept();

                new BlockChainServerWorker(socket).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class BlockChainServerWorker extends Thread{
    Socket socket = null;

    public BlockChainServerWorker(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run(){

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
