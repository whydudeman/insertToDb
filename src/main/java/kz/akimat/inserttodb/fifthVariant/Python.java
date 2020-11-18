package kz.akimat.inserttodb.fifthVariant;

import kz.akimat.inserttodb.firstVariant.Main;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.sql.SQLException;

public class Python {
    public static void main(String... strings) throws IOException, SQLException {
       int rowNumber=5;
       int columnNumber=5;
       int totalCells=5*5;
       int[][] twoDimension=new int[rowNumber][columnNumber];
       for(int i=0;i<rowNumber;i++){
           for(int j=0;j<columnNumber;j++){
               int steps=getRandomNumber(0,totalCells);
               i=steps/(rowNumber)-i;
               j=(steps%columnNumber)-j;
           }
       }

    }

    public static int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
