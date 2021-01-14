package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class IronCondorSpread extends AbstractSpread {
    private static final String IRON_CONDOR = "IRON CONDOR";

    @Override
    protected OrderModel parseOrder(List<String> dataList, int tradeId, boolean am, boolean stop, BigDecimal condition)
	    throws Exception {
	int quantity = parseQuantity(dataList.get(0));
	// dataList.get(1) = IRON
	// dataList.get(2) = CONDOR
	String symbol = parseSymbol(dataList.get(3));
	LocalDate expiry = parseExpiry(dataList.get(4), dataList.get(5), dataList.get(6));
	List<BigDecimal> strikeList = parseSlashesBD(dataList.get(7));
	List<OptionType> type = parseTypes(dataList.get(8));
	BigDecimal price = parsePrice(dataList.get(9), condition);

	OrderModel order = new OrderModel(getSpread(), price, dataList.get(9), stop, condition);
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, am, strikeList.get(0), type.get(0)));
	order.addPosition(new Position(tradeId, -quantity, symbol, expiry, am, strikeList.get(1), type.get(0)));
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, am, strikeList.get(2), type.get(1)));
	order.addPosition(new Position(tradeId, -quantity, symbol, expiry, am, strikeList.get(3), type.get(1)));

	return order;
    }

    @Override
    public String getSpread() {
	return IRON_CONDOR;
    }
}
