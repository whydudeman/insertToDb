package kz.akimat.inserttodb.thirdVariant;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ExcellModel {
    public Integer protocolPoint;
    public String taskText;
    public String userName;
    public String executor;
    public Date deadline;
    public String status;
    public String result;
    public String protocolNumber;
    public List<String> departments;

    public ExcellModel(Row row, String username) {
        //parsing protocolNumber and protocolPoint from one single String
        String protocolData = getStringFromRowByIndex(row.getCell(0));
        if (protocolData != null && !protocolData.isEmpty()) {
            String protocolPointAsString = protocolData.substring(protocolData.lastIndexOf(".") + 1).trim();
            String protocolNumberParser = protocolData.substring(0, protocolData.lastIndexOf(".")).trim();
            System.out.println(protocolPointAsString + " IAM " + protocolNumberParser);
            this.protocolPoint = Integer.valueOf(protocolPointAsString);
            this.protocolNumber = protocolNumberParser;
        }
        //end of parsing

        this.taskText = getStringFromRowByIndex(row.getCell(1));//Поручение
        this.userName = username;
        this.executor = getStringFromRowByIndex(row.getCell(1));//Ответственный за исполнение
        this.deadline = getDateFromRowByIndex(row.getCell(2));//Срок исполнения
        String statusText = row.getCell(3).getStringCellValue();//Статус
        if (statusText.contains("На исполнении"))
            this.status = "IN_PROGRESS";
        if (statusText.contains("В работе"))
            this.status = "IN_PROGRESS";
        if (statusText.contains("Исполнено"))
            this.status = "DONE";
        if (statusText.contains("Не исполнено"))
            this.status = "NOT_DONE";
        this.result = row.getCell(3).getStringCellValue();//Информация по исполнению
//        List<String> stringList = new ArrayList<>(Arrays.asList(executor.replace("совместно", ",").split(",")));
//        List<String> splitByComma = new ArrayList<>(Arrays.asList(executor.replace("\n", ",").split(",")));
//        if (!splitByComma.isEmpty()) {
//            if (!stringList.isEmpty()) {
//                splitByComma.addAll(stringList);
//            }
//            this.departments = splitByComma;
//        }

    }

    public static String getStringFromRowByIndex(Cell cell) {
        if (cell != null)
            if (cell.getCellType().equals(CellType.STRING))
                return cell.getStringCellValue().trim();
        return "";
    }

    public static Date getDateFromRowByIndex(Cell cell) {
        if (cell != null) {
            if (cell.getCellType().equals(CellType.STRING))
                if(getDateFromString(cell.getStringCellValue())!=null)
                    return getEndOfDate(getDateFromString(cell.getStringCellValue()));
            if (cell.getCellType().equals(CellType.NUMERIC))
                return getEndOfDate(cell.getDateCellValue());
        }
        return null;
    }

    public static Integer getIntegerFromRowByIndex(Cell cell) {
        if (cell != null) {
            if (cell.getCellType().equals(CellType.STRING))
                return Integer.valueOf(cell.getStringCellValue());
            if (cell.getCellType().equals(CellType.NUMERIC))
                return (int) cell.getNumericCellValue();
        }
        return 0;
    }

    public static Date getDateFromString(String date) {
        List<String> formatStrings = Arrays.asList("dd/MM/yyyy", "dd.MM.yyyy");
        for (String formatString : formatStrings) {
            try {
                return new SimpleDateFormat(formatString).parse(date);
            } catch (ParseException e) {
            }
        }
//        System.out.println(date + " IT IS DATE");
        return null;

    }

    private static Date getEndOfDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
