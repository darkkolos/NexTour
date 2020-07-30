package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseDados {

    private String ip;
    private String porto;
    private String bd;
    private String login;
    private String password;

    public BaseDados() {
        ip = "node28455-nextour.es-1.axarnet.cloud";
        porto = "3306";
        bd = "nextour";
        login = "root";
        password = "password";
    }

    public VariaveisBD abrirConexao() {
        Connection conn = null;
        Statement st = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + porto
                    + "/" + bd + "", login, password);
            st = conn.createStatement();
        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(BaseDados.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return new VariaveisBD(st, conn);
    }

    public boolean fecharConexao(VariaveisBD variaveisBD) {
        try {
            variaveisBD.getStatement().close();
            variaveisBD.getConnection().close();
        } catch (SQLException ex) {
            Logger.getLogger(BaseDados.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }
}
