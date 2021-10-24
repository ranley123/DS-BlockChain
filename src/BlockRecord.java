public class BlockRecord{
    String blockID;
    String signedBlockId;
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

    public String getSignedBlockId() {
        return signedBlockId;
    }

    public void setSignedBlockId(String signedBlockId) {
        this.signedBlockId = signedBlockId;
    }

    public String getBlockID() {
        return blockID;
    }

    public void setBlockID(String blockID) {
        this.blockID = blockID;
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