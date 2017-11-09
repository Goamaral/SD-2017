import java.sql.*;

class OracleCon {

    Connection con;
    Statement stmt;
    Boolean showLogs;


    public OracleCon (String username, String password, Boolean logs){
        showLogs = logs;
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");

            con = DriverManager.getConnection(
            "jdbc:oracle:thin:@localhost:1521:xe",username,password);
            if(showLogs)
                System.out.println("Connected to Oracle Database");
            stmt = con.createStatement();

        }catch(Exception e){ 
            if(showLogs)
                System.out.println(e);
        }
    }


    public void showLogs(Boolean bool) {
        showLogs = bool;
    }


    public Statement getStatement() {
        return stmt;
    }


    public void closeConnection() throws Exception {
        try {
            con.close();
        }catch(Exception e) {
            if(showLogs)
                System.out.println(e);
        }
    }


    public ResultSet query(String string) throws Exception {
        try {
            ResultSet rs = stmt.executeQuery(string);
            if(showLogs) 
                System.out.println(string);
            return rs;
        }catch(Exception e) {
            if(showLogs)
                System.out.println("Error on query(\"" + string + "\"): "+ e);
            return null;
        }
    }


    public void insert(String string) throws Exception {
        try {
            stmt.executeUpdate(string);
            if(showLogs) 
                System.out.println(string);
        }catch(Exception e) {
            if(showLogs)
                System.out.println("Error on insert(\"" + string + "\"): "+ e);
        }
    }
}
