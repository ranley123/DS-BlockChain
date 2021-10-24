import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

public class Blockchain {
    public static int TOTALPROCESSES = 3;
    public static List<PublicKeyMsg> pkList = new LinkedList<>();
    public static KeyPair keyPair;
    public static PrivateKey privteKey;
    public static int processID = 0;
    public static boolean processBegin = false;
    public static List<BlockRecord> blockchain = new LinkedList<>();
    public static Comparator<BlockRecord> comparator = new Comparator<BlockRecord>() {
        @Override
        public int compare(BlockRecord blockRecord1, BlockRecord blockRecord2) {
            String date1 = blockRecord1.getTimestamp();
            String date2 = blockRecord2.getTimestamp();
            if (date1.equals(date2)) {
                return 0;
            }
            if (date1 == null) {
                return -1;
            }
            if (date2 == null) {
                return 1;
            }
            return date1.compareTo(date2);
        }
    };
    public static PriorityBlockingQueue<BlockRecord> unverifiedBlockQueue = new PriorityBlockingQueue<>(11, comparator);



    public PublicKeyMsg initPK(){
        byte[] bytePK = keyPair.getPublic().getEncoded();
        String stringPK = Base64.getEncoder().encodeToString(bytePK);
        PublicKeyMsg msg = new PublicKeyMsg(processID, stringPK);
        return msg;
    }

    public void multicastPKs(){
        // init this process's public key
        PublicKeyMsg pkMsg = initPK();
        Gson g = new Gson();

        try{
            for(int i = 0; i < TOTALPROCESSES; i++){
                int curPort = Utils.pkBaseServer + i;
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

        multicastBlock(dummy, true);

    }

    public void multicastBlock(BlockRecord block, boolean isVerified){
        Gson g = new Gson();
        int baseServer = isVerified? Utils.bcBaseServer: Utils.ubBaseServer;
        try{
            for(int i = 0; i < TOTALPROCESSES; i++){
                int curPort = baseServer + i;
                Socket socket = new Socket("localhost", curPort);
                PrintWriter pw = new PrintWriter(socket.getOutputStream());

                String msg = g.toJson(block);
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

    public static boolean hasDuplicateBlock(BlockRecord block){
        for(BlockRecord b: blockchain){
            if(b.getBlockID().equals(block.getBlockID()))
                return true;
        }
        return false;
    }

    public void run() {
        Thread mainServer = new Thread(new MainServer());
        Thread pkServer = new Thread(new PublicKeyServer(Utils.pkBaseServer + processID));
        Thread ubServer = new Thread(new UnverifiedBlockServer(Utils.ubBaseServer + processID));
        Thread bcServer = new Thread(new UpdatedBlockChainServer(Utils.bcBaseServer + processID));

        mainServer.start();
        pkServer.start();
        ubServer.start();
        bcServer.start();

        try{
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("starts process: " + processID);
        if(processID == 2){
            System.out.println("Sending starts signal...");
            processBegin = true;
            try {
                for (int i = 0; i < 1; i++) {
                    int curServerPort = Utils.mainBaseServer + i;
                    Socket socket = new Socket("localhost", curServerPort);
                    PrintWriter pw = new PrintWriter(socket.getOutputStream());
                    String msg = "ready";
                    pw.write(msg);
                    pw.flush();

                    System.out.println("sent ready to port: " + curServerPort);

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
            keyPair = Utils.generateKeyPair(999);
            privteKey = keyPair.getPrivate();

        } catch (Exception e) {
            e.printStackTrace();
        }

        while(!processBegin){
            Utils.wait(1000);
        }
        System.out.println("Process Begins...");
        System.out.println("Sending out Public Keys...");
        multicastPKs();
        Utils.wait(1000);

        for(int i = 0; i < pkList.size(); i++){
            System.out.println(pkList.get(i));
        }

        System.out.println("Init Block Chain...");

        if(processID == 0)
            initBlockchain();

        String filename = String.format("BlockInput%d.txt", processID);
        List<BlockRecord> ubList = Utils.readFile(filename);
        for(BlockRecord block: ubList){
            multicastBlock(block, false);
        }
        Utils.wait(1000);


    }

    public static void main(String[] args){

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

//        System.out.println(processID);

        Utils.setPorts(processID);
        Blockchain main = new Blockchain();
        main.run();
    }

}

class MainServer implements Runnable{
    public void run() {
        // defined queue length
        int queueLength = 6;
        // declaring socket variable
        Socket socket;
        // displaying message on the terminal stating start of main server
        System.out.println("Main server started at: " + Utils.mainServerPort);
        try {
            // assigning serverSocket object - its port number and queue length
            ServerSocket serverSocket = new ServerSocket(Utils.mainServerPort, queueLength);
            while (true) {
                // accepting request
                socket = serverSocket.accept();
                // spawning a new worker thread and invokes worker class run()
                new MainServerWorker(socket).start();
            }
        } catch (IOException ioException) {
            // exception handling
            ioException.printStackTrace();
        }
    }
}

class MainServerWorker extends Thread {
    // declaring socket variable
    Socket socket;

    // constructor declaration - assigning socket to locally defined socket variable
    public MainServerWorker(Socket socket) {
        this.socket = socket;
    }

    // run() method
    public void run() {
        try {
            BufferedReader inputData = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String dataRead = inputData.readLine();
            Blockchain.processBegin = true;
            // closes the socket connection
            socket.close();
        } catch (IOException ioException) {
            // exception handling
            ioException.printStackTrace();
        }
    }
}


