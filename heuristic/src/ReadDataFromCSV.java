import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ReadDataFromCSV {
    private BufferedReader reader;
    private String[] tempStringsSet;
    private int Dim; 

    static public Double[][] readDoubleToMatrix(String path) throws IOException{
        // readData from csv
        ArrayList<Double[]> dataInList = new ArrayList<Double[]>();
        try {
            ReadDataFromCSV csvReader = new ReadDataFromCSV(path);
            csvReader.readDoubleValues(dataInList);
            csvReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  trun2Matirx(dataInList);
    }


    public ReadDataFromCSV(String path) throws IOException{
        File infile = new File(path);
        try{
            BufferedReader reader = new BufferedReader(new FileReader(infile));
            this.reader = reader;
        }finally{
            //nothing
        }
    }

    private void readCsvLine() throws IOException{
        String line = reader.readLine();
        this.tempStringsSet = line.split(",");
        this.Dim = this.tempStringsSet.length;
    }

    public static boolean isNumeric(String str) {
        // use the Java class BigDecimal to check if it is a string
        try {
            new BigDecimal(str).toString();
        } catch (Exception e){
            return false;
        }
        return true;
    }    


    public static boolean isInt(String str) {
        // use the regex to check if it is a string
        Pattern pattern = Pattern.compile("-?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    public void readDoubleValues(ArrayList<Double[]> Valueslist) throws IOException{
        while(reader.ready()){
            readCsvLine();
            Double[] temp = new Double[Dim];
            for (int i = 0; i < Dim; i++){
                temp[i] = Double.valueOf(tempStringsSet[i]);
            }
            Valueslist.add(temp);
        }
    }

    // Force to read as IntValues, no matter double or int.
    public void readAsIntValues(ArrayList<Integer[]> Valueslist) throws IOException{
        while(reader.ready()){
            readCsvLine();
            Integer[] temp = new Integer[Dim];
            for (int i = 0; i < Dim; i++){
                temp[i] = Double.valueOf(tempStringsSet[i]).intValue();
            }
            Valueslist.add(temp);
        }
    }

    // read as IntValues, only when it is int.
    public void readIntValues(ArrayList<Integer[]> Valueslist) throws IOException{
        while(reader.ready()){
            readCsvLine();
            Integer[] temp = new Integer[Dim];
            System.out.println("Dim = "+Dim);
            for (int i = 0; i < Dim; i++){
                System.out.print(tempStringsSet[i]+", ");
                temp[i] = Integer.valueOf(tempStringsSet[i]);
            }
            System.out.println();
            Valueslist.add(temp);
        }
    }

    //Succeed.
    public static Double[][] trun2Matirx(ArrayList<Double[]> Valueslist){
        int numOfRow = Valueslist.size();
        // System.out.println(numOfRow);
        Double[][] outputMatrix = new Double[numOfRow][];
        for (int i = 0; i < numOfRow; i++) {
            outputMatrix[i] = Valueslist.get(i);
        }

        return outputMatrix;
    }

    public boolean close() throws IOException{
        try{
            this.reader.close();
            return true;
        }finally{
            //nothing
        }
    }
    
}
