import com.google.gson.Gson;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.*;

public class Blockchain {
    public static int TOTALPROCESSES = 3;
    public static List<PublicKeyMsg> pkList = new LinkedList<>();
    public static KeyPair keyPair;
    public static int processID = 0;
    public static boolean processBegin = false;
    public static List<BlockRecord> blockchain = new LinkedList<>();

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

    public void initBlockchain(){
        BlockRecord dummy = new BlockRecord();
        dummy.setBlockID(UUID.randomUUID().toString());

        Date date = new Date();
        String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
        String TimeStampString = T1 + "." + processID + "\n";
        dummy.setTimestamp(TimeStampString);

        dummy.setBlockNum("0");
        dummy.setFirstname("James");
        dummy.setLastname("Bond");
        dummy.setDateOfBirth("2021-10-19");
        dummy.setSsNum("000-00-0000");
        dummy.setDiagnose("fever");
        dummy.setTreat("exercise");
        dummy.setRx("food");
        dummy.setPrevHash("1111111111");
        dummy.setWinningHash(hashBlock(dummy));

        blockchain.add(dummy);

    }

    public String hashBlock(BlockRecord block){
        String SHA256String = "";
        String blockStr = block.getBlockID() +
                block.getFirstname() +
                block.getLastname() +
                block.getSsNum() +
                block.getDateOfBirth() +
                block.getDiagnose() +
                block.getTreat() +
                block.getRx();

        try{
            MessageDigest ourMD = MessageDigest.getInstance("SHA-256");
            ourMD.update (blockStr.getBytes());
            byte byteData[] = ourMD.digest();

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }
            SHA256String = sb.toString(); // For ease of looking at it, we'll save it as a string.
        }catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        };

        return SHA256String;
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
            KeyPair keyPair = Utils.generateKeyPair(999);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(!processBegin){
            Utils.wait(3000);
        }

        sendMultiPKs();
        Utils.wait(3000);


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
    String blockID;
    String blockNum;
    String verificationProcessID;
    String prevHash;
    String winningHash;
    String firstname;
    String lastname;
    String ssNum;
    String dateOfBirth;
    String timestamp;
    String diagnose;
    String treat;
    String rx;

    public String getBlockID() {
        return blockID;
    }

    public void setBlockID(String blockID) {
        this.blockID = blockID;
    }

    public String getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(String blockNum) {
        this.blockNum = blockNum;
    }

    public String getVerificationProcessID() {
        return verificationProcessID;
    }

    public void setVerificationProcessID(String verificationProcessID) {
        this.verificationProcessID = verificationProcessID;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getSsNum() {
        return ssNum;
    }

    public void setSsNum(String ssNum) {
        this.ssNum = ssNum;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getDiagnose() {
        return diagnose;
    }

    public void setDiagnose(String diagnose) {
        this.diagnose = diagnose;
    }

    public String getTreat() {
        return treat;
    }

    public void setTreat(String treat) {
        this.treat = treat;
    }

    public String getRx() {
        return rx;
    }

    public void setRx(String rx) {
        this.rx = rx;
    }

    public String getWinningHash() {
        return winningHash;
    }

    public void setWinningHash(String winningHash) {
        this.winningHash = winningHash;
    }

    @Override
    public String toString() {
        return blockID + prevHash + firstname + lastname + ssNum + dateOfBirth + diagnose + treat + rx;
    }
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

class Utils{
    public static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
        rng.setSeed(seed);
        keyGenerator.initialize(1024, rng);

        return (keyGenerator.generateKeyPair());
    }


    public static void wait(int milliseconds){
        try{
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
