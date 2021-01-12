package org.kutsuki.zerotwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.repository.OpeningsRepository;
import org.kutsuki.zerotwo.rest.openings.ImagineDragonRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private OpeningsRepository repository;

    @Autowired
    private ImagineDragonRest rest;

    @Test
    public void contextLoads() {
	// boolean test = true;
	// String escaped = "#522 FILLED BOT 2 BUTTERFLY SPX 100 15 JAN 21 [AM]
	// 3645/3630/3600 PUT @-.40cr CBOE";
	// manager.parseAlert(escaped, "", test);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
