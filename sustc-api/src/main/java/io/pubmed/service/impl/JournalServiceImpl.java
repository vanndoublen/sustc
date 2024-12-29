package io.pubmed.service.impl;

import io.pubmed.dto.Journal;
import io.pubmed.service.JournalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class JournalServiceImpl implements JournalService {
    @Autowired
    private DataSource dataSource;
    @Override
    public double getImpactFactor(String journal_title, int year) {
        String sqlA = "select sum(count_by_year) sum from cited_count_by_year cc -- A\n" +
                "join article a on cc.reference_id = a.id\n" +
                "join article_journal aj on aj.article_id = cc.reference_id\n" +
                "join journal j on j.id = aj.journal_id\n" +
                "where  year = ?  and extract(year from a.date_created) in (?, ?)\n" +
                "and j.title = ?";

        String sqlB = "select count(a.id) count from article a -- B\n" +
                "join article_journal aj on a.id = aj.article_id\n" +
                "join journal j on j.id = aj.journal_id\n" +
                "where extract(year from date_created) in (?, ?) and j.title = ?;";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();

            int iniYear = year - 2;
            int finalYear = year - 1;
            int A = 0;
            int B = 0;

             try (PreparedStatement AState = conn.prepareStatement(sqlA);){
                 AState.setInt(1, year);
                 AState.setInt(2, iniYear);
                 AState.setInt(3, finalYear);
                 AState.setString(4, journal_title);

                 try (ResultSet ASet = AState.executeQuery();){
                     if (ASet.next()){
                         A = ASet.getInt("sum");
                     }
                 }
             }

             try (PreparedStatement BState = conn.prepareStatement(sqlB);){
                 BState.setInt(1, iniYear);
                 BState.setInt(2, finalYear);
                 BState.setString(3, journal_title);

                 try (ResultSet BSet = BState.executeQuery();){
                     if (BSet.next()){
                         B = BSet.getInt("count");
                     }
                 }
             }
            return (double) A/B;


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // add new journal into journal table first
    // query articles that matches the condition
    // store article's id and relationship to journal
    // change the relationship in article_journal
    // revert back the relationship
    // delete the new journal from journal table
    @Override
    public boolean updateJournalName(Journal journal, int year, String new_name, String new_id) {
        String insertJournalSql = "insert into journal values (?, ?, ?, ?, ?, ?);";

        String queryArticleSql = "select aj2.article_id id from journal j\n" +
                "join article_journal aj2 on j.id = aj2.journal_id\n" +
                "join article a on aj2.article_id = a.id\n" +
                "where extract(year from a.date_created) >= ? and aj2.journal_id = ?;";

        String updateAJSql = "update article_journal aj set journal_id = ?\n" +
                "where aj.article_id = ?;";

        String revertSql = "update article_journal aj set journal_id = ?\n" +
                "where aj.article_id = ?;";

        String deleteJournalSql = "delete from journal where id = ?;";

        String checkJournalIdSql = "select exists(select  1 from journal where id = ?) as exist;";

        int countInsert = 0;
        boolean checkId = false;

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            boolean check = false;
            int countUpdate = 0;
            int countRevert = 0;
            List<Integer> oldIdList = new ArrayList<>();
            String oldJournalId = journal.getId();
            String oldTitle = journal.getTitle();

            //check existence first before doing anything else, because journal should be new
            try(PreparedStatement checkJournalIdState = conn.prepareStatement(checkJournalIdSql)){
                checkJournalIdState.setString(1, new_id);
                System.out.println(checkId + " exist 1");

                try (ResultSet resultSet = checkJournalIdState.executeQuery() ){
                    if (resultSet.next()){
                        checkId = resultSet.getBoolean("exist");
                    }
                }
                System.out.println(checkId + " exist id");
                if (checkId){ //if it is already existed, then the input is wrong
                    return false;
                }
            }


            //insert new journal
            try (PreparedStatement insertJournalState = conn.prepareStatement(insertJournalSql);) {
                 insertJournalState.setString(1, new_id);
                 insertJournalState.setString(2, ""); // country
                 insertJournalState.setString(3, ""); // issn, either 9 digits or ''
                 insertJournalState.setString(4, new_name);
                 insertJournalState.setString(5, ""); // volume
                 insertJournalState.setString(6, ""); // issue

                 countInsert = insertJournalState.executeUpdate();
                System.out.println(countInsert + " from insert");

             }

            //query and store article id
            try (PreparedStatement queryArticleState = conn.prepareStatement(queryArticleSql);){
                queryArticleState.setInt(1, year);
                queryArticleState.setString(2, oldJournalId);

                try (ResultSet articleSet = queryArticleState.executeQuery();) {
                    while (articleSet.next()){
                        oldIdList.add(articleSet.getInt("id"));
                    }
                }
            }



            // update relationship
            try (PreparedStatement updateAJState = conn.prepareStatement(updateAJSql);){
                int batchSize = 100; //batch size
                int batchCount = 0;
                for (int i = 0; i < oldIdList.size(); i++) {
                    updateAJState.setString(1, new_id);
                    updateAJState.setInt(2, oldIdList.get(i));
                    updateAJState.addBatch();
                    System.out.println("id: " + oldIdList.get(i));

                    batchCount ++;
                    if (batchCount % batchSize == 0){
                        int[] countBatch  = updateAJState.executeBatch();
                        countUpdate += Arrays.stream(countBatch).sum();;
                        batchCount = 0;
                    }
                }
                if (batchCount >0){
                    int[] remain = updateAJState.executeBatch();
                    countUpdate += Arrays.stream(remain).sum();
                }
            }
            System.out.println("count update : " + countUpdate);


            if (countUpdate > 0){ // if it takes affect
                check = true;
                // revert it back
                try (PreparedStatement revertState = conn.prepareStatement(revertSql);) {
                    int batchSize = 100; //batch size
                    int batchCount = 0;
                    for (int i = 0; i < oldIdList.size(); i++) {
                        revertState.setString(1, oldJournalId);
                        revertState.setInt(2, oldIdList.get(i));
                        revertState.addBatch();

                        batchCount++;
                        if (batchCount % batchSize == 0) {
                            int[] countBatch = revertState.executeBatch();
                            countRevert += Arrays.stream(countBatch).sum();
                            batchCount = 0;
                        }
                        if (batchCount > 0) {
                            int[] remain = revertState.executeBatch();
                            countRevert += Arrays.stream(remain).sum();
                        }
                    }
                    System.out.println("count revert : " + countRevert);
                }

            }
            // delete the new journal
            System.out.println("check insert " + countInsert);
            if (countInsert > 0){
                try (PreparedStatement deleteJournalState = conn.prepareStatement(deleteJournalSql);){
                    deleteJournalState.setString(1, new_id);
                    deleteJournalState.execute();
                }
            }

            conn.commit();
            return check;


        } catch (SQLException e) {
            if (conn != null){
                try {
                    conn.rollback();
                    System.out.println("rollback because : " + e.getMessage());
                } catch (SQLException ex) {
                    throw new RuntimeException("rollback fail :" +ex);
                }
            }
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
