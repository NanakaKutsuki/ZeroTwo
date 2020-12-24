package org.kutsuki.zerotwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.portfolio.PortfolioManager;
import org.kutsuki.zerotwo.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private PortfolioRepository repository;

    @Autowired
    private PortfolioManager manager;

    @Test
    public void contextLoads() {
	boolean test = true;
	String escaped = "";
	manager.parseAlert(escaped, test);
	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
