public class PublicKeyMsg{
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
