package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class ButterflySpread extends AbstractSpread {
    private static final String BUTTERFLY = "BUTTERFLY";

    @Override
    protected OrderModel parseOrder() throws Exception {
	int quantity = parseQuantity(getDataList().get(0));
	// dataList.get(1) = BUTTERFLY
	String symbol = parseSymbol(getDataList().get(2));
	LocalDate expiry = parseExpiry(getDataList().get(3), getDataList().get(4), getDataList().get(5));
	List<BigDecimal> strikeList = parseSlashesBD(getDataList().get(6));
	OptionType type = parseType(getDataList().get(7));
	BigDecimal price = parsePrice(getDataList().get(8));
	String orderType = parseOrderType(getDataList().get(8), quantity);

	OrderModel order = createOrder(orderType, price);
	order.addPosition(new Position(getTradeId(), quantity, symbol, expiry, isAM(), strikeList.get(0), type));
	int qty2 = -quantity * 2;
	order.addPosition(new Position(getTradeId(), qty2, symbol, expiry, isAM(), strikeList.get(1), type));
	order.addPosition(new Position(getTradeId(), quantity, symbol, expiry, isAM(), strikeList.get(2), type));

	return order;
    }

    @Override
    public String getComplex() {
	return BUTTERFLY;
    }

    @Override
    public String getSpread() {
	return BUTTERFLY;
    }
}
