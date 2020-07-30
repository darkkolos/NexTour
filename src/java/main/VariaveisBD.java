package main;

import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author sergy_000
 */
public class VariaveisBD {
    
    private Statement st;
    private Connection conn;
    
    public VariaveisBD(Statement st, Connection conn){
        this.st = st;
        this.conn = conn;
    }

    public Statement getStatement() {
        return st;
    }

    public Connection getConnection() {
        return conn;
    }
    
    
            
}
