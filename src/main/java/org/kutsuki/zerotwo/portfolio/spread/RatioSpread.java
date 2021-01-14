package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class RatioSpread extends AbstractSpread {
    private static final String BACKRATIO = "BACKRATIO";

    @Override
    protected OrderModel parseOrder(List<String> dataList, int tradeId, boolean am, boolean stop, BigDecimal condition)
	    throws Exception {
	int quantity = parseQuantity(dataList.get(0));
	List<BigDecimal> ratioList = parseSlashesBD(dataList.get(1));
	// dataList.get(2) = BACKRATIO
	String symbol = parseSymbol(dataList.get(3));
	LocalDate expiry = parseExpiry(dataList.get(4), dataList.get(5), dataList.get(6));
	List<BigDecimal> strikeList = parseSlashesBD(dataList.get(7));
	OptionType type = parseType(dataList.get(8));
	BigDecimal price = parsePrice(dataList.get(9), condition);

	OrderModel order = new OrderModel(getSpread(), price, dataList.get(9), stop, condition);
	int qty1 = -quantity * ratioList.get(0).intValue();
	order.addPosition(new Position(tradeId, qty1, symbol, expiry, am, strikeList.get(0), type));
	int qty2 = quantity * ratioList.get(1).intValue();
	order.addPosition(new Position(tradeId, qty2, symbol, expiry, am, strikeList.get(1), type));

	return order;
    }

    @Override
    public String getSpread() {
	return BACKRATIO;
    }
}
