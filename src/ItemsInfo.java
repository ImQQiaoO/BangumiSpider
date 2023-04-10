public class ItemsInfo {
    private int ID;
    private String workName;
    private String userScore;
    private String collectedDate;
    private String userComment;
    private String workRanking;
    private double workAverage;
    private int scorerNum;
    private int VIBScorerNum;
    private double VIBAverage;

    public ItemsInfo() {
    }

    public ItemsInfo(int ID, String workName, String userScore, String collectedDate, String userComment,
                     String workRanking, double workAverage, int scorerNum, int VIBScorerNum, double VIBAverage) {
        this.ID = ID;
        this.workName = workName;
        this.userScore = userScore;
        this.collectedDate = collectedDate;
        this.userComment = userComment;
        this.workRanking = workRanking;
        this.workAverage = workAverage;
        this.scorerNum = scorerNum;
        this.VIBScorerNum = VIBScorerNum;
        this.VIBAverage = VIBAverage;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public String getUserScore() {
        return userScore;
    }

    public void setUserScore(String userScore) {
        this.userScore = userScore;
    }

    public String getCollectedDate() {
        return collectedDate;
    }

    public void setCollectedDate(String collectedDate) {
        this.collectedDate = collectedDate;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getWorkRanking() {
        return workRanking;
    }

    public void setWorkRanking(String workRanking) {
        this.workRanking = workRanking;
    }

    public double getWorkAverage() {
        return workAverage;
    }

    public void setWorkAverage(double workAverage) {
        this.workAverage = workAverage;
    }

    public int getScorerNum() {
        return scorerNum;
    }

    public void setScorerNum(int scorerNum) {
        this.scorerNum = scorerNum;
    }

    public int getVIBScorerNum() {
        return VIBScorerNum;
    }

    public void setVIBScorerNum(int VIBScorerNum) {
        this.VIBScorerNum = VIBScorerNum;
    }

    public double getVIBAverage() {
        return VIBAverage;
    }

    public void setVIBAverage(double VIBAverage) {
        this.VIBAverage = VIBAverage;
    }
}