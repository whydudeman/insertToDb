package kz.akimat.inserttodb.secondVariant;

import kz.akimat.inserttodb.Utils.DbConstants;
import kz.akimat.inserttodb.Utils.User;
import kz.akimat.inserttodb.Utils.UserUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import java.util.Objects;

public class NewMain {
    public static void main(String... strings) throws IOException, SQLException {
        NewMain objExcelFile = new NewMain();
        String fileName = "asdas.xlsx";
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
            for (int j = 1; j < rowCount; j++) {
                Row row = sheet.getRow(j);
                insertAndUpdateTask(users, row);
            }
        }
    }

    private void insertAndUpdateTask(List<User> users, Row row) throws SQLException {
        NewExcellData excellData = new NewExcellData(row);
        List<Long> userId = UserUtils.getUsersId(excellData.userNames, users);
        List<Long> executorIds = UserUtils.getUserIdByDepartment(excellData.departments, users);
        System.out.println(row.getRowNum());
        if (userId != null && !executorIds.isEmpty()) {
            Long protocolId = getProtocolIdFromDB(excellData.protocolNumber);
            System.out.println("Protocol Id: "+protocolId);
            if (protocolId != null) {
                Long taskId = null;
                if (taskId == null) {
                    Long sphereId = getSphereIdFromDB(excellData.sphere.trim());
                    if (sphereId == null) {
                        System.out.println("SPHERE IS NULL");
                        throw new NullPointerException("NULLERROR");
                    }
                    taskId = createTask(excellData, protocolId, sphereId);
                    createTaskUserInDb(taskId, executorIds);
                    createTaskHistoryInDB(taskId);
                    int counter=1; boolean isMain=true;
                    for (Long userLongId : userId) {

                        if (counter != 1)
                            isMain=false;
                        if (getTaskUser(taskId, userLongId, "CONTROL") == null)
                            createTaskUserInDb(taskId, userLongId,isMain);
                        counter++;
                    }
                }else {
                    System.out.println("TASK ID is Already created");
                }
//                if (taskId != null) {
//                    if (isTaskNeedsChange(taskId, excellData.status)) {
//                        updateTaskInDB(taskId, excellData);
//                        createTaskHistoryInDB(taskId);
//                        createTaskUserInDb(taskId, executorIds);
//                        for (Long userLongId : userId)
//                            if (getTaskUser(taskId, userLongId, "CONTROL") == null)
//                                createTaskUserInDb(taskId, userId);
//                    }
//                } else {
//                    System.out.println("TASK: Error could not found and create Task, userName: " + excellData.userNames
//                            + " protocolNumber " + excellData.protocolNumber + " Row number: " + row.getRowNum());
//                }
            } else {
                System.out.println("PROTOCOL: Could not find protocol from db" + " Number: " + excellData.protocolPoint + " Row number: " + row.getRowNum() + " DistrictId: ");
            }
        } else {
            System.out.println("USER: Error could not found User from DB, userName: " + excellData.userNames
                    + " protocolNumber " + excellData.protocolNumber + " Row number: " + row.getRowNum() + " UserId: "
                    + userId + " Executors: " + excellData.executor + " ids: " + executorIds.toString());
        }
    }

    private Long createTask(NewExcellData excellData, Long protocolId, Long sphereId) {
        String SQL_INSERT = "INSERT INTO `task`(`created_at`, `updated_at`,`deadline`, `protocol_point`, `result`, " +
                "`status`, `task_text`, `author_id`,`protocol_id`, `sphere_id`) " +
                "VALUES (NOW(), NOW(),?,?,?,?,?,?,?,?)";
        Date deadline = null;
        if (excellData.deadline != null) {
            deadline = new Date(excellData.deadline.getTime());
        }
        try (
                Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                        Statement.RETURN_GENERATED_KEYS);
        ) {
            statement.setDate(1, deadline);
            statement.setString(2, String.valueOf(excellData.protocolPoint));
            statement.setString(3, excellData.result);
            statement.setString(4, excellData.status);
            statement.setString(5, excellData.taskText);
            statement.setLong(6, 45L);
            statement.setLong(7, protocolId);
            if (sphereId != null)
                statement.setLong(8, sphereId);
            else System.out.println("SPHERE: ERROR");


            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating Task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isTaskNeedsChange(Long taskId, String currentStatus) {
        return !Objects.equals(getStatusFromDBTask(taskId), currentStatus);
    }

    private String getStatusFromDBTask(Long taskId) {
        String SQL_SELECT = "SELECT status from task where id=?";
        try (Connection conn = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {
            preparedStatement.setLong(1, taskId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Long getTaskUser(Long taskId, Long userId, String type) {
        String SQL_SELECT = "SELECT id from task_user where task_id=? and user_id=? and type=? ";
        try (Connection conn = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {
            preparedStatement.setLong(1, taskId);
            preparedStatement.setLong(2, userId);
            preparedStatement.setString(3, type);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Long getProtocolIdFromDB(String protocolNumber) throws SQLException {
        String SQL_SELECT = "SELECT id from protocol where protocol_number=?";
        return getIdWithValueAndQuery(protocolNumber, SQL_SELECT);
    }

    private Long getIdWithValueAndQuery(String value, String SQL_SELECT) {
        try (Connection conn = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
             PreparedStatement preparedStatement = conn.prepareStatement(SQL_SELECT)) {
            preparedStatement.setString(1, value);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getLong(1)+" Getting from db");
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Long getSphereIdFromDB(String sphere) {
        System.out.println(sphere+" Sphere");
        String SQL_SELECT = "SELECT id from sphere where name=?";
        return getIdWithValueAndQuery(sphere, SQL_SELECT);
    }

    private static void updateTaskInDB(Long taskId, NewExcellData excellData) {

        String SQL_INSERT = "UPDATE `task` SET  `updated_at`=NOW(),`deadline`=?,`protocol_point`=?," +
                "`result`=?,`status`=?,`task_text`=?,`author_id`=? WHERE id=?";
        Date deadline = null;
        if (excellData.deadline != null) {
            deadline = new Date(excellData.deadline.getTime());
        }
        try (
                Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        ) {
            statement.setDate(1, deadline);
            statement.setString(2, excellData.protocolPoint.toString());
            statement.setString(3, excellData.result);
            statement.setString(4, excellData.status);
            statement.setString(5, excellData.taskText);
            statement.setLong(6, 45l);
            statement.setLong(7, taskId);

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void createTaskUserInDb(Long taskId, Long userId,boolean isMain) {
        String SQL_INSERT = "INSERT INTO `task_user`( `created_at`, `updated_at`, `is_main`, " +
                "`type`, `task_id`, `user_id`) VALUES (NOW(),NOW(),?,?,?,?)";
        try (
                Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        ) {
            statement.setBoolean(1,isMain);
            statement.setString(2, "CONTROL");
            statement.setLong(3, taskId);
            statement.setLong(4, userId);
            statement.executeUpdate();
            // ...
        } catch (SQLException e) {
            System.out.println("CONTROL: TASK AND USER ALREADY EXISTS");
        }


    }

    private void createTaskUserInDb(Long taskId, List<Long> executersId) {
        String SQL_INSERT = "INSERT INTO `task_user`( `created_at`, `updated_at`, `is_main`, " +
                "`type`, `task_id`, `user_id`) VALUES (NOW(),NOW(),?,?,?,?)";
        int count=1;
        boolean isMain=true;
        for (Long id : executersId) {
            if(count!=1)
                isMain=false;
            try (
                    Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                    PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
            ) {
                statement.setBoolean(1,isMain);
                statement.setString(2, "EXECUTION");
                statement.setLong(3, taskId);
                statement.setLong(4, id);
                statement.executeUpdate();
                // ...
            } catch (SQLException e) {
                System.out.println("EXECUTION: TASK AND USER ALREADY EXISTS");
            }
            count++;
        }

    }

    private static void createTaskHistoryInDB(Long taskId) {
        String SQL_INSERT = "INSERT INTO task_history (created_at, updated_at, date, deadline,status, author_id,task_id) \n" +
                "SELECT NOW(),NOW(),NOW(),deadline,status,author_id,id FROM task where id=?";
        try (
                Connection connection = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
        ) {
            statement.setLong(1, taskId);
            statement.executeUpdate();
            // ...
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Long getTaskIdFromDB(Long protocolId, Integer protocolPoint) throws SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        String SQL_SELECT = "SELECT id from task where protocol_point=? and protocol_id=?";
        try {
            conn = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);
            preparedStatement = conn.prepareStatement(SQL_SELECT);
            preparedStatement.setString(1, protocolPoint.toString());
            preparedStatement.setLong(2, protocolId);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null) preparedStatement.close();
            if (conn != null) conn.close();
        }

        return null;
    }
}