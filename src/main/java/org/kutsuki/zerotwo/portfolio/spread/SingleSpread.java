package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class SingleSpread extends AbstractSpread {
    private static final String SINGLE = "SINGLE";

    @Override
    protected OrderModel parseOrder(List<String> dataList, int tradeId, boolean am, boolean stop, BigDecimal condition)
	    throws Exception {
	int quantity = parseQuantity(dataList.get(0));
	String symbol = parseSymbol(dataList.get(1));
	LocalDate expiry = parseExpiry(dataList.get(2), dataList.get(3), dataList.get(4));
	BigDecimal strike = parseStrike(dataList.get(5));
	OptionType type = parseType(dataList.get(6));
	BigDecimal price = parsePrice(dataList.get(7), condition);

	OrderModel order = new OrderModel(type.toString(), price, dataList.get(7), stop, condition);
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, am, strike, type));

	return order;
    }

    @Override
    public String getSpread() {
	return SINGLE;
    }

}
