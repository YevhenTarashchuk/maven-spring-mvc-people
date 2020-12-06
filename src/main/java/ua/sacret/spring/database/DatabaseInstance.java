package ua.sacret.spring.database;

import java.sql.*;

public class DatabaseInstance
{
    private String url;
    private String userName;
    private String pass;
    private boolean connected;
    private long lastRequestDate;
    private Connection connection;
    private ResultSet resultSet;

    public static final String DB_ADDRESS = "localhost:3306";
    public static final String DB_NAME = "program";
    public static final String DB_USER = "root";
    public static final String DB_USER_PASSWORD = "blackwind07";
    public static DatabaseInstance DB_INSTANCE;


    private DatabaseInstance(String url, String userName, String pass) {
        this.url = url;
        this.userName = userName;
        this.pass = pass;
        this.connection = null;
        this.connected = false;
        this.lastRequestDate = 0l;
        this.resultSet = null;
        connectToDB();
    }

    public static DatabaseInstance getDatabase(){
        DatabaseInstance db = DB_INSTANCE;
        if(db == null) {
            String url = "jdbc:mysql://"+DB_ADDRESS+"/"+DB_NAME+"?useUnicode=true&characterEncoding=utf8&serverTimezone=UTC";
            String userName = DB_USER;
            String pass = DB_USER_PASSWORD;
            db = new DatabaseInstance(url, userName, pass);
        }
        return db;
    }

    private void connectToDB() {
        try{
            System.out.println("Connection to DB..");
            this.connection = DriverManager.getConnection(url, userName, pass);
            this.lastRequestDate = System.currentTimeMillis();
            this.connected = true;
            System.out.println("DB has been connected");
            DB_INSTANCE = this;
        } catch (SQLException e){
            this.connected = false;
            System.err.println("Couldn't connect to DB " + e);
            DB_INSTANCE = null;
        }
    }

    public boolean isActuallyConnected() {
        if(!this.connected) {
            return false;
        }
        try {
            Statement statement = connection.createStatement();
            statement.execute("SELECT 1");
            statement.close();
            DB_INSTANCE = this;
            return true;
        } catch (SQLException e) {

        }
        DB_INSTANCE = null;
        return false;
    }

    private void reconnectIfNeeded() {
        long timeout = 30000l;
        if((System.currentTimeMillis() - this.lastRequestDate) > timeout) {
            if(!isActuallyConnected()){
                System.out.println("BD connection lost, reconnecting..");
                this.connectToDB();
            } else {
                lastRequestDate = System.currentTimeMillis() + timeout;
            }
        }
    }

    public boolean update(String query) {
        this.reconnectIfNeeded();
        if (this.connection == null) {
            return false;
        }
        Statement statement = null;
        boolean ok = false;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(query);
            ok = true;
        } catch (Exception e) {
            System.out.println("Failed to excute query " + query);
            System.err.println("Exception " + e);
        }

        try {
            if(statement != null) {
                statement.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return ok;
    }

}
