package io.pubmed.service.impl;

import java.sql.*;

public class CitedCountImport {
    private static Connection con = null;
    private ResultSet resultSet;
    //    private String className = null;
    private static String host = "localhost";
    private static String dbname = "test4";
    private static String user = "postgres";
    private static String pwd = "pospassword";
    private static String port = "5432";

    public static void getConnection(){
        try {
            Class.forName("org.postgresql.Driver");

        } catch (Exception e) {
            System.err.println("Cannot find the PostgreSQL driver. Check CLASSPATH.");
            System.exit(1);
        }

        try {
            String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbname;
            con = DriverManager.getConnection(url, user, pwd);

        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void closeConnection(){
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void createCitedCountTable(){

        String sql = "create table if not exists cited_count (\n" +
                "    id bigint primary key ,\n" +
                "    cited_count bigint not null default 0\n" +
                ");";
        try {
            Statement statement;
            if (con != null){
                statement = con.createStatement();
                statement.execute(sql);
                statement.close();
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void insertIntoCitedCount(){

        String sql = "insert into cited_count (id, cited_count)\n" +
                "select reference_id, count(*)\n" +
                "from article_references\n" +
                "group by reference_id;";
        try {
            Statement statement;
            if (con != null){
                statement = con.createStatement();
                statement.execute(sql);
                statement.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void createCitedCountByYearTable(){

        String sql = "create table if not exists cited_count_by_year(\n" +
                "    year integer,\n" +
                "    reference_id bigint,\n" +
                "    count_by_year integer,\n" +
                "    primary key (year, reference_id, count_by_year)\n" +
                ");\n";
        try {
            Statement statement;
            if (con != null){
                statement = con.createStatement();
                statement.execute(sql);
                statement.close();
            }


        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void insertIntoCitedCountByYear(){

        String sql = "insert into cited_count_by_year (year, reference_id, count_by_year)\n" +
                "select year,reference_id, count(s)from (select reference_id, EXTRACT(year from date_created) as year from cited_count c\n" +
                "join article_references ar on c.id = ar.reference_id\n" +
                "join article art on ar.article_id = art.id\n" +
                "group by date_created, reference_id, article_id) as s\n" +
                "group by reference_id , year;";
        try {
            Statement statement;
            if (con != null){
                statement = con.createStatement();
                statement.execute(sql);
                statement.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        getConnection();
        createCitedCountTable();
        insertIntoCitedCount();
        createCitedCountByYearTable();
        insertIntoCitedCountByYear();
        closeConnection();
        long end = System.currentTimeMillis();
        System.out.println("Time:" + (end - start));
    } // Time:16603 (time after imported)
}
