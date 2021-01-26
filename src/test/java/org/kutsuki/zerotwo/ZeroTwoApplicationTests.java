package org.kutsuki.zerotwo;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderManager;
import org.kutsuki.zerotwo.portfolio.OrderModel;
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

	boolean stop = false;
	OrderModel model = new OrderModel("VERTICAL", "VERTICAL", "NET_DEBIT", BigDecimal.valueOf(0.05), false, stop,
		false, 0);
	model.addPosition(
		new Position(424, 1, "GME", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(42), OptionType.CALL));
	model.addPosition(new Position(424, -1, "GME", LocalDate.of(2021, 1, 22), false, BigDecimal.valueOf(43),
		OptionType.CALL));

//	PostPlaceOrder post = new PostPlaceOrder(model, true);
//	manager.placeOrder(model);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
