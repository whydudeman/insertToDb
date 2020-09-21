package kz.akimat.inserttodb.Utils;

public class User {
    private Long id;
    private String name;
    private String nameWithSpace;
    private String department;
    private String nameNoChange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNameNoChange(String name) {
        this.nameNoChange = name;
    }

    public String getNameNoChange(){
        return nameNoChange;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        String fio[] = name.split(" ");
        if (fio.length > 2) {
            this.name = fio[0] + " " + fio[1].substring(0, 1) + "" + fio[2].substring(0, 1);
        } else if (fio.length > 1) {
            this.name = fio[0] + " " + fio[1].substring(0, 1) + "";
        } else {
            this.name = name;
        }
    }

    public String getNameWithSpace() {
        return nameWithSpace;
    }

    public void setNameWithSpace(String nameWithSpace) {
        String fio[] = nameWithSpace.split(" ");
        if (fio.length > 2) {
            this.nameWithSpace = fio[0] + " " + fio[1].substring(0, 1) + " " + fio[2].substring(0, 1);
        } else if (fio.length > 1) {
            this.nameWithSpace = fio[0] + " " + fio[1].substring(0, 1) + "";
        } else {
            this.nameWithSpace = nameWithSpace;
        }
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
