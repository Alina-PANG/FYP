package fyp.app.util;

import fyp.app.entities.Count;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DBConnector {
    public void saveCount(Count count){
        try
        {
            // create a mysql database connection
            String myDriver = "com.mysql.cj.jdbc.Driver";
            String myUrl = "jdbc:mysql://localhost/hyper_simulation";
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, "root", "19980312");


            // the mysql insert statement
            String query = " insert into count (times, inputFile, countExp, countAdd, countDrop, countBorrow, countSwitch, firmId, finalFitness)"
                    + " values (?, ?, ?, ?, ?,?,?,?,?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setInt (1, count.getTime());
            preparedStmt.setString (2, count.getInputFile());
            preparedStmt.setInt   (3, count.getCountExp());
            preparedStmt.setInt   (4, count.getCountAdd());
            preparedStmt.setInt   (5, count.getCountDrop());
            preparedStmt.setInt   (6, count.getCountBorrow());
            preparedStmt.setInt   (7, count.getCountSwitch());
            preparedStmt.setInt(8, count.getFirmId());
            preparedStmt.setDouble    (9, count.getFinalFitness());

            // execute the preparedstatement
            preparedStmt.execute();

            conn.close();
        }
        catch (Exception e)
        {
            System.err.println("Got an exception!");
            e.printStackTrace();
        }
    }
}
