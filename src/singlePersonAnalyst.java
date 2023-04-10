import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class singlePersonAnalyst {

    static ArrayList<ItemsInfo> itemsInfoList = new ArrayList<>();

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.print("请输入想要查询用户的uid:");
        String uid = sc.next();
        takeOutCSVReader(uid);

        int normalWorkCnt = 0;
        double itemsAbs;
        double totalItemsAbs = 0.0;

        for (ItemsInfo itemsInfo : itemsInfoList) {
            if (itemsInfo.getWorkAverage() > 0 && !itemsInfo.getUserScore().equals("-")) {
                itemsAbs = Math.abs(itemsInfo.getWorkAverage() - Double.parseDouble(itemsInfo.getUserScore()));
                totalItemsAbs = totalItemsAbs + itemsAbs;
                System.out.println(itemsInfo.getWorkName() + " " + itemsInfo.getWorkAverage() + " " + itemsInfo.getUserScore() + "        " + itemsAbs);
                normalWorkCnt++;
            }
        }

//        System.out.println(totalItemsAbs);
        System.out.println();
        System.out.println(normalWorkCnt);
        System.out.println("Average: " + totalItemsAbs / normalWorkCnt);
    }

    public static void takeOutCSVReader(String uid) throws IOException {

        String fileName = "..\\BangumiSpider\\" + uid + ".csv";
        FileInputStream fileInputStream = new FileInputStream(fileName);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String strLine;
        String[] singleItems = new String[10];
        int lineCnt = 0;
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
//                for (String singleItem : singleItems) {
//                    System.out.println(singleItem);
//                }
                ItemsInfo itemsInfo = new ItemsInfo(Integer.parseInt(singleItems[1]), singleItems[0], singleItems[2], singleItems[5], singleItems[9],
                        singleItems[6], Double.parseDouble(singleItems[3]), Integer.parseInt(singleItems[7]), Integer.parseInt(singleItems[8]), Double.parseDouble(singleItems[4]));
                itemsInfoList.add(itemsInfo);
            }

        }
    }
}