package io.pubmed.service.impl;

import io.pubmed.service.GrantService;
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
public class GrantServiceImpl implements GrantService {
    @Autowired
    private DataSource dataSource;
    @Override
    public int[] getCountryFundPapers(String country) {
        String sql = "select ai.identifier as id from grant_info g\n" +
                "join article_grants ag on ag.grant_id = g.id\n" +
                "join article_ids ai on ai.article_id = ag.article_id\n" +
                "where g.country = ? and ai.type = 'pubmed';";
        String sqlCount = "select count(ai.identifier) as count from grant_info g\n" +
                "join article_grants ag on ag.grant_id = g.id\n" +
                "join article_ids ai on ai.article_id = ag.article_id\n" +
                "where g.country = ? and ai.type = 'pubmed';";

        Connection conn = null;
        try  {
            conn = dataSource.getConnection();
            int count = 0;

            try (PreparedStatement countState = conn.prepareStatement(sqlCount);){
                countState.setString(1, country);
                try (ResultSet countSet = countState.executeQuery();){
                    if (countSet.next()){
                        count = countSet.getInt("count");
                    }
                }
            }

            int[] result = new int[count];

            try (PreparedStatement statement = conn.prepareStatement(sql);){
                statement.setString(1, country);
                int index = 0;
                try (ResultSet resultSet = statement.executeQuery();){
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
}
