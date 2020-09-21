package kz.akimat.inserttodb.fifthVariant;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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
    public List<String> userNames;

    public ExcellModel(Row row) {

        this.protocolPoint=getIntegerFromRowByIndex(row.getCell(0));
        System.out.println("№: "+protocolPoint);
        String taskTextNotFiltered=getStringFromRowByIndex(row.getCell(1));//Поручение
        this.taskText = taskTextNotFiltered.substring(0,taskTextNotFiltered.indexOf("(Поручение")).trim();
        System.out.println("Task Text: "+taskText);
        System.out.println("Protocol Date: "+getProtocolDate(taskTextNotFiltered));
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
        String usernameString = getStringFromRowByIndex(row.getCell(8));//Ответственный за координацию (замакима)
        List<String> names=new ArrayList<>();
        List<String> usernames=new ArrayList<>(Arrays.asList(usernameString.split(",")));
        for(String username:usernames){
            names.add(username.trim());
        }
        this.userNames=names;

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
                if(!cell.getStringCellValue().isEmpty() || getDateFromString(cell.getStringCellValue())!=null)
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
        return null;

    }

    private static Date getEndOfDate(Date date) {
        if(date!=null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }
        return null;
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
