package kz.akimat.inserttodb.forthVariant;

import kz.akimat.inserttodb.Utils.DbConstants;
import kz.akimat.inserttodb.Utils.User;
import kz.akimat.inserttodb.Utils.UserUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChangeTaskByTaskText {
    public static void main(String... strings) throws IOException, SQLException {
        ChangeTaskByTaskText objExcelFile = new ChangeTaskByTaskText();
        String fileName = "not_done.xlsx";
        String path = "/home/nurbol/Downloads/";
        Workbook workbook = getExcelDocument(fileName, path);
        objExcelFile.processExcelObject(workbook);
    }

    //Gets and reads Excel File and depending of type of document returns Workbook which is Excel object
    private static Workbook getExcelDocument(String fileName, String path) throws IOException {

        File file = new File(path + fileName);

        FileInputStream inputStream = new FileInputStream(file);
        Workbook workbook = null;
        String fileExtensionName = fileName.substring(fileName.indexOf("."));
        if (fileExtensionName.equals(".xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (fileExtensionName.equals(".xls")) {
            workbook = new HSSFWorkbook(inputStream);
        }
        return workbook;
    }

    //
    public void processExcelObject(Workbook workbook) throws SQLException {
        List<User> users = UserUtils.getUsers();
        for (int i = 0; i < Objects.requireNonNull(workbook).getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            int rowCount = sheet.getLastRowNum() - sheet.getFirstRowNum();
            String sheetName = sheet.getSheetName();
            for (int j = 5; j <= rowCount; j++) {
                Row row = sheet.getRow(j);
                String username = "";
                DataFormatter formatter = new DataFormatter();
                String val = formatter.formatCellValue(row.getCell(1));
//                System.out.println(row.getRowNum());
//                System.out.println(val);

                if (val.isEmpty())
                    username = row.getCell(0).getStringCellValue();
                else
                    updateTask(row);

            }
        }

    }

    private void updateTask(Row row) {
        ExcellModel excellModel = new ExcellModel(row, "");
        if (excellModel.protocolPoint != null) {
            List<Task> tasks = getTasksByTaskText(excellModel.taskText, excellModel.protocolDate);
            for (Task task : tasks) {
                String newStatus = ExcellModel.getStringFromRowByIndex(row.getCell(5));
                java.util.Date newDeadline = ExcellModel.getDateFromRowByIndex(row.getCell(6));
                String newResult = ExcellModel.getStringFromRowByIndex(row.getCell(7));
                if (!newResult.isEmpty()) {
                    task.isResultChanged = true;
                    task.result = newResult;
                    task.isChanged = true;
                }
                if (!newStatus.isEmpty()) {
                    task.isStatusChanged = true;
                    task.status = newStatus;
                    if (newStatus.contains("На исполнении") || newStatus.contains("на исполнении"))
                        task.status = "IN_PROGRESS";
                    if (newStatus.contains("В работе") || newStatus.contains("в работе"))
                        task.status = "IN_PROGRESS";
                    if (newStatus.contains("Исполнено") || newStatus.contains("исполнено"))
                        task.status = "DONE";
                    if (newStatus.contains("Не исполнено") || newStatus.contains("не исполнено"))
                        task.status = "NOT_DONE";
                    task.isChanged = true;
                }
                if (newDeadline != null) {
                    task.isDeadlinChanged = true;
                    task.deadline = newDeadline;
                    task.isChanged = true;
                }
                if (task.isChanged) {
                    updateTaskInDB(task);
                    if (getIdOfTaskHistory(task.id) != null) {
                        updateTaskHistoryInDB(task.id);
                    } else {
                        createTaskHistoryInDB(task.id);
                    }
                }
            }
        }
    }

    private Long getIdOfTaskHistory(Long taskId) {
        return getIdWithValueAndQuery(taskId, "SELECT id from task_history where task_id=? and type='TASK_UPDATED'");
    }

    private static void updateTaskHistoryInDB(Long taskId) {
        String SQL_INSERT = "UPDATE task_history s,task t INNER JOIN protocol p ON  p.id = t.protocol_id SET s.updated_at = NOW(), s.DATE = p.date, s.deadline = t.deadline, " +
                "s.status = t.status, s.author_id = t.author_id, s.task_id = t.id, s.type =? \n" +
                "  where t.id=? and type='TASK_UPDATED'";
        try (
                Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        ) {
            statement.setString(1, "TASK_UPDATED");
            statement.setLong(2, taskId);
            statement.executeUpdate();
            // ...
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateTaskInDB(Task task) {
        String SQL_INSERT = task.getQuery();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        java.sql.Timestamp sqlDate = null;
        if (task.isDeadlinChanged) {
            sqlDate = new java.sql.Timestamp(task.deadline.getTime());
            System.out.println(sqlDate);
        }
        try (
                Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        ) {
            if (task.isDeadlinChanged && task.isStatusChanged && task.isResultChanged) {

                System.out.println(sdf.format(task.deadline.getTime()));
                statement.setTimestamp(1, sqlDate);
                statement.setString(2, task.result);
                statement.setString(3, task.status);
                statement.setLong(4, task.id);
                System.out.println("UPDATE: DEADLINE STATUS RESULT CHANGED");
            } else if (task.isDeadlinChanged && task.isStatusChanged) {
                System.out.println(sdf.format(task.deadline.getTime()));
                statement.setTimestamp(1, (sqlDate));
                statement.setString(2, task.status);
                statement.setLong(3, task.id);
                System.out.println("UPDATE: DEADLINE STATUS CHANGED");
            } else if (task.isDeadlinChanged && task.isResultChanged) {
                System.out.println(sdf.format(task.deadline.getTime()));
                statement.setTimestamp(1, sqlDate);
                statement.setString(2, task.result);
                statement.setLong(3, task.id);
                System.out.println("UPDATE: DEADLINE RESULT CHANGED");
            } else if (task.isResultChanged && task.isStatusChanged) {
                statement.setString(1, task.result);
                statement.setString(2, task.status);
                statement.setLong(3, task.id);
                System.out.println("UPDATE: RESULT STATUS CHANGED");
            } else if (task.isDeadlinChanged) {
                System.out.println(sdf.format(task.deadline.getTime()));
                statement.setTimestamp(1, sqlDate);
                statement.setLong(2, task.id);
                System.out.println("UPDATE: DEADLINE CHANGED");
            } else if (task.isResultChanged) {
                statement.setString(1, task.result);
                statement.setLong(2, task.id);
                System.out.println("UPDATE: RESULT CHANGED");
            } else if (task.isStatusChanged) {
                statement.setString(1, task.status);
                statement.setLong(2, task.id);
                System.out.println("UPDATE: STATUS CHANGED");
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    private Long getIdWithValueAndQuery(Long value, String SQL_SELECT) {
        try (Connection conn = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {
            preparedStatement.setLong(1, value);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void createTaskHistoryInDB(Long taskId) {
        String SQL_INSERT = "INSERT INTO task_history (created_at, updated_at, date, deadline,status, author_id,task_id,type) \n" +
                "SELECT NOW(),NOW(),p.date,t.deadline,t.status,t.author_id,t.id,?  FROM task t INNER JOIN protocol p on p.id=t.protocol_id where t.id=?";
        try (
                Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        ) {
            statement.setString(1, "TASK_UPDATED");
            statement.setLong(2, taskId);
            statement.executeUpdate();
            // ...
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Task> getTasksByTaskText(String taskText, java.util.Date protocolDate) {
        Timestamp sqlDate = new Timestamp(protocolDate.getTime());
        System.out.println(taskText);
        String SQL_SELECT = "SELECT t.id,t.deadline,t.protocol_point,t.result,t.status,t.task_text,p.date from task t " +
                "inner join protocol p on t.protocol_id=p.id where task_text=? and p.date=?";
        try (Connection conn = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT);) {

            preparedStatement.setString(1, taskText);
            preparedStatement.setTimestamp(2, sqlDate);
            ResultSet rs = preparedStatement.executeQuery();
            List<Task> tasks = new ArrayList<>();
            while (rs.next()) {
                Task task = new Task();
                task.id = rs.getLong(1);
                task.deadline = rs.getDate(2);
                task.protocolPoint = rs.getString(3);
                task.result = rs.getString(4);
                task.status = rs.getString(5);
                task.taskText = rs.getString(6);
                tasks.add(task);
            }
            return tasks;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Task getTaskByProtocolAndProtocolPoint(Long protocolId, Integer protocolPoint) {

        String SQL_SELECT = "SELECT id,deadline,protocol_point,result,status,task_text from task where protocol_point=? and protocol_id=?";
        try (Connection conn = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT);) {

            preparedStatement.setString(1, protocolPoint.toString());
            preparedStatement.setLong(2, protocolId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Task task = new Task();
                task.id = rs.getLong(1);
                task.deadline = rs.getDate(2);
                task.protocolPoint = rs.getString(3);
                task.result = rs.getString(4);
                task.status = rs.getString(5);
                task.taskText = rs.getString(6);

                return task;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}