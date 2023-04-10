import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RankSpider {

    private static final int sleepTime = 5000;
    private static final ArrayList<RankItemsInfo> rankItemsInfoList = new ArrayList<>(); //保留

    public static void main(String[] args) throws Exception {

        System.out.print("请输入开始访问的页数：");
        Scanner scStartPage = new Scanner(System.in);
        int startPage = scStartPage.nextInt();

        System.out.print("请输入需要访问的最大页数：");
        Scanner scMaxPage = new Scanner(System.in);
        int maxPage = scMaxPage.nextInt();

        System.out.println("请输入进行访问的Cookie和User-Agent:");
        Scanner scCookie = new Scanner(System.in);
        String cookie = scCookie.nextLine();
        Scanner scUserAgent = new Scanner(System.in);
        String userAgent = scUserAgent.nextLine();

        //如果从第一页开始爬虫，则认为用户首次进行爬虫
        //如果不是从第一页开始爬虫，则认为用户进行过爬虫动作，由于某些原因中断，而继续进行爬虫
        if(startPage == 1) {
            String fileName = ".\\bangumiRanking0.csv";
            Path path = Paths.get(fileName);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
                bufferedWriter.write("");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                bufferedWriter.write(dateFormat.format(date) + "\n");
                bufferedWriter.write("作品名,作品ID,作品均分,作品评分人数,上映时间,作品原排名,VIB评分人数,VIB均分\n");
            }
        }

        for (int i = startPage; i < maxPage + 1; i++) {
            Thread.sleep(sleepTime);
            getAllItemsID(i, cookie, userAgent);
        }
    }

    public static void getAllItemsID(int i, String cookie, String userAgent) throws Exception {

        URL url = new URL("https://bgm.tv/anime/browser?sort=rank&page=" + i);
        HttpsURLConnection getRankConnection = (HttpsURLConnection) url.openConnection();
        HttpsURLConnection.setFollowRedirects(true);
        BufferedReader br = new BufferedReader(new InputStreamReader(getRankConnection.getInputStream()));
        String line;

        String IDRegex = "item_\\d{0,6}";
        String workNameRegex = "a href=\"/subject/.*\" class=\"l\">.*</a";
        String rankNumRegex = "/small>\\d*</span";
        String rankDateRegex = ".*/ .* </p>";

        Pattern IDPAttern = Pattern.compile(IDRegex);
        Pattern workNamePattern = Pattern.compile(workNameRegex);
        Pattern rankNumPattern = Pattern.compile(rankNumRegex);
        Pattern rankDatePattern = Pattern.compile(rankDateRegex);

        ArrayList<Object> itemsList = new ArrayList<>();

        while ((line = br.readLine()) != null) {

            boolean IDMarker = false;

            Matcher IDAMatcher = IDPAttern.matcher(line);
            Matcher workNameMatcher = workNamePattern.matcher(line);
            Matcher rankNumMatcher = rankNumPattern.matcher(line);
            Matcher rankDateMatcher = rankDatePattern.matcher(line);

            int ID = 0;

            while (IDAMatcher.find()) {
                ID = Integer.parseInt(IDAMatcher.group().substring(IDAMatcher.group().indexOf("_") + 1));
                itemsList.add(ID);
                IDMarker = true;
            }
            if (IDMarker) {
                Main.getVIBScore(ID, cookie, userAgent, i);
                itemsList.addAll(Main.singleItemsList);
                Main.singleItemsList.clear();

                itemsList.addAll(Main.commonMarkList);
                Main.commonMarkList.clear();
            }
            while (workNameMatcher.find()) {
                itemsList.add(workNameMatcher.group().substring(workNameMatcher.group().indexOf(">") + 1, workNameMatcher.group().indexOf("<")));
            }
            while (rankNumMatcher.find()) {
                itemsList.add(rankNumMatcher.group().substring(rankNumMatcher.group().indexOf(">") + 1, rankNumMatcher.group().indexOf("<")));
            }
            while (rankDateMatcher.find()) {
                String dateContent = rankDateMatcher.group().replace(" ", "").replace("</p>", "");
                if (dateContent.contains("话")) {
                    dateContent = dateContent.substring(dateContent.indexOf("/") + 1);
                }
                if (dateContent.contains("/")) {
                    dateContent = dateContent.substring(0, dateContent.indexOf("/"));
                }
                itemsList.add(dateContent);
            }
        }

        for (Object o : itemsList) {
            System.out.println(o);
        }
        for (int j = 0; j < itemsList.size() / 8; j++) {
            RankItemsInfo rankItemsInfo = new RankItemsInfo((int) itemsList.get(j * 8), (int) itemsList.get(j * 8 + 1), (Double) itemsList.get(j * 8 + 2),
                    (int) itemsList.get(j * 8 + 3), (Double) itemsList.get(j * 8 + 4), (String) itemsList.get(j * 8 + 5), (String) itemsList.get(j * 8 + 6),
                    (String) itemsList.get(j * 8 + 7));
            rankItemsInfoList.add(rankItemsInfo);
            ArrayList<RankItemsInfo> rankItemsInfoPageList = new ArrayList<>();
            rankItemsInfoPageList.add(rankItemsInfo);
            rankTakeOutCSV(rankItemsInfoPageList);
        }

    }

    private static void rankTakeOutCSV(ArrayList<RankItemsInfo> rankItemsInfoPageList) {

        String fileName = ".\\bangumiRanking0.csv";
        Path path = Paths.get(fileName);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            for (RankItemsInfo rankItemsInfo : rankItemsInfoPageList) {
                bufferedWriter.write(rankItemsInfo.getWorkName() + "," + rankItemsInfo.getItemsID() + "," +
                        rankItemsInfo.getCommonAver() + "," + rankItemsInfo.getCommonScorerNum() + "," + rankItemsInfo.getDate()
                        + "," + rankItemsInfo.getRank() + "," + rankItemsInfo.getVIBScorerNum() + "," + rankItemsInfo.getVIBAver() + "\n");
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}