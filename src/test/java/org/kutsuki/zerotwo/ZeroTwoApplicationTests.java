package org.kutsuki.zerotwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.openings.PlatinumReefOpenings;
import org.kutsuki.zerotwo.repository.OpeningsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private OpeningsRepository repository;

    @Autowired
    private PlatinumReefOpenings openings;

    @Test
    public void contextLoads() {
	// openings.clear();
	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
