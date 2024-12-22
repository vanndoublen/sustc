package io.pubmed.service.impl;

import io.pubmed.dto.Article;
import io.pubmed.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;


@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private DataSource dataSource;


    @Override
    public int getArticleCitationsByYear(int id, int year) {
//        String sql ="select count(*) as count from article inner join article_references ar on article.id = ar.article_id\n" +
//                "where reference_id = ?  AND  EXTRACT(year from date_completed) = ?;";
        String sql = "select count_by_year from cited_count_by_year where reference_id = ? and  year = ?;";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, year);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count_by_year");
//                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }


    @Override
    public double addArticleAndUpdateIF(Article article) {

        String sqlA = "select sum(count_by_year) from cited_count_by_year cc -- A\n" +
                "join article a on cc.reference_id = a.id\n" +
                "join article_journal aj on aj.article_id = cc.reference_id\n" +
                "join journal j on j.id = aj.journal_id\n" +
                "where year = ?  and extract(year from a.date_created) in (?, ?)\n" +
                "and j.title = ?";

        String sqlB = "select count(a.id) from article a -- B\n" +
                "join article_journal aj on a.id = aj.article_id\n" +
                "join journal j on j.id = aj.journal_id\n" +
                "where extract(year from date_created) in (?, ?) and j.title = ?";

        String sqlAddArticle = "insert into article values (?, ?, ?, ?, ?);";
        String sqlAddAJ = "insert into article_journal(journal_id,article_id) values (?, ?);";

        String sqlGetJournalId = "select id from journal\n" +
                "where title = ?;";
        String sqlGetYear = "select (extract(year from date_created) ) as year from article where id = ?;";

        String sqlDeleteAJ = "delete from article_journal where article_id = ? and journal_id = ? ;";
        String sqlDeleteArticle = "delete from article where id = ?;";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String journalId = null;
            int year = 0;
            int iniYear = 0;
            int finalYear = 0;
            int A = 0;
            int B = 0;

             try (PreparedStatement journalIdState = conn.prepareStatement(sqlGetJournalId);){
                 journalIdState.setString(1, article.getJournal().getTitle());
                 try (ResultSet journalSet = journalIdState.executeQuery();){
                     if (journalSet.next()) {
                         journalId = journalSet.getString("id");
                     }
                 }
             }

             try (PreparedStatement addArticleState = conn.prepareStatement(sqlAddArticle);){
                 addArticleState.setInt(1, article.getId());
                 addArticleState.setString(2, article.getTitle());
                 addArticleState.setString(3, article.getPub_model());
                 addArticleState.setDate(4, (Date) article.getCreated());
                 addArticleState.setDate(5, (Date) article.getCompleted());
                 addArticleState.execute();
             }

            try (PreparedStatement addAJState = conn.prepareStatement(sqlAddAJ);){

                addAJState.setString(1, journalId);
                addAJState.setInt(2, article.getId());
                addAJState.execute();
            }


            try (PreparedStatement getYearState = conn.prepareStatement(sqlGetYear);){
                getYearState.setInt(1, article.getId());
                try (ResultSet yearSet = getYearState.executeQuery();){
                    if (yearSet.next()){
                        year = yearSet.getInt("year");
                    }
                }
                year ++;
                iniYear = year - 2;
                finalYear = year - 1;
            }


            try (PreparedStatement AState = conn.prepareStatement(sqlA);){
                AState.setInt(1, year);
                AState.setInt(2, iniYear);
                AState.setInt(3, finalYear);
                AState.setString(4, article.getJournal().getTitle());
                try (ResultSet ASet = AState.executeQuery();){
                    if (ASet.next()){
                        A = ASet.getInt("sum");
                    }
                }
            }


            try (PreparedStatement BState = conn.prepareStatement(sqlB);){
                BState.setInt(1, iniYear);
                BState.setInt(2, finalYear);
                BState.setString(3, article.getJournal().getTitle());
                try (ResultSet BSet = BState.executeQuery();){
                    if (BSet.next()){
                        B = BSet.getInt("count");
                    }
                }
            }


            try (PreparedStatement deleteAJState = conn.prepareStatement(sqlDeleteAJ);){
                deleteAJState.setInt(1, article.getId());
                deleteAJState.setString(2, journalId);
                deleteAJState.execute();
            }

            try (PreparedStatement deleteArticleState = conn.prepareStatement(sqlDeleteArticle);){
                deleteArticleState.setInt(1, article.getId());
                deleteArticleState.execute();
            }

//            System.out.println("addArticleAndUpdateIF )))))))))))))))))))))))))))))))))))))))))))))))) ");
            return (double) A / B;


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
}
