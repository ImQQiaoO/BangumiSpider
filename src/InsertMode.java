import java.io.*;
import java.rmi.RemoteException;
import java.util.Scanner;

public class InsertMode {
    public static int modeChooseInput;

    public void modeChoose(){
        System.out.println("请选择爬取模式：");
        System.out.println(" - 1.爬取此人收藏的所有条目信息，并对原文件进行覆盖（如果存在原文件的话）；");
        System.out.println(" - 2.爬取此人在默认收藏页面下，未存在于原文件中的条目信息，并插入到原文件中，这种方式更加节省时间");
        System.out.println("     ，但是项目根目录下必须存在此人的.csv文件。且此.csv文件格式未被修改，且此人最少有一个条目已被成功爬取：");
        System.out.print("请输入数字1或2：");
        String modeChooseInputStr = new Scanner(System.in).nextLine();
        while (!modeChooseInputStr.equals("1") && !modeChooseInputStr.equals("2")) {
            System.out.println("输入错误，请重新输入：");
            modeChooseInputStr = new Scanner(System.in).nextLine();
        }
        InsertMode.modeChooseInput = Integer.parseInt(modeChooseInputStr);
    }

    public int getTargetIndex(String uid) throws IOException {
        if (InsertMode.modeChooseInput == 1) {
            return -1;
        }
        // 读取文件，获取最后一行的index
        String filePath = ".\\" + uid + ".csv";
        LineNumberReader lineNumberReader = new LineNumberReader(new BufferedReader(new FileReader(filePath)));
        String strLine;
        String insertTargetIndex = "";
        while ((strLine = lineNumberReader.readLine()) != null) {
            // 由于未改动的文件第三行是上次爬取时或得到的最新的条目的index，所以这里直接拿到第三行
            if (lineNumberReader.getLineNumber() == 3) {
                insertTargetIndex = strLine.split(",")[1];
            }
        }
        if (insertTargetIndex.isEmpty()) {
            throw new RemoteException("文件格式错误，无法获取最后一行的index");
        }
        return Integer.parseInt(insertTargetIndex);
    }
}
