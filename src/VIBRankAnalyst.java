import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class VIBRankAnalyst {

    private static final double m = 100.0;
    private static final ArrayList<VIBRankItemsInfo> VIBRankItemsInfoList = new ArrayList<>();

    public static void main(String[] args) throws IOException {
//        rankingCSVReader();
        ArrayList<RankItemsInfo> rankItemsInfoArrayList = new ArrayList<>();
        ArrayList<Double> weightAverList = new ArrayList<>();

        String fileName = ".\\bangumiVIBWeightedRanking.csv";
        Path path = Paths.get(fileName);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write("");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        FileReader file = new FileReader("..\\BangumiSpider\\bangumiRanking0.csv");
        BufferedReader buffer = new BufferedReader(file);
        //read the 1st line
        String dateLine = buffer.readLine();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            bufferedWriter.write(dateLine + "\n");
            bufferedWriter.write("作品名,作品ID,作品均分,作品评分人数,作品集数,上映时间,VIB评分人数,VIB均分,VIB加权平均,作品原排名,VIB加权排名\n");
        }

        for (int i = 0; i < rankingCSVReader().size(); i++) {
            if (rankingCSVReader().get(i).getVIBScorerNum() != -1) {
                rankItemsInfoArrayList.add(rankingCSVReader().get(i));
            }
        }
        int cnt = rankItemsInfoArrayList.size();
        double totalAverage = 0.0;
        for (RankItemsInfo rankItemsInfo : rankItemsInfoArrayList) {
//            System.out.println(rankItemsInfoArrayList.get(i).getWorkName() + " " + rankItemsInfoArrayList.get(i).getVIBScorerNum());
            totalAverage = totalAverage + rankItemsInfo.getVIBAver();
        }
        double VIBRankAverage = totalAverage / cnt;
        for (RankItemsInfo rankItemsInfo : rankItemsInfoArrayList) {
            double weightVIBAver = (rankItemsInfo.getVIBScorerNum() / (rankItemsInfo.getVIBScorerNum() + m)) * rankItemsInfo.getVIBAver() + (m / (rankItemsInfo.getVIBScorerNum() + m)) * VIBRankAverage;
            weightAverList.add(weightVIBAver);
        }

        for (int i = 0; i < weightAverList.size(); i++) {
            VIBRankItemsInfo vibRankItemsInfo = new VIBRankItemsInfo(rankItemsInfoArrayList.get(i).getItemsID(), rankItemsInfoArrayList.get(i).getVIBScorerNum(), rankItemsInfoArrayList.get(i).getVIBAver(),
                    rankItemsInfoArrayList.get(i).getCommonScorerNum(), rankItemsInfoArrayList.get(i).getCommonAver(), rankItemsInfoArrayList.get(i).getWorkName(), rankItemsInfoArrayList.get(i).getRank(),
                    rankItemsInfoArrayList.get(i).getEpNum(), rankItemsInfoArrayList.get(i).getDate(), weightAverList.get(i));
            VIBRankItemsInfoList.add(vibRankItemsInfo);
            System.out.println(weightAverList.get(i));
        }

        sortWeightedAver();

        takeoutVIBWeightRank();

    }

    private static void takeoutVIBWeightRank() {
        String fileName = ".\\bangumiVIBWeightedRanking.csv";
        Path path = Paths.get(fileName);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            for (int i = 0; i < VIBRankItemsInfoList.size(); i++) {
                bufferedWriter.write(VIBRankItemsInfoList.get(i).getWorkName() + "," + VIBRankItemsInfoList.get(i).getItemsID() + "," +
                        VIBRankItemsInfoList.get(i).getCommonAver() + "," + VIBRankItemsInfoList.get(i).getCommonScorerNum() + "," + VIBRankItemsInfoList.get(i).getDate()
                        + "," + VIBRankItemsInfoList.get(i).getEpNum() + "," + VIBRankItemsInfoList.get(i).getVIBScorerNum() + "," + VIBRankItemsInfoList.get(i).getVIBAver() + "," +
                        VIBRankItemsInfoList.get(i).getWeightedAverVIB() + "," + VIBRankItemsInfoList.get(i).getRank() + "," + (i + 1) + "\n");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

/*
 *    private static void sortWeightedAver(ArrayList<VIBRankItemsInfo> VIBRankItemsInfoList) {
 *
 *        //接口多态（匿名内部类）
 *        Comparator<VIBRankItemsInfo> vibRankItemsInfoComparator = new Comparator<VIBRankItemsInfo>() {
 *            @Override
 *            public int compare(VIBRankItemsInfo o1, VIBRankItemsInfo o2) {
 *                return Double.compare(o2.getWeightedAverVIB(), o1.getWeightedAverVIB());
 *            }
 *        };
 *        VIBRankItemsInfoList.sort(vibRankItemsInfoComparator);
 *    }
 */

    private static void sortWeightedAver() {

        Comparator<VIBRankItemsInfo> vibRankItemsInfoComparator = (o1, o2) -> Double.compare(o2.getWeightedAverVIB(), o1.getWeightedAverVIB());
        VIBRankAnalyst.VIBRankItemsInfoList.sort(vibRankItemsInfoComparator);
    }

    public static ArrayList<RankItemsInfo> rankingCSVReader() throws IOException {
        String fileName = "..\\BangumiSpider\\bangumiRanking0.csv";
        FileInputStream fileInputStream = new FileInputStream(fileName);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String strLine;
        String[] singleItems = new String[9];
        int lineCnt = 0;
        ArrayList<RankItemsInfo> rankItemsInfoArrayList = new ArrayList<>();
        while ((strLine = bufferedReader.readLine()) != null) {
            lineCnt++;
            if (lineCnt > 2) {

//                System.out.println(strLine);
                String item = strLine;
                for (int i = 0; i < singleItems.length - 1; i++) {
                    singleItems[i] = item.substring(0, item.indexOf(","));
                    item = item.substring(item.indexOf(",") + 1);
                }
                singleItems[singleItems.length - 1] = item;

                RankItemsInfo rankItemsInfo = new RankItemsInfo(Integer.parseInt(singleItems[1]), Integer.parseInt(singleItems[7]), Double.parseDouble(singleItems[8]),
                        Integer.parseInt(singleItems[3]), Double.parseDouble(singleItems[2]), singleItems[0], singleItems[6], singleItems[4], singleItems[5]);
                rankItemsInfoArrayList.add(rankItemsInfo);
            }
        }
        return rankItemsInfoArrayList;
    }
}