package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class SingleSpread extends AbstractSpread {
    private static final String SINGLE = "SINGLE";

    @Override
    public String getSpread() {
	return SINGLE;
    }

    @Override
    public OrderModel parseOrder(String[] split, int tradeId) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[1]);

	int i = startIndex(split, 2);

	LocalDate expiry = parseExpiry(split[2 + i], split[3 + i], split[4 + i]);
	BigDecimal strike = parseStrike(split[5 + i]);
	OptionType type = parseType(split[6 + i]);
	BigDecimal price = parsePrice(split[7 + i]);

	OrderModel order = new OrderModel(type.toString(), price, split[7 + i]);
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, strike, type));

	return order;
    }

}
