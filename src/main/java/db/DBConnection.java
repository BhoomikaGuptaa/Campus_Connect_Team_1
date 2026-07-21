package db;
import java.sql.*;
public final class DBConnection {
 private DBConnection() {}
 public static Connection getConnection() throws SQLException {
  String url = env("CAMPUS_DB_URL", "jdbc:mysql://localhost:3306/campus_connect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Los_Angeles");
  String user = env("CAMPUS_DB_USER", "root");
  String password = env("CAMPUS_DB_PASSWORD", "");
  try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) { throw new SQLException("MySQL driver not found", e); }
  return DriverManager.getConnection(url, user, password);
 }
 private static String env(String name, String fallback) { String value=System.getenv(name); return value==null||value.isBlank()?fallback:value; }
}
