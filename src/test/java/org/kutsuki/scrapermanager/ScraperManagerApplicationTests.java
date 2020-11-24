package org.kutsuki.scrapermanager;

import org.junit.jupiter.api.Test;
import org.kutsuki.scrapermanager.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ScraperManagerApplicationTests {
    @Autowired
    private PortfolioRepository repository;

    @Test
    void contextLoads() {
	System.out.println(repository.findAll());
    }
}
