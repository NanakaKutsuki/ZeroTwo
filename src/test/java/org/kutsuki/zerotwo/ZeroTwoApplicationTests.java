package org.kutsuki.zerotwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.portfolio.OrderManager;
import org.kutsuki.zerotwo.repository.SkipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private SkipRepository repository;

    @Autowired
    private OrderManager manager;

    @Test
    public void contextLoads() {
	// boolean test = true;
	// String msg = "";
	// manager.parseMessage(msg, "", test);

//	boolean stop = true;
//	OrderModel model = new OrderModel("VERTICAL", "VERTICAL", "NET_CREDIT", BigDecimal.valueOf(0.05), false, stop,
//		false, 0);
//	model.addPosition(
//		new Position(0, -1, "GME", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(42), OptionType.CALL));
//	model.addPosition(
//		new Position(0, 1, "GME", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(43), OptionType.CALL));
//
//	PostPlaceOrder post = new PostPlaceOrder(model, true);
//	manager.placeOrder(model);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
