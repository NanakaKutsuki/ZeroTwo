package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class IronCondorSpread extends AbstractSpread {
    private static final String IRON_CONDOR = "IRON CONDOR";
    private static final String IRON_CONDOR_COMPLEX = "IRON_CONDOR";

    @Override
    protected OrderModel parseOrder() throws Exception {
	int quantity = parseQuantity(getDataList().get(0));
	// dataList.get(1) = IRON
	// dataList.get(2) = CONDOR
	String symbol = parseSymbol(getDataList().get(3));
	LocalDate expiry = parseExpiry(getDataList().get(4), getDataList().get(5), getDataList().get(6));
	List<BigDecimal> strikeList = parseSlashesBD(getDataList().get(7));
	List<OptionType> type = parseTypes(getDataList().get(8));
	BigDecimal price = parsePrice(getDataList().get(9));
	String orderType = parseOrderType(getDataList().get(9), quantity);

	OrderModel order = createOrder(orderType, price);
	order.addPosition(new Position(getTradeId(), quantity, symbol, expiry, strikeList.get(0), type.get(0)));
	order.addPosition(new Position(getTradeId(), -quantity, symbol, expiry, strikeList.get(1), type.get(0)));
	order.addPosition(new Position(getTradeId(), quantity, symbol, expiry, strikeList.get(2), type.get(1)));
	order.addPosition(new Position(getTradeId(), -quantity, symbol, expiry, strikeList.get(3), type.get(1)));

	return order;
    }

    @Override
    public String getComplex() {
	return IRON_CONDOR_COMPLEX;
    }

    @Override
    public String getSpread() {
	return IRON_CONDOR;
    }
}
