package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class DiagonalSpread extends AbstractSpread {
    private static final String DIAGONAL = "DIAGONAL";

    @Override
    protected OrderModel parseOrder() throws Exception {
	int quantity = parseQuantity(getDataList().get(0));
	// dataList.get(1) = DIAGONAL
	String symbol = parseSymbol(getDataList().get(2));

	// day month (year/day) month year
	List<String> splitDate = parseSlashes(getDataList().get(5));

	LocalDate expiry = parseExpiry(getDataList().get(3), getDataList().get(4), splitDate.get(0));
	LocalDate expiry2 = parseExpiry(splitDate.get(1), getDataList().get(6), getDataList().get(7));
	List<BigDecimal> strikeList = parseSlashesBD(getDataList().get(8));
	OptionType type = parseType(getDataList().get(9));
	BigDecimal price = parsePrice(getDataList().get(10));
	String orderType = parseOrderType(getDataList().get(10), quantity);

	OrderModel order = createOrder(orderType, price);
	order.addPosition(new Position(getTradeId(), quantity, symbol, expiry, strikeList.get(0), type));
	order.addPosition(new Position(getTradeId(), -quantity, symbol, expiry2, strikeList.get(1), type));

	return order;
    }

    @Override
    public String getComplex() {
	return DIAGONAL;
    }

    @Override
    public String getSpread() {
	return DIAGONAL;
    }
}
