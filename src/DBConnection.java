import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class DBConnection {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/healthcenter?serverTimezone=UTC";
        String user = "root";
        String password = "@Mhaine1125";
        return DriverManager.getConnection(url, user, password);
    }
}

