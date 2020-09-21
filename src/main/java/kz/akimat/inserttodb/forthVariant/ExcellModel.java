package kz.akimat.inserttodb.forthVariant;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcellModel {
    public Integer protocolPoint;
    public String taskText;
    public String userName;
    public String executor;
    public Date deadline;
    public String status;
    public String result;
    public String protocolNumber;
    public Date protocolDate;
    public List<String> departments;

    public ExcellModel(Row row, String username) {

        this.protocolPoint=getIntegerFromRowByIndex(row.getCell(0));
        System.out.println("№: "+protocolPoint);
        String taskTextNotFiltered=getStringFromRowByIndex(row.getCell(1));//Поручение
        this.taskText = taskTextNotFiltered.substring(0,taskTextNotFiltered.indexOf("(Поручение")).trim();
        System.out.println("Task Text: "+taskText);
        System.out.println("Protocol Date: "+getProtocolDate(taskTextNotFiltered));
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
        this.protocolDate=getDateFromString(getProtocolDate(taskTextNotFiltered));
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
        System.out.println(date + " IT IS DATE");
        throw new RuntimeException("Could not get Date from String");

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

    private  String getProtocolDate(String taskText) {
        String result="empty";
        Matcher m = Pattern.compile("(\\d{1,2}.\\d{1,2}.\\d{4}|\\d{1,2}.\\d{1,2})", Pattern.CASE_INSENSITIVE).matcher(taskText);
        while (m.find()) {
            result=m.group(1);
        }
        return result;
    }
}
