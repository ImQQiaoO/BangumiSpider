import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {

    private static int pageNum = 1;
    private static final int sleepTime = 8000; //访问间隔
    private static final int TOTAL_ITEMS_CATEGORY_COUNTER = 5; //方便扩容或删减程序
    private static final int SINGLE_ITEMS_CATEGORY_COUNTER = 5; //方便扩容或删减程序
    private static final ArrayList<Object> totalItemsList = new ArrayList<>();
    static ArrayList<Object> singleItemsList = new ArrayList<>();
    static ArrayList<Object> commonMarkList = new ArrayList<>();
    private static final ArrayList<Object> releaseDateList = new ArrayList<>();
    private static final ArrayList<ItemsInfo> objectItemsList = new ArrayList<>();
    private static int insertTargetIndex = -1;
    private static boolean reachTarget = false;

    public static void main(String[] args) throws Exception {

        String uid = checkin();
//        String uid = "756774"; //todo:输入用户UID

        // 选择爬取模式
        modeChoose(uid);

        WebConfigs webConfigs = webConfigs();

        URL url = new URL("https://bgm.tv/anime/list/" + uid + "/collect?page=1"); //TODO: 请在开发完成后将此行删除，并将此行的前两行注释取消。
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(); //

        HttpsURLConnection.setFollowRedirects(true);
        connection.setRequestProperty("Cookie", "");
        connection.setSSLSocketFactory(webConfigs.sslContext().getSocketFactory());
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
        connection.setDoInput(true);

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line = null;

        String itemRegex = "item_\\d{0,6}";
        String userScoreRegex = "<span class=\"starlight stars\\d{1,2}";
        String titleRegex = "<a href=\"/subject/\\d{0,6}\" class=\"l\">.+</a>";
        String collectedDateRegex = "<span class=\"tip_j\">.{8,10}</span>";
        String commentRegex = "<div class=\"text\"> .*</div>";
        // 添加：获取该条目的上映日期
        String releaseDateRegex = ".*/ .* </p>";

        Pattern pattern = Pattern.compile(itemRegex);
        Pattern userScorePattern = Pattern.compile(userScoreRegex);
        Pattern titlePattern = Pattern.compile(titleRegex);
        Pattern collectedDatePattern = Pattern.compile(collectedDateRegex);
        Pattern commentPattern = Pattern.compile(commentRegex);
        Pattern releaseDatePattern = Pattern.compile(releaseDateRegex);

        while ((line = br.readLine()) != null) {

            listsAdder(pattern, userScorePattern, titlePattern, collectedDatePattern, commentPattern, releaseDatePattern, line);

            if (line.startsWith("</li></ul><div id=\"multipage\"><div class=\"page_inner\"><strong class=\"p_cur\">1</strong>")) {
                pageNum = maxPageFinder(line);
            }

        }

//      如果当前用户页面数量大于2，将页码大于2的页面中全部条目获取出来
        if (pageNum >= 2) {
            for (int i = 2; i <= pageNum; i++) {
                Thread.sleep(sleepTime);
//                System.out.println("Page" + i + ": ");
//                URL urlNext = new URL("https://bgm.tv/anime/list/" + uid + "/collect?page=1");
                URL urlNext = new URL("https://bgm.tv/anime/list/" + uid + "/collect?page=" + i);

                connection = (HttpsURLConnection) urlNext.openConnection();
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {

                    listsAdder(pattern, userScorePattern, titlePattern, collectedDatePattern, commentPattern,releaseDatePattern, line);

                    if (line.startsWith("</li></ul><div id=\"multipage\"><div class=\"page_inner\"><strong class=\"p_cur\">1</strong>")) {
                        pageNum = maxPageFinder(line);
                    }
                }
            }
        }

        br.close();
//        fileWriter();

        //访问所有条目所处的站点，爬取条目评分人数、作品排名，并计算该条目的平均分
        itemsErgodic(webConfigs.cookie(), webConfigs.userAgent());

        //获取此用户的注册时间
        String regDate = getRegDate(uid);

        //面向对象编程
        getObjectItems();

        //在控制台中输出此人所有信息
        printAllInfo();

        //将所有信息输出保存为.csv格式
        takeOutCSV(uid, regDate);

    }

    private static WebConfigs webConfigs() throws NoSuchAlgorithmException, KeyManagementException {
        System.out.println("请输入进行访问的Cookie和User-Agent:");
        Scanner scCookie = new Scanner(System.in);
        String cookie = scCookie.nextLine();
        Scanner scUserAgent = new Scanner(System.in);
        String userAgent = scUserAgent.nextLine();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustManagers = new X509TrustManager[]{new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                // 检查证书是否过期等其他方面的问题，为了简单起见，这里默认信任所有证书
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }};

        sslContext.init(null, trustManagers, null);
        return new WebConfigs(cookie, userAgent, sslContext);
    }

    private record WebConfigs(String cookie, String userAgent, SSLContext sslContext) {
    }

    private static void modeChoose(String uid) throws IOException {
        InsertMode insertMode = new InsertMode();
        insertMode.modeChoose();
        insertTargetIndex = insertMode.getTargetIndex(uid);
    }

    public static String checkin() {

        System.out.print("请输入您想要查询的用户Bangumi UID: ");
        Scanner sc = new Scanner(System.in);
        return sc.next();
    }

    public static void fileWriter() throws IOException {

        Scanner sc = new Scanner(System.in);
        String website = sc.next();
//        URL url = new URL("https://bgm.tv/anime/list/516549/collect?page=1");
        URL url = new URL(website);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String fileName = ".\\" + sc.next();
        Path path = Paths.get(fileName);
        String readPageLine = null;

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write("");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        while ((readPageLine = br.readLine()) != null) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
                bufferedWriter.write(readPageLine + "\n");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }


    }

    public static void listsAdder(Pattern pattern, Pattern userScorePattern, Pattern titlePattern, Pattern collectedDatePattern,
                                  Pattern commentPattern, Pattern releaseDatePattern, String line) {

        int scoreMarker = 0;
        int commentMarker = 0;
        Matcher matcher = pattern.matcher(line);
        Matcher userScoreMatcher = userScorePattern.matcher(line);
        Matcher titleMatcher = titlePattern.matcher(line);
        Matcher collectedDateMatcher = collectedDatePattern.matcher(line);
        Matcher commentMatcher = commentPattern.matcher(line);
        Matcher releaseDateMatcher = releaseDatePattern.matcher(line);

        // 如果是插入模式，且未到达目标条目，将所有条目ID加入totalItemsList
        if (!reachTarget || InsertMode.modeChooseInput == 1) {

            while (matcher.find()) {
                int itemID = Integer.parseInt(matcher.group().substring(matcher.group().indexOf("_") + 1));
                totalItemsList.add(itemID);
                if (itemID == insertTargetIndex) {
                    reachTarget = true;
                    break;
                }

            }
            while (userScoreMatcher.find()) {
                totalItemsList.remove(totalItemsList.size() - 1);
                totalItemsList.add(userScoreMatcher.group().substring(userScoreMatcher.group().lastIndexOf("s") + 1));
                scoreMarker--;
            }
            while (titleMatcher.find()) {
                totalItemsList.add(titleMatcher.group().substring(titleMatcher.group().indexOf(">") + 1, titleMatcher.group().lastIndexOf("<")));
                scoreMarker++;
            }
            if (scoreMarker == 1) {
                totalItemsList.add("-");
            }
            while (collectedDateMatcher.find()) {
                totalItemsList.add(collectedDateMatcher.group().substring(collectedDateMatcher.group().indexOf(">") + 1, collectedDateMatcher.group().lastIndexOf("<")));
                commentMarker++;
            }
            if (commentMarker == 1) {
                totalItemsList.add("*");
            }
            while (commentMatcher.find()) {
                totalItemsList.remove(totalItemsList.size() - 1);
                totalItemsList.add(commentMatcher.group().substring(commentMatcher.group().indexOf(">") + 1, commentMatcher.group().lastIndexOf("<")));
                commentMarker--;
            }
            while (releaseDateMatcher.find()) {
                String dateContent = releaseDateMatcher.group().replace(" ", "").replace("</p>", "");
                System.out.println(dateContent);
//                String epNum;
//                if (dateContent.contains("话")) {
//                    epNum = dateContent.substring(0, dateContent.indexOf("话") + 1);
//                    dateContent = dateContent.substring(dateContent.indexOf("/") + 1);
//                } else {
//                    epNum = "--话";
//                }
                if (dateContent.contains("/")) {
                    dateContent = dateContent.split("/")[1];
                }
//                totalItemsList.add(epNum);
                // 获取到该条目的上映日期，直接创建一个新的List专门存储此条目
                releaseDateList.add(dateContent);
            }
        }
    }

    public static int maxPageFinder(String line) {

        int getPageNum = 0;
//        System.out.println(line);
        if (line.contains("></a><span class=\"p_edge\">(&nbsp;1&nbsp;/&nbsp;") && line.contains("&nbsp;)</span></div></div>")) {
            String pageNumRegex = "&nbsp;/&nbsp;\\d{2,3}&nbsp;";
            Pattern pattern = Pattern.compile(pageNumRegex);
            Matcher matcher = pattern.matcher(line);
            String regEx = "[^0-9]";
            Pattern p = Pattern.compile(regEx);
            while (matcher.find()) {
                String s = matcher.group();
                Matcher m = p.matcher(s);
                String result = m.replaceAll("").trim();
                getPageNum = Integer.parseInt(result);
            }
        } else {
            String pageNumRegex = "\" class=\"p\">\\d{1,2}</a>";
            Pattern pattern = Pattern.compile(pageNumRegex);
            Matcher matcher = pattern.matcher(line);
            String regEx = "[^0-9]";
            Pattern p = Pattern.compile(regEx);
            while (matcher.find()) {
                String s = matcher.group();
                Matcher m = p.matcher(s);
                String result = m.replaceAll("").trim();
                getPageNum = Integer.parseInt(result);
            }
        }
        return getPageNum;
    }

    // TODO IMPORTANT METHOD
    public static void itemsErgodic(String cookie, String userAgent) throws Exception {

        //访问所有条目所处的站点，爬取条目评分人数，计算该条目的平均分
        int[] scorersNum = new int[10];
        for (int i = 0; i <= (totalItemsList.size() / TOTAL_ITEMS_CATEGORY_COUNTER) - 1; i++) {
            Thread.sleep(sleepTime);
            URL urlItems = new URL("https://bgm.tv/subject/" + totalItemsList.get(TOTAL_ITEMS_CATEGORY_COUNTER * i));
            HttpsURLConnection itemsConnection = (HttpsURLConnection) urlItems.openConnection();
            HttpsURLConnection.setFollowRedirects(true);
            itemsConnection.setRequestProperty("cookie", cookie);
            itemsConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0");
            BufferedReader itemsBr = new BufferedReader(new InputStreamReader(itemsConnection.getInputStream()));
            String itemsLine = null;

            String scorersRegex = "% .{4,}\" class=\"textTip\"><span class=\"label\">";
            String rankRegex = "Bangumi Anime Ranked:.*</small";

            Pattern scorerPattern = Pattern.compile(scorersRegex);
            Pattern rankPattern = Pattern.compile(rankRegex);

            int j = 0;

            while ((itemsLine = itemsBr.readLine()) != null) {

                Matcher scorerMatcher = scorerPattern.matcher(itemsLine);
                Matcher rankMatcher = rankPattern.matcher(itemsLine);

                while (scorerMatcher.find()) {
                    scorersNum[j] = Integer.parseInt(scorerMatcher.group().substring(scorerMatcher.group().indexOf("(") + 1,
                            scorerMatcher.group().indexOf("人")));
                    j++;
                }
                while (rankMatcher.find()) {
                    if (rankMatcher.group().contains("#")) {
                        singleItemsList.add(rankMatcher.group().substring(rankMatcher.group().indexOf("#") + 1, rankMatcher.group().lastIndexOf("<")));
                    } else {
                        singleItemsList.add(rankMatcher.group().substring(rankMatcher.group().lastIndexOf(">") + 1, rankMatcher.group().lastIndexOf("<")));
                    }
                }

            }
            int mark = 11;
            double totalMark = 0.0;
            int totalScorer = 0;
            for (int k : scorersNum) {
                mark--;
                totalMark = totalMark + k * mark;
                totalScorer = totalScorer + k;
            }

            if (j == 0) {
                singleItemsList.add("注册未满三个月或非登陆状态无法获取R18条目相关信息");
                singleItemsList.add(-1.00);
                singleItemsList.add(-1);

            } else {
                singleItemsList.add((totalMark / totalScorer));
                singleItemsList.add(totalScorer);
            }

            //New Feature:获取该条目VIB评分
            int midItemsID = (int) totalItemsList.get(TOTAL_ITEMS_CATEGORY_COUNTER * i);
            int rankPageNum = -1;
            getVIBScore(midItemsID, cookie, userAgent, rankPageNum);
        }
    }

    public static String getRegDate(String uid) throws InterruptedException, IOException {

        Thread.sleep(sleepTime);
        String regDate = null;
        URL urlUser = new URL("https://bgm.tv/user/" + uid);
        HttpsURLConnection userConnection = (HttpsURLConnection) urlUser.openConnection();
        BufferedReader regDateBr = new BufferedReader(new InputStreamReader(userConnection.getInputStream()));
        String regDateLine = null;

        String userRegeDateRegex = "<span class=\"tip\">.*</span></li>";

        Pattern userRegedatePattern = Pattern.compile(userRegeDateRegex);

        while ((regDateLine = regDateBr.readLine()) != null) {

            Matcher regdateMatcher = userRegedatePattern.matcher(regDateLine);
            while (regdateMatcher.find()) {
                regDate = regdateMatcher.group().substring(regdateMatcher.group().indexOf(">") + 1, regdateMatcher.group().indexOf("加") - 1);
            }

        }
        return regDate;
    }

    public static void getObjectItems() {

        for (int i = 0; i < (totalItemsList.size() / TOTAL_ITEMS_CATEGORY_COUNTER); i++) {
            ItemsInfo itemsInfo = new ItemsInfo((int) totalItemsList.get(TOTAL_ITEMS_CATEGORY_COUNTER * i),
                    (String) totalItemsList.get(TOTAL_ITEMS_CATEGORY_COUNTER * i + 1), (String) totalItemsList.get(TOTAL_ITEMS_CATEGORY_COUNTER * i + 2),
                    (String) totalItemsList.get(TOTAL_ITEMS_CATEGORY_COUNTER * i + 3), (String) totalItemsList.get(TOTAL_ITEMS_CATEGORY_COUNTER * i + 4),
                    (String) singleItemsList.get(SINGLE_ITEMS_CATEGORY_COUNTER * i), (Double) singleItemsList.get(SINGLE_ITEMS_CATEGORY_COUNTER * i + 1),
                    (int) singleItemsList.get(SINGLE_ITEMS_CATEGORY_COUNTER * i + 2), (int) singleItemsList.get(SINGLE_ITEMS_CATEGORY_COUNTER * i + 3),
                    (Double) singleItemsList.get(SINGLE_ITEMS_CATEGORY_COUNTER * i + 4), (String) releaseDateList.get(i));
            objectItemsList.add(itemsInfo);
        }
    }

    private static void printAllInfo() {

        System.out.println("------------------------------------------------");
        for (ItemsInfo itemsInfo : objectItemsList) {
            System.out.print("作品名: " + itemsInfo.getWorkName() + "\t");
            System.out.print("作品ID: " + itemsInfo.getID() + "\t");
            System.out.print("此人打分:" + itemsInfo.getUserScore() + "\t");
            System.out.print("作品均分:" + itemsInfo.getWorkAverage() + "\t");
            System.out.print("收藏时间:" + itemsInfo.getCollectedDate() + "\t");
            System.out.print("上映时间:" + itemsInfo.getReleaseDate() + "\t");
            System.out.print("作品排名:" + itemsInfo.getWorkRanking() + "\t");
            System.out.print("作品评分人数:" + itemsInfo.getScorerNum() + "\t");
            System.out.print("VIB评分人数:" + itemsInfo.getVIBScorerNum() + "\t");
            System.out.print("VIB均分:" + itemsInfo.getVIBAverage() + "\t");
            System.out.println("此人评论:" + itemsInfo.getUserComment());
        }
    }

    public static void takeOutCSV(String uid, String regDate) throws IOException {

        System.out.println("------------------------------------------------");
        if (InsertMode.modeChooseInput == 2) {
            System.out.println("插入模式已开启，将在表格中插入此人未收录的条目信息。");
        }
        System.out.println("是否需要输出.csv格式的表格？ 是请输入Y(y)，否请输入N(n)。");
        Scanner sc = new Scanner(System.in);
        String takeOutJudge = sc.next().toUpperCase();
        if (takeOutJudge.equals("Y")) {
            String fileName = ".\\" + uid + ".csv";
            Path path = Paths.get(fileName);
            if (InsertMode.modeChooseInput == 1) {
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
                    bufferedWriter.write("");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            if (InsertMode.modeChooseInput == 1) {
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
                    bufferedWriter.write("用户:" + uid + "," + "加入时间:" + regDate + "\n");
                    bufferedWriter.write("作品名,作品ID,此人打分,作品均分,收藏时间,上映时间,作品排名,作品评分人数,VIB评分人数,VIB均分,此人评论,\n");
                    for (ItemsInfo itemsInfo : objectItemsList) {
                        bufferedWriter.write(itemsInfo.getWorkName() + "," + itemsInfo.getID() + "," + itemsInfo.getUserScore() + ","
                                + itemsInfo.getWorkAverage() + "," + itemsInfo.getCollectedDate() + "," + itemsInfo.getReleaseDate() + "," + itemsInfo.getWorkRanking() + ","
                                + itemsInfo.getScorerNum() + "," + itemsInfo.getVIBScorerNum() + "," + itemsInfo.getVIBAverage() + "," + itemsInfo.getUserComment() + "\n");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } else if (InsertMode.modeChooseInput == 2) {   // 插入模式
                // 将文件中的全部内容按行读取：
                ArrayList<String> fileContent = new ArrayList<>();
                try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        fileContent.add(line);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                // 将objectItemsList中的内容插入到集合fileContent的第三个元素处
                for (int i = objectItemsList.size() - 1; i >= 0; i--) {
                    fileContent.add(2, objectItemsList.get(i).getWorkName() + "," + objectItemsList.get(i).getID() + "," + objectItemsList.get(i).getUserScore() + ","
                            + objectItemsList.get(i).getWorkAverage() + "," + objectItemsList.get(i).getCollectedDate() + "," + objectItemsList.get(i).getReleaseDate() + "," + objectItemsList.get(i).getWorkRanking() + ","
                            + objectItemsList.get(i).getScorerNum() + "," + objectItemsList.get(i).getVIBScorerNum() + "," + objectItemsList.get(i).getVIBAverage() + "," + objectItemsList.get(i).getUserComment());
                }
                // 将集合中的内容写入文件
                try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                    for (String s : fileContent) {
                        bufferedWriter.write(s + "\n");
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

        } else if (!takeOutJudge.equals("N")) {
            System.out.println("输入错误，请重新选择。");
            takeOutCSV(uid, regDate);
        }
    }

    public static void getVIBScore(int i, String cookie, String userAgent, int rankPageNum) throws Exception {

        int cnt = 0;
        int VIBMarkerSum = 0;
        double totalVIBMark = 0.0;
        double VIBMarkerAver;

        String[] VIBScorersIndex = new String[10];

        int[] VIBScorersNum = new int[10];

        Thread.sleep(5000);

        URL VIBScore = new URL("https://bgm.tv/subject/" + i + "/stats");
        HttpsURLConnection VIBConnection = (HttpsURLConnection) VIBScore.openConnection();
        HttpsURLConnection.setFollowRedirects(true);
        VIBConnection.setRequestProperty("cookie", cookie);
        VIBConnection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:108.0) Gecko/20100101 Firefox/108.0");
        BufferedReader VIBBr = new BufferedReader(new InputStreamReader(VIBConnection.getInputStream()));
        String VIBLine;

        String VIBContent = null;
        String VIBRegex = "\"chart_root\":\"chartVIB\",\"data\":.*,\"series_set\":";

//        String commonLine;
        String commonDataContent = null;
        String commonDataRegex = "\"chart_root\":\"chartCollectInterestType\",\"data\":.*,\"series_set\"";

        Pattern VIBPattern = Pattern.compile(VIBRegex);

        Pattern commonDataPattern = Pattern.compile(commonDataRegex);

        boolean r18Flag = false;

        while ((VIBLine = VIBBr.readLine()) != null) {

            Matcher VIBMatcher = VIBPattern.matcher(VIBLine);

            while (VIBMatcher.find()) {
                VIBContent = VIBMatcher.group().substring(VIBMatcher.group().indexOf("[") + 1, VIBMatcher.group().indexOf("]"));
                cnt++;
            }

            Matcher commonDataMatcher = commonDataPattern.matcher(VIBLine);
            while (commonDataMatcher.find()) {
                commonDataContent = commonDataMatcher.group().substring(commonDataMatcher.group().indexOf("[") + 1, commonDataMatcher.group().indexOf("]"));
            }

            if (VIBLine.contains("数据库中没有查询您所指定的条目")) {
                r18Flag = true;
            }
        }

        if (cnt != 1) {
            singleItemsList.add(-1);
            singleItemsList.add(-1.0);
        } else {
            System.out.println(VIBContent);

            for (int j = 0; j < 10; j++) {
                VIBScorersIndex[j] = VIBContent.substring(VIBContent.indexOf("{"), (VIBContent.indexOf("}") + 1));
                VIBContent = VIBContent.substring((VIBContent.indexOf("}") + 1));
                if (VIBScorersIndex[j].contains("\"vib\"")) {
                    VIBScorersNum[j] = Integer.parseInt(VIBScorersIndex[j].substring(VIBScorersIndex[j].lastIndexOf(":") + 1,
                            VIBScorersIndex[j].lastIndexOf("}")));
                } else {
                    VIBScorersNum[j] = 0;
                }
            }
            int mark = 11;
            for (int j = 0; j < 10; j++) {
                mark--;
                VIBMarkerSum = VIBMarkerSum + VIBScorersNum[j];
                totalVIBMark = totalVIBMark + mark * VIBScorersNum[j];
            }
            VIBMarkerAver = totalVIBMark / VIBMarkerSum;
            singleItemsList.add(VIBMarkerSum);
            singleItemsList.add(VIBMarkerAver);
        }

        if (!r18Flag) {
            String[] singleMarkInfo = new String[10];
            int[] scorers = new int[10];
            for (int j = 0; j < 10; j++) {
                assert commonDataContent != null;
                singleMarkInfo[j] = commonDataContent.substring(commonDataContent.indexOf("{") + 1, commonDataContent.indexOf("}"));
                if (j < 9) {
                    commonDataContent = commonDataContent.substring(commonDataContent.indexOf("}") + 2);
                }
            }
            for (int m = 0; m < singleMarkInfo.length; m++) {
                Vector<String> scorerNum = new Vector<>();
                if (singleMarkInfo[m].contains(",")) {
                    singleMarkInfo[m] = singleMarkInfo[m].substring(singleMarkInfo[m].indexOf(",") + 1);
                    while (singleMarkInfo[m].contains(",")) {
                        scorerNum.add(singleMarkInfo[m].substring(0, singleMarkInfo[m].indexOf(",")));
                        singleMarkInfo[m] = singleMarkInfo[m].substring(singleMarkInfo[m].indexOf(",") + 1);
                    }
                    scorerNum.add(singleMarkInfo[m]);
                    for (int j = 0; j < scorerNum.size(); j++) {
                        if (scorerNum.get(j).contains("\"1\"")) {
                            scorerNum.remove(j);
                        }
                    }
                    int sumScorers = 0;
                    for (String s : scorerNum) {
                        int getScorers = Integer.parseInt(s.substring(s.indexOf(":") + 1));
                        sumScorers = sumScorers + getScorers;
                        scorers[m] = sumScorers;
                    }
                } else {
                    scorers[m] = 0;
                }
            }
            int mark = 11;
            double sumCommonMark = 0.0;
            int sumCommonScorer = 0;
            double commonAver;
            for (int scorer : scorers) {
                mark--;
                sumCommonScorer = sumCommonScorer + scorer;
                sumCommonMark = sumCommonMark + scorer * mark;
            }
            commonAver = sumCommonMark / sumCommonScorer;
            System.out.println(sumCommonScorer);
            System.out.println(commonAver);
            commonMarkList.add(sumCommonScorer);
            commonMarkList.add(commonAver);
        } else {
            System.out.println("R18 Item");
            commonMarkList.add(-2);
            commonMarkList.add(-2.0);
            if (rankPageNum > 0) {
                throw new RuntimeException("Cookie has expired. The Page Number is " + rankPageNum + ", please try is again.");
            }
        }
    }

}