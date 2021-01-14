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
    protected OrderModel parseOrder(List<String> dataList, int tradeId, boolean am, boolean stop, BigDecimal condition)
	    throws Exception {
	int quantity = parseQuantity(dataList.get(0));
	// dataList.get(1) = VERTICAL
	String symbol = parseSymbol(dataList.get(2));
	LocalDate expiry = parseExpiry(dataList.get(3), dataList.get(4), dataList.get(5));
	List<BigDecimal> strikeList = parseSlashesBD(dataList.get(6));
	OptionType type = parseType(dataList.get(7));
	BigDecimal price = parsePrice(dataList.get(8), condition);

	OrderModel order = new OrderModel(getSpread(), price, dataList.get(8), stop, condition);
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, am, strikeList.get(0), type));
	order.addPosition(new Position(tradeId, -quantity, symbol, expiry, am, strikeList.get(1), type));

	return order;
    }

    @Override
    public String getSpread() {
	return VERTICAL;
    }
}
