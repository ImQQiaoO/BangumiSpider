public class RankItemsInfo {
    private int itemsID;
    private int VIBScorerNum;
    private double VIBAver;
    private int commonScorerNum;
    private double commonAver;
    private String workName;
    private String rank;
    private String epNum;
    private String date;

    public RankItemsInfo() {
    }

    public RankItemsInfo(int itemsID, int VIBScorerNum, double VIBAver, int commonScorerNum, double commonAver, String workName, String rank, String epNum, String date) {
        this.itemsID = itemsID;
        this.VIBScorerNum = VIBScorerNum;
        this.VIBAver = VIBAver;
        this.commonScorerNum = commonScorerNum;
        this.commonAver = commonAver;
        this.workName = workName;
        this.rank = rank;
        this.epNum = epNum;
        this.date = date;
    }


    public int getItemsID() {
        return itemsID;
    }

    public void setItemsID(int itemsID) {
        this.itemsID = itemsID;
    }

    public int getVIBScorerNum() {
        return VIBScorerNum;
    }

    public void setVIBScorerNum(int VIBScorerNum) {
        this.VIBScorerNum = VIBScorerNum;
    }

    public double getVIBAver() {
        return VIBAver;
    }

    public void setVIBAver(double VIBAver) {
        this.VIBAver = VIBAver;
    }

    public int getCommonScorerNum() {
        return commonScorerNum;
    }

    public void setCommonScorerNum(int commonScorerNum) {
        this.commonScorerNum = commonScorerNum;
    }

    public double getCommonAver() {
        return commonAver;
    }

    public void setCommonAver(double commonAver) {
        this.commonAver = commonAver;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getEpNum() {
        return epNum;
    }

    public void setEpNum(String epNum) {
        this.epNum = epNum;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}