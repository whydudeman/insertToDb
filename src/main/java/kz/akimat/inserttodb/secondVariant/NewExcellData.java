package kz.akimat.inserttodb.secondVariant;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NewExcellData {
    public Integer protocolPoint;
    public String taskText;
    public List<String> userNames;
    public String executor;
    public Date deadline;
    public String status;
    public String result;
    public String sphere;
    public String protocolTitle;
    public Date protocolDate;
    public String protocolNumber;
    public String protocolType;
    public String districtId;
    public List<String> departments;

    public NewExcellData(Row row) {
//        String extension = getStringFromRowByIndex(row.getCell(0));//Номер
//        System.out.println(extension);
//        String substring = extension.substring(extension.lastIndexOf(".") + 1);
//        System.out.println("SUBSTRING"+substring);

//        System.out.println(taskText);
        String protocolPointFull=getStringFromRowByIndex(row.getCell(0));
        String protocolPointString=protocolPointFull.substring(protocolPointFull.lastIndexOf(",") + 1);
        this.protocolPoint = Integer.valueOf(protocolPointString);
        this.taskText = getStringFromRowByIndex(row.getCell(1));//Поручение
         String usernameString = getStringFromRowByIndex(row.getCell(2));//Ответственный за координацию (замакима)
        List<String> names=new ArrayList<>();
        List<String> usernames=new ArrayList<>(Arrays.asList(usernameString.split(",")));
        for(String username:usernames){
            names.add(username.trim());
        }
        this.userNames=names;
        this.executor = getStringFromRowByIndex(row.getCell(3));//Ответственный за исполнение
        this.deadline = getDateFromRowByIndex(row.getCell(4));//Срок исполнения
        String statusText = row.getCell(6).getStringCellValue();//Статус
        if (statusText.contains("На исполнении"))
            this.status = "IN_PROGRESS";
        if (statusText.contains("В работе"))
            this.status = "IN_PROGRESS";
        if (statusText.contains("Исполнено"))
            this.status = "DONE";
        if (statusText.contains("Не исполнено"))
            this.status = "NOT_DONE";
        this.result = row.getCell(7).getStringCellValue();//Информация по исполнению
        this.sphere = getStringFromRowByIndex(row.getCell(8));//Сфера
        this.protocolTitle = getStringFromRowByIndex(row.getCell(10));//Наименование протокола
        this.protocolDate = getDateFromRowByIndex(row.getCell(9));//Дата протокола
        this.protocolNumber = getStringFromRowByIndex(row.getCell(11));//Номер протокола
        this.protocolType = getStringFromRowByIndex(row.getCell(12));//Тип протокола
        this.districtId = districtId;
        List<String> stringList = new ArrayList<>(Arrays.asList(executor.replace("совместно", ",").split(",")));
        List<String> splitByComma = new ArrayList<>(Arrays.asList(executor.replace("\n", ",").split(",")));

        if (!splitByComma.isEmpty()) {
            if (!stringList.isEmpty()) {
                splitByComma.addAll(stringList);
            }
            this.departments = splitByComma;
        }

        System.out.println(protocolPoint+" "+protocolNumber+" "+sphere+" "+status);
    }

    private String getStringFromRowByIndex(Cell cell) {
        if (cell != null)
            if (cell.getCellType().equals(CellType.STRING))
                return cell.getStringCellValue().trim();
        return "";
    }

    private Date getDateFromRowByIndex(Cell cell) {
        if (cell != null) {
            if (cell.getCellType().equals(CellType.STRING))
                return getDateFromString(cell.getStringCellValue());
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getDateCellValue();
        }
        return null;
    }

    private Integer getIntegerFromRowByIndex(Cell cell) {
        if (cell != null) {
            if (cell.getCellType().equals(CellType.STRING))
                return Integer.valueOf(cell.getStringCellValue());
            if (cell.getCellType().equals(CellType.NUMERIC))
                return (int) cell.getNumericCellValue();
        }
        return 0;
    }

    private Date getDateFromString(String date) {
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
}
