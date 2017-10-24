import java.sql.*;

class OracleCon {

    Connection con;
    Statement stmt;

    public Statement getStatement() {
        return stmt;
    }

    public void closeConnection() throws Exception {
        try {
            con.close();
        }catch(Exception e) {
            System.out.println(e);
        }
    }


    public ResultSet query(String string) throws Exception {
        try {
            ResultSet rs = stmt.executeQuery(string);
            return rs;
        }catch(Exception e) {
            System.out.println("Error on query(\"" + string + "\"): "+ e);
            return null;
        }
    }

    public void insert(String string) throws Exception {
        try {
            stmt.executeUpdate(string);
        }catch(Exception e) {
            System.out.println("Error on insert(\"" + string + "\"): "+ e);
        }
    }

    public OracleCon (String username, String password){
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");

            con = DriverManager.getConnection(
            "jdbc:oracle:thin:@localhost:1521:xe",username,password);

            stmt = con.createStatement();

            /*
            ResultSet rs = stmt.executeQuery("select * from emp");
            while(rs.next())
                System.out.println(rs.getString(1));

            con.close();
            */

        }catch(Exception e){ 
            System.out.println(e);
        }
    }

}
