package dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class connectDB {
    public static Connection getConnection()  {
        Connection connection = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
           String url = "jdbc:sqlserver://LOCDZ\\MSSQLSERVER01:1433;databaseName=Library;encrypt=true;trustServerCertificate=true;";
            String user = "sa";
            String password = "1234567890";
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }
}
