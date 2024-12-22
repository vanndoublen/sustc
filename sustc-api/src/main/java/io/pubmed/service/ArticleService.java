package io.pubmed.service;

import io.pubmed.dto.Article;

public interface ArticleService {

    /**
     * Find the number of citations for an article in a given year
     *
     * @param id   the article's id
     * @param year need queried year. Note that year of any article is its field Article.created.
     * @return the number of article's citations in given year
     */
    int getArticleCitationsByYear(int id, int year);


    /**
     * Fist, add one article to your database
     * Second, output the journal IF after adding this article 1 year later
     * (For example: if we add an article whose created year is 2022, we need to calculate the IF in 2023 after adding this article)
     * Third, delete the article from your database
     * In this case, we make sure that each test will be an independent event. The results of one test do not affect the others.
     * <p>
     * Note that year of any article is its field Article.created.
     * <p>
     * if year = 2024, you need sum citations of given journal in 2024 /
     * [2022-2023] published articles num in the journal.
     * Example:
     * IF（2024） = A / B
     * A = The number of times all articles in the journal from 2022 to 2023 were cited in 2024.
     * B = Number of articles in the journal from 2022 to 2023.
     *
     * @param article all the article's info
     * @return the updated IF of given article's Journal
     */
    double addArticleAndUpdateIF(Article article);
}
