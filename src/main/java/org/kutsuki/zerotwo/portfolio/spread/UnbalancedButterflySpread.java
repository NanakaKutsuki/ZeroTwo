package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

public class UnbalancedButterflySpread extends AbstractSpread {
    private static final String UNBALANCED_BUTTERFLY = "~BUTTERFLY";
    private static final String UNBALANCED_BUTTERFLY_COMPLEX = "BUTTERFLY";

    @Override
    protected OrderModel parseOrder() throws Exception {
	int quantity = parseQuantity(getDataList().get(0));
	List<BigDecimal> ratioList = parseSlashesBD(getDataList().get(1));
	// dataList.get(2) = ~BUTTERFLY
	String symbol = parseSymbol(getDataList().get(3));
	LocalDate expiry = parseExpiry(getDataList().get(4), getDataList().get(5), getDataList().get(6));
	List<BigDecimal> strikeList = parseSlashesBD(getDataList().get(7));
	OptionType type = parseType(getDataList().get(8));
	BigDecimal price = parsePrice(getDataList().get(9));
	String orderType = parseOrderType(getDataList().get(9), quantity);

	OrderModel order = createOrder(orderType, price);
	int qty1 = quantity * ratioList.get(0).intValue();
	order.addPosition(new Position(getTradeId(), qty1, symbol, expiry, isAM(), strikeList.get(0), type));
	int qty2 = -quantity * ratioList.get(1).intValue();
	order.addPosition(new Position(getTradeId(), qty2, symbol, expiry, isAM(), strikeList.get(1), type));
	int qty3 = quantity * ratioList.get(2).intValue();
	order.addPosition(new Position(getTradeId(), qty3, symbol, expiry, isAM(), strikeList.get(2), type));

	return order;
    }

    @Override
    public String getComplex() {
	return UNBALANCED_BUTTERFLY_COMPLEX;
    }

    @Override
    public String getSpread() {
	return UNBALANCED_BUTTERFLY;
    }
}
