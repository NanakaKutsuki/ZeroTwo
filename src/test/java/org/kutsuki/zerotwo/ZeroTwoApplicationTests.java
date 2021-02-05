package org.kutsuki.zerotwo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.portfolio.OrderManager;
import org.kutsuki.zerotwo.repository.TdaPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ZeroTwoApplicationTests {
    @Autowired
    private TdaPositionRepository repository;

    @Autowired
    private OrderManager manager;

    @Test
    public void contextLoads() {
	// boolean test = true;
	// String msg = "";
	// manager.parseMessage(msg, "", test);

//	boolean stop = true;
//	OrderModel model = new OrderModel("VERTICAL", "VERTICAL", "NET_CREDIT", BigDecimal.valueOf(0.25), false, stop,
//		false, 0);
//	model.addPosition(
//		new Position(424, -1, "INTC", LocalDate.of(2021, 2, 5), BigDecimal.valueOf(42), OptionType.CALL));
//	model.addPosition(
//		new Position(424, 1, "INTC", LocalDate.of(2021, 2, 5), BigDecimal.valueOf(43), OptionType.CALL));

//	manager.placeStopOrder(model);

	// repository.save(new TdaPosition("AAPL_020521P132", 4, LocalDate.of(2021, 2,
	// 5)));
	// repository.save(new TdaPosition("AAPL_020521P130", -8, LocalDate.of(2021, 2,
	// 5)));

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
