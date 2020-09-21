package kz.akimat.inserttodb.thirdVariant;

import java.util.Date;

public class Task {
    public Long id;
    public Date deadline;
    public String protocolPoint;
    public String result;
    public String status;
    public String taskText;
    public Boolean isStatusChanged = false;
    public Boolean isResultChanged = false;
    public Boolean isDeadlinChanged = false;
    public Boolean isChanged = false;

    public Task() {
    }

    public Task(Date deadline, String protocolPoint, String result, String status, String taskText) {
        this.deadline = deadline;
        this.protocolPoint = protocolPoint;
        this.result = result;
        this.status = status;
        this.taskText = taskText;
    }

    public String getQuery() {
        String SQL_INSERT_BASE = "UPDATE `task` SET  `updated_at`=NOW(), ";
        String SQL_INSERT_ALL = " `deadline`=?, `result`=?,`status`=? ";
        String SQL_INSERT_STATUS = " `status`=? ";
        String SQL_INSERT_Result = " `result`=? ";
        String SQL_INSERT_Deadline = "`deadline`=?";
        String SQL_INSERT_Deadline_Status = "`deadline`=?, `status`=? ";
        String SQL_INSERT_Result_Status = " `result`=?,`status`=? ";
        String SQL_INSERT_Deadline_Result = " `deadline`=?, `result`=? ";
        String SQL_WHERE = " where id=? ";
        if (isDeadlinChanged && isStatusChanged && isResultChanged)
            return SQL_INSERT_BASE + SQL_INSERT_ALL + SQL_WHERE;
        else if (isDeadlinChanged && isStatusChanged)
            return SQL_INSERT_BASE + SQL_INSERT_Deadline_Status + SQL_WHERE;
        else if (isDeadlinChanged && isResultChanged)
            return SQL_INSERT_BASE + SQL_INSERT_Deadline_Result + SQL_WHERE;
        else if (isResultChanged && isStatusChanged)
            return SQL_INSERT_BASE + SQL_INSERT_Result_Status + SQL_WHERE;
        else if (isDeadlinChanged)
            return SQL_INSERT_BASE + SQL_INSERT_Deadline + SQL_WHERE;
        else if (isResultChanged)
            return SQL_INSERT_BASE + SQL_INSERT_Result + SQL_WHERE;
        else if (isStatusChanged)
            return SQL_INSERT_BASE + SQL_INSERT_STATUS + SQL_WHERE;
        return null;
    }
}
