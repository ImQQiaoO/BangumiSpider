public class VIBRankItemsInfo extends RankItemsInfo{
    private double weightedAverVIB;

    public VIBRankItemsInfo() {
    }

    public VIBRankItemsInfo(int itemsID, int VIBScorerNum, double VIBAver, int commonScorerNum, double commonAver, String workName, String rank, String epNum, String date, double weightedAverVIB) {
        super(itemsID, VIBScorerNum, VIBAver, commonScorerNum, commonAver, workName, rank, epNum, date);
        this.weightedAverVIB = weightedAverVIB;
    }

    public double getWeightedAverVIB() {
        return weightedAverVIB;
    }

    public void setWeightedAverVIB(double weightedAverVIB) {
        this.weightedAverVIB = weightedAverVIB;
    }
}
