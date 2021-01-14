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
    protected OrderModel parseOrder(List<String> dataList, int tradeId, boolean am, boolean stop, BigDecimal condition)
	    throws Exception {
	int quantity = parseQuantity(dataList.get(0));
	// dataList.get(1) = DIAGONAL
	String symbol = parseSymbol(dataList.get(2));

	// day month (year/day) month year
	List<String> splitDate = parseSlashes(dataList.get(5));

	LocalDate expiry = parseExpiry(dataList.get(3), dataList.get(4), splitDate.get(0));
	LocalDate expiry2 = parseExpiry(splitDate.get(1), dataList.get(6), dataList.get(7));
	List<BigDecimal> strikeList = parseSlashesBD(dataList.get(8));
	OptionType type = parseType(dataList.get(9));
	BigDecimal price = parsePrice(dataList.get(10), condition);

	OrderModel order = new OrderModel(getSpread(), price, dataList.get(10), stop, condition);
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, am, strikeList.get(0), type));
	order.addPosition(new Position(tradeId, -quantity, symbol, expiry2, am, strikeList.get(1), type));

	return order;
    }

    @Override
    public String getSpread() {
	return DIAGONAL;
    }
}
