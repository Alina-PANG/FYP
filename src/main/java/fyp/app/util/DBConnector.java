package fyp.app.util;

import fyp.app.entities.Component;
import fyp.app.entities.Count;
import fyp.app.entities.Fitness;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class DBConnector {
    private final String username = "root";
    private final String password = "19980312";
    private final String myUrl = "jdbc:mysql://localhost/hyper_simulation?autoReconnect=true&useSSL=false";
    private final String myDriver = "com.mysql.cj.jdbc.Driver";

    public void saveCount(Count count){
        try
        {
            // create a mysql database connection
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, username, password);

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

    public void saveComponent(Component component){
        try
        {
            // create a mysql database connection
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, username, password);


            // the mysql insert statement
            String query = " insert into component (times, inputFile, size, CIndex)"
                    + " values (?, ?, ?, ?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setInt (1, component.getTime());
            preparedStmt.setString (2, component.getInputFile());
            preparedStmt.setInt   (3, component.getSize());
            preparedStmt.setInt   (4, component.getIndex());

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

    public void saveFitness(Fitness fitness){
        try
        {
            // create a mysql database connection
            Class.forName(myDriver);
            Connection conn = DriverManager.getConnection(myUrl, username, password);

            // the mysql insert statement
            String query = " insert into fitness (times, inputFile, iteration, fitness, firmId)"
                    + " values (?, ?, ?, ?,?)";

            // create the mysql insert preparedstatement
            PreparedStatement preparedStmt = conn.prepareStatement(query);
            preparedStmt.setInt (1, fitness.getTime());
            preparedStmt.setString (2, fitness.getInputFile());
            preparedStmt.setInt   (3, fitness.getIteration());
            preparedStmt.setDouble   (4, fitness.getFitness());
            preparedStmt.setInt   (5, fitness.getFirmId());

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
