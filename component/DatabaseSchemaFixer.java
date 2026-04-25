package com.rabbiter.oes.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaFixer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            System.out.println("Checking database schema for 'wrong_question' table...");

            // Try to query the analysis column to see if it exists
            try {
                jdbcTemplate.queryForList("SELECT analysis FROM wrong_question LIMIT 1");
                System.out.println("Column 'analysis' already exists in 'wrong_question'.");
            } catch (Exception e) {
                // Column likely doesn't exist, try to add it
                System.out.println("Column 'analysis' not found. Attempting to add it...");
                try {
                    jdbcTemplate.execute("ALTER TABLE wrong_question ADD COLUMN analysis TEXT COMMENT '题目解析'");
                    System.out.println("Successfully added column 'analysis' to 'wrong_question'.");
                } catch (Exception ex) {
                    System.err.println("Failed to add column 'analysis': " + ex.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("DatabaseSchemaFixer encountered an error: " + e.getMessage());
        }
    }
}
