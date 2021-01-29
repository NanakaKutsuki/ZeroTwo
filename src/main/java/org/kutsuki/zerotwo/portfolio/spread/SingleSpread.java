package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class SingleSpread extends AbstractSpread {
    private static final String LIMIT = "LIMIT";
    private static final String SINGLE = "SINGLE";
    private static final String SINGLE_COMPLEX = "NONE";

    @Override
    protected OrderModel parseOrder() throws Exception {
	int quantity = parseQuantity(getDataList().get(0));
	String symbol = parseSymbol(getDataList().get(1));
	LocalDate expiry = parseExpiry(getDataList().get(2), getDataList().get(3), getDataList().get(4));
	BigDecimal strike = parseStrike(getDataList().get(5));
	OptionType type = parseType(getDataList().get(6));
	BigDecimal price = parsePrice(getDataList().get(7));
	String orderType = LIMIT;

	OrderModel order = createOrder(type.toString(), orderType, price);
	order.addPosition(new Position(getTradeId(), quantity, symbol, expiry, strike, type));

	return order;
    }

    @Override
    public String getComplex() {
	return SINGLE_COMPLEX;
    }

    @Override
    public String getSpread() {
	return SINGLE;
    }
}
