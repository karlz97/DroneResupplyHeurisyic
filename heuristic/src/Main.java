import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<Integer[]> testValue = new ArrayList<Integer[]>();
        try {
            ReadDataFromCSV csvReader = new ReadDataFromCSV("exNODES.csv");
            csvReader.readAsIntValues(testValue);
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < testValue.size(); i++) {
            for (int j = 0; j <testValue.get(0).length; j++) {  //testValue.get(0) 第一行元素的长度
                System.out.print(testValue.get(i)[j]+"\t");
            }
            System.out.println();
        }

    }
}
