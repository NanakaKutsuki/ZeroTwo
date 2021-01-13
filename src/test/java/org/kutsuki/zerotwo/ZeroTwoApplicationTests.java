package org.kutsuki.zerotwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.portfolio.PortfolioManager;
import org.kutsuki.zerotwo.repository.OpeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private OpeningRepository repository;

    @Autowired
    private PortfolioManager manager;

    @Test
    public void contextLoads() {
	boolean test = true;
	String msg = "#527 FILLED BOT +1 BUTTERFLY RIOT 22 JAN 21 25/30/35 CALL @.65 ISE";
	manager.parseMessage(msg, "", test);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
