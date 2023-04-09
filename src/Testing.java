import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Testing {
    public static void main(String[] args) throws IOException, InterruptedException {
//        Scanner sc = new Scanner(System.in);
//        String cookie = sc.nextLine();
//        Main.getVIBScore(13871,cookie,"");
        int i = 15; //15
        getAllItemsID(i);
    }

    public static void getAllItemsID(int i) throws IOException {

        URL url = new URL("https://bgm.tv/anime/browser?sort=rank&page=" + i);
        HttpsURLConnection getRankConnection = (HttpsURLConnection) url.openConnection();
        HttpsURLConnection.setFollowRedirects(true);
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(getRankConnection.getInputStream()));
        String rankDateRegex = ".*/ .* </p>";

        Pattern rankDatePattern = Pattern.compile(rankDateRegex);

        ArrayList<Object> itemsList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            Matcher rankDateMatcher = rankDatePattern.matcher(line);
            while (rankDateMatcher.find()) {

                String dateContent = rankDateMatcher.group().replace(" ", "").replace("</p>", "");
                String epNum;
                if (dateContent.contains("话")) {
                    epNum = dateContent.substring(0, dateContent.indexOf("话") + 1);
                    dateContent = dateContent.substring(dateContent.indexOf("/") + 1);
                } else {
                    epNum = "--话";
                }
                if (dateContent.contains("/")) {
                    dateContent = dateContent.substring(0, dateContent.indexOf("/"));
                }
                itemsList.add(epNum);
                itemsList.add(dateContent);
            }
        }
        outputPrinter(itemsList);
    }

    private static void outputPrinter(ArrayList<Object> itemsList) {
        System.out.println("----------------------------");
        for (int j = 0; j < itemsList.size() / 2; j++) {
            System.out.println((j + 1) + "\t" + itemsList.get(2 * j) + "\t" + itemsList.get(2 * j + 1));
        }
    }
}
