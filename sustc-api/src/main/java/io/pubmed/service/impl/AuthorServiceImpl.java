package io.pubmed.service.impl;

import io.pubmed.dto.Author;
import io.pubmed.service.AuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
@Slf4j
public class AuthorServiceImpl implements AuthorService {
    @Autowired
    private DataSource dataSource;
    @Override
    public int[] getArticlesByAuthorSortedByCitations(Author author) {
        String sql = "select ai.article_id as id from article_ids ai\n" +
                "join article_authors aa on aa.article_id = ai.article_id\n" +
                "join authors au on aa.author_id = au.author_id\n" +
                "where ai.type = 'pubmed'\n" +
                "    and au.last_name = ? and au.fore_name = ? and au.initials = ?\n" +
                "order by ai.article_id desc;";

        String sqlCount = "select count(*) as count from article_ids ai\n" +
                "join article_authors aa on aa.article_id = ai.article_id\n" +
                "join authors au on aa.author_id = au.author_id\n" +
                "where ai.type = 'pubmed'\n" +
                "    and au.last_name = ? and au.fore_name = ? and au.initials = ?;";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if (author.getFore_name() == null){
                author.setFore_name("");
            }
            if (author.getInitials() == null){
                author.setInitials("");
            }
//            if (author.getLast_name() == null){
//                author.setLast_name(author.getCollective_name());
//            }
            int count = 0;

            try (PreparedStatement countState = conn.prepareStatement(sqlCount);){
                countState.setString(1, author.getLast_name());
                countState.setString(2, author.getInitials());
                countState.setString(3, author.getInitials());

                try (ResultSet countSet = countState.executeQuery();){
                    if (countSet.next()){
                        count = countSet.getInt("count");
                    }
                }

            }

            int[] result = new int[count];

            try (PreparedStatement statement = conn.prepareStatement(sql);){
                statement.setString(1, author.getLast_name());
                statement.setString(2, author.getInitials());
                statement.setString(3, author.getInitials());
                try (ResultSet resultSet = statement.executeQuery();){
                    int index = 0;
                    while (resultSet.next()){
                        result[index++] = resultSet.getInt("id");
                    }
                }
            }
            return result;

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

    @Override
    public String getJournalWithMostArticlesByAuthor(Author author) {
        String sql = "select journal_title  from count_article_for_author\n" +
                "where last_name = ? and fore_name = ? and initials = ?\n" +
                "order by count_article desc\n" +
                "limit 1;";

        Connection conn = null;
        try{
            conn = dataSource.getConnection();
            if (author.getFore_name() == null){
                author.setFore_name("");
            }
            if (author.getInitials() == null){
                author.setInitials("");
            }


            try (PreparedStatement statement = conn.prepareStatement(sql);){
                statement.setString(1, author.getLast_name());
                statement.setString(2, author.getFore_name());
                statement.setString(3, author.getInitials());
                try (ResultSet resultSet = statement.executeQuery();){
                    if (resultSet.next()){
                        return resultSet.getString("journal_title");
                    }
                }
            }
            return "";

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

    @Override
    public int getMinArticlesToLinkAuthors(Author A, Author E) {
        return 0;
    }
}
