package org.kutsuki.zerotwo.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;

public class IronCondorSpread extends AbstractSpread {
    private static final String IRON_CONDOR = "IRON CONDOR";

    @Override
    public OrderModel parseOrder(String[] split) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[3]);

	int i = startIndex(split, 4);

	LocalDate expiry = parseExpiry(split[4 + i], split[5 + i], split[6 + i]);
	List<BigDecimal> strikeList = parseSlashesBD(split[7 + i]);
	List<OptionType> type = parseTypes(split[8 + i]);
	BigDecimal price = parsePrice(split[9 + i]);

	OrderModel order = new OrderModel(getSpread(), price, split[9 + i]);
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(0), type.get(0)));
	order.addPosition(new Position(-quantity, symbol, expiry, strikeList.get(1), type.get(0)));
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(2), type.get(1)));
	order.addPosition(new Position(quantity, symbol, expiry, strikeList.get(3), type.get(1)));

	return order;
    }

    @Override
    public String getSpread() {
	return IRON_CONDOR;
    }
}
