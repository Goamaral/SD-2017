import java.sql.*;

class OracleCon {
    private Statement stmt;
    private Boolean showLogs;


    private OracleCon (String username, String password, Boolean logs){
        showLogs = logs;

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe", username, password);
            if (showLogs) System.out.println("Connected to Oracle Database");
            stmt = con.createStatement();
            query("alter SESSION set NLS_DATE_FORMAT = 'DD-MM-YYYY HH24:MI'");
        } catch (Exception e) {
            if (showLogs) e.printStackTrace();
        }

    }

    public ResultSet query(String string) {
        try {
            ResultSet rs = stmt.executeQuery(string);
            if(showLogs) 
                System.out.println(string);

            return rs;
        }catch(Exception e) {
            if(showLogs) System.out.println("Error on query(\"" + string + "\"): "+ e);

            return null;
        }
    }
}
