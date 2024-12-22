package io.pubmed.service.impl;

import io.pubmed.service.KeywordService;
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
public class KeywordServiceImpl implements KeywordService {
    @Autowired
    private DataSource dataSource;
    @Override
    public int[] getArticleCountByKeywordInPastYears(String keyword) {
        String sql = "select count(keyword) count from keywords k\n" +
                "join article_keywords ak on ak.keyword_id = k.id\n" +
                "join article a on ak.article_id = a.id\n" +
                "where keyword = ?\n" +
                "group by extract(year from date_completed), keyword\n" +
                "order by extract(year from date_completed) desc;";

        String sqlCount = "select count (sub) from (\n" +
                "select count(keyword) count, keyword from keywords k\n" +
                "join article_keywords ak on ak.keyword_id = k.id\n" +
                "join article a on ak.article_id = a.id\n" +
                "where keyword = ?\n" +
                "group by extract(year from date_completed), keyword\n" +
                ") as sub;";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            int count = 0;

            try (PreparedStatement countState = conn.prepareStatement(sqlCount);){
                countState.setString(1, keyword);

                try (ResultSet countSet = countState.executeQuery();){
                    if (countSet.next()){
                        count = countSet.getInt("count");
                    }
                }
            }

            int[] result = new int[count];

            try (PreparedStatement statement = conn.prepareStatement(sql);){
                statement.setString(1, keyword);
                try (ResultSet resultSet = statement.executeQuery();){
                    int index = 0;
                    while (resultSet.next()){
                        result[index++] = resultSet.getInt("count");
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
}
