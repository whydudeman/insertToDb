package kz.akimat.inserttodb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserUtils {

    public static List<User> getUsers() {
        try {
            Connection con = DriverManager.getConnection(DbConstants.jdbcURL, DbConstants.username, DbConstants.password);

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select u.id, u.name, d.name from user u\n" +
                    "inner join position p ON p.id = u.position_id \n" +
                    "left join department d ON d.id = p.department_id");
            List<User> users = new ArrayList();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong(1));
                user.setName(rs.getString(2));
                user.setNameWithSpace(rs.getString(2));
                user.setDepartment(rs.getString(3));
                user.setNameNoChange(rs.getString(2));
                users.add(user);
            }
            con.close();
            return users;
        } catch (Exception e) {
            System.out.println(e);
        }
        return new ArrayList<>();
    }

    public static Long getUserId(String name, List<User> users) {
        if (name != null && !name.isEmpty()) {
            Long id = users.stream().filter(user -> user.getNameNoChange().equals(name)).map(User::getId).findAny().orElse(null);
            if (id == null)
                id = users.stream().filter(user ->
                        levenstain(user.getName(), name) < 4 || levenstain(user.getNameWithSpace(), name) < 4)
                        .map(User::getId).findFirst().orElse(null);
            return id;
        }
        return null;
    }

    public static List<Long> getUsersId(List<String> names, List<User> users) {
        List<Long> ids = new ArrayList<>();
        for (String name : names) {
            for (User user : users) {
                if (levenstain(user.getName(), name) < 4 || levenstain(user.getNameWithSpace(), name) < 4)
                    ids.add(user.getId());
            }

        }
        return ids;
    }

    public static List<Long> getUserIdByDepartment(List<String> departments, List<User> users) {
        List<Long> ids = new ArrayList<>();
        for (String s : departments) {
            for (User user : users) {
                if (user.getDepartment() != null && (user.getDepartment().trim().equals(s) ||
                        levenstain(user.getDepartment().trim(), s) < 4) ||
                        user.getName() != null && (user.getName().trim().equals(s) || levenstain(user.getName().trim(), s) < 4)) {
                    ids.add(user.getId());
                }
            }

        }
        return ids;
    }

    public static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    public static int levenstain(CharSequence lhs, CharSequence rhs) {
        int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

        for (int i = 0; i <= lhs.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= rhs.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= lhs.length(); i++)
            for (int j = 1; j <= rhs.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

        return distance[lhs.length()][rhs.length()];
    }
}
