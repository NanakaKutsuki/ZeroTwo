package org.kutsuki.zerotwo;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderManager;
import org.kutsuki.zerotwo.portfolio.OrderModel;
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

	String token = "FYRkOfATrEl8W0bfWmMsfWNlvDxY7HXRRdzEK01+lPhyGo0RwKZD6CgS7nfrwCnndYg8Yz319pshOuo3tIb9Yj6O5b8ADQdda2t6ACt/q2GVYsvRDNa0DT7k28CnMOzv3+UQkK51xJsn2pi4Zk4MBwxKDYUQrvtQeuTLHi+DvgIF/d6PWSY++PyUT08c3vqTgO361r8ToXHOAGtRwIBH6bSvLZsno7YBLZy8NToNh/Z7FEM5XBbSBJVmTcq9bJ3Rv2BK//03zTzV1M5PvCNekaeLdp5OPJB5Pj//nwAQC+MhoY5ZnkIxKEZYNNTfRXp9V5mWeWdOfRNUNizFCjkcXpPOcMH+ClZ476NFfygwJCeWV6pYk1ERefC2NIn0NM4b9aEHmCQ1g54YXO3WIp4F6mLM4itQ484YNutbYmdDN1FLpoISX48OadV4ZCYNjQIMlm2NUl4aSuF+imt+xG4muvJCVuo2+9QZ5i/6xGW8oeszkIECF4xMiPEz0UUT2IgXJCZEJkpohhwMNeHIvI5lQkew6SBfI/hp9U6eAURuW/7FgJFMk100MQuG4LYrgoVi/JHHvlNNfE3gtkMenip1GyuPXXaeOBoAkJmqNRXSpprP6OwFE1OqNcCirFs733ZqJoX2mfMoCYRYszfVQ25Hvce/TCUZhCb7ZOE/duXsrkPVYOwJGLrYbg11baOgJBEUmgltx1CaQx0wlBd8qAQCxCV6Cjhy0PCyzym27i++HmUJCJjCceapNDGjOOTEL8dM8GGmQtdXld4EM7jlECw6GmmTxQKfFRoJPNQdpDFkmGRgIggEQY11X9LJD6i+m0Bgu7ONnGTAzrJbQvV9oBxOpEIMTMXwyTzbamTE6N7nqzLp38i6exlrUj5hiU90QBBAITZYOiu8kWF6RAThrTlDSNKGn5yXvESzF/67IpJjLxb5S9XSKTWwZCxr3BO5TDws32Uwig0yEwp1oWTsXaUzN0pBy/ZgVFhsg6+ECvP2juFqadfVMFHG6yf10Y6N48UZXLBqChjl0Vvavq17FrMCAFJLlIvAFLn95zu75277ajx/+sop5uoXnCzFKHK2j7BD4Hur9n6on0pDs/SLIcqloiCmxO2oHbfTkNCBTO/jqfkG0nFQHvpBdg==212FD3x19z9sWBHDJACbC00B75E";
	manager.setToken(token);

	boolean stop = true;
	OrderModel model = new OrderModel("VERTICAL", "VERTICAL", "NET_CREDIT", BigDecimal.valueOf(0.25), false, stop,
		false, 0);
	model.addPosition(new Position(424, -1, "INTC", LocalDate.of(2021, 1, 29), false, BigDecimal.valueOf(42),
		OptionType.CALL));
	model.addPosition(new Position(424, 1, "INTC", LocalDate.of(2021, 1, 29), false, BigDecimal.valueOf(43),
		OptionType.CALL));

	manager.placeStopOrder(model);

	Assertions.assertTrue(repository.count() > 0, "Should be stuff in the database");
    }
}
