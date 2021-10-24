import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.security.*;
import java.util.*;

public class Utils{
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

    public static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
        rng.setSeed(seed);
        keyGenerator.initialize(1024, rng);

        return (keyGenerator.generateKeyPair());
    }

    public static void writeToJSON() {
        System.out.println("=========> In WriteJSON <=========\n");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(Blockchain.blockchain);
        try (FileWriter fw = new FileWriter("BlockchainLedger.json")) {
            String data = gson.toJson(Blockchain.blockchain);
            fw.write(data);
            fw.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public static void wait(int milliseconds){
        try{
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        //gets the algorithm
        Signature signer = Signature.getInstance("SHA1withRSA");
        //initializes the signature with the private key put in the method call
        signer.initSign(key);
        //updates the data in byte form, getting it ready for signing
        signer.update(data);
        //signs the data
        return (signer.sign());
    }

    public static List<BlockRecord> readFile(String filename) throws Exception {
        List<BlockRecord> list = new LinkedList<>();
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = "";

            while((line = reader.readLine()) != null){
                String[] parts = line.split(" ");
                BlockRecord block = new BlockRecord();
                block.setBlockID(UUID.randomUUID().toString());

                Date date = new Date();
                String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
                String TimeStampString = T1 + "." + Blockchain.processID + "\n";
                block.setTimestamp(TimeStampString);

                byte[] signed = signData(block.getBlockID().getBytes(), Blockchain.privteKey);
                block.setSignedBlockId(Base64.getEncoder().encodeToString(signed));

                block.setFirstname(parts[0]);
                block.setLastname(parts[1]);
                block.setSsNum(parts[2]);
                block.setDateOfBirth(parts[3]);
                block.setDiagnose(parts[4]);
                block.setTreat(parts[5]);
                block.setRx(parts[6]);
                block.setVerificationProcessID(Integer.toString(Blockchain.processID));

                list.add(block);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return list;
    }

    class BRComparator implements Comparator<BlockRecord> {
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
    }
}
