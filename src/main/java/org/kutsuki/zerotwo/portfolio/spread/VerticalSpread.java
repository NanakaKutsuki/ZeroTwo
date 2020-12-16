package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class VerticalSpread extends AbstractSpread {
    private static final String VERTICAL = "VERTICAL";

    @Override
    public OrderModel parseOrder(String[] split, int tradeId) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[2]);

	int i = startIndex(split, 3);

	LocalDate expiry = parseExpiry(split[3 + i], split[4 + i], split[5 + i]);
	List<BigDecimal> strikeList = parseSlashesBD(split[6 + i]);
	OptionType type = parseType(split[7 + i]);
	BigDecimal price = parsePrice(split[8 + i]);

	OrderModel order = new OrderModel(getSpread(), price, split[8 + i]);
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, strikeList.get(0), type));
	order.addPosition(new Position(tradeId, -quantity, symbol, expiry, strikeList.get(1), type));

	return order;
    }

    @Override
    public String getSpread() {
	return VERTICAL;
    }
}