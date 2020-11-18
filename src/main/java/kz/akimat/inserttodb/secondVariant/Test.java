package kz.akimat.inserttodb.secondVariant;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.IOException;
import java.sql.SQLException;

public class Test {
    public static void main(String... strings) throws IOException, SQLException {
        String protocolData ="2020-7,5";
        String protocolPointAsString = protocolData.substring(protocolData.lastIndexOf(",") + 1);
        String protocolNumberParser = protocolData.substring(0, protocolData.lastIndexOf(","));
        System.out.println(protocolPointAsString+"  "+protocolNumberParser);
    }

}
