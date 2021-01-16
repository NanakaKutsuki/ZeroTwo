package org.kutsuki.zerotwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.repository.OpeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private OpeningRepository repository;

    // @Autowired
    // private OrderManager manager;

    @Test
    public void contextLoads() {
	// boolean test = true;
	// String msg = "";
	// manager.parseMessage(msg, "", test);

	// manager.refreshToken();
	// String temp =
	// manager.setTemp(temp);
	// manager.placeOrder();

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
