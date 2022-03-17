import java.io.IOException;
import java.util.ArrayList;



public class Main {
    public static void main(String[] args) {
        ArrayList<Integer[]> testValue = new ArrayList<Integer[]>();
        ArrayList<Double[]> testValue2 = new ArrayList<Double[]>();
        try {
            ReadDataFromCSV csvReader = new ReadDataFromCSV("exNODES.csv");
            csvReader.readAsIntValues(testValue);
            csvReader.close();
            ReadDataFromCSV csvReader2 = new ReadDataFromCSV("exNODES.csv");
            csvReader2.readDoubleValues(testValue2);
            csvReader2.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < testValue.size(); i++) {
            for (int j = 0; j <testValue.get(0).length; j++) {  //testValue.get(0) 第一行元素的长度
                System.out.print(testValue.get(i)[j]+"\t");
            }
            System.out.println();
        }


        System.out.println("watchout!");
        Double[][] testMatrix = trun2Matirx(testValue2);
        //just to test:
        for (int i = 0; i < testMatrix.length; i++) {
            for (int j = 0; j <testMatrix[i].length; j++) {  //testValue.get(0) 第一行元素的长度
                System.out.print(testMatrix[i][j]+"\t");
            }
            System.out.println();
        }
    }

    //Succeed.
    public static Double[][] trun2Matirx(ArrayList<Double[]> Valueslist){
        int numOfRow = Valueslist.size();
        System.out.println(numOfRow);
        Double[][] outputMatrix = new Double[numOfRow][];
        for (int i = 0; i < numOfRow; i++) {
            outputMatrix[i] = Valueslist.get(i);
        }

        return outputMatrix;
    }

}
