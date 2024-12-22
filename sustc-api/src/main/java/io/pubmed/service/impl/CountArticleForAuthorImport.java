package io.pubmed.service.impl;

import java.sql.*;

public class CountArticleForAuthorImport {
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

    public static void createCountArticleForAuthorTable(){
        String sql = "create table if not exists count_article_for_author (\n" +
                "    author_id int,\n" +
                "    last_name varchar(255),\n" +
                "    fore_name varchar(255),\n" +
                "    initials varchar(10),\n" +
                "    journal_title varchar(1000),\n" +
                "    count_article int,\n" +
                "    primary key (author_id, journal_title)\n" +
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

    public static void insertCountArticleForAuthor(){
        String sql = "insert into count_article_for_author (author_id, last_name, fore_name, initials, journal_title, count_article)\n" +
                "select au.author_id , au.last_name, au.fore_name, au.initials, j.title journal_title, count(aj.article_id) count from journal j\n" +
                "join article_journal aj on aj.journal_id = j.id\n" +
                "join article a on a.id = aj.article_id\n" +
                "join article_authors aa on aa.article_id = a.id\n" +
                "join authors au on au.author_id = aa.author_id\n" +
                "group by au.fore_name, au.last_name, au.initials, j.title, au.author_id;";
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
        createCountArticleForAuthorTable();
        insertCountArticleForAuthor();
        closeConnection();
        long end = System.currentTimeMillis();
        System.out.println("Time:" + (end - start));
    } // Time: 103,467 ms
}
