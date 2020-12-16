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
    public OrderModel parseOrder(String[] split, int tradeId) throws Exception {
	int quantity = parseQuantity(split[0]);
	List<BigDecimal> ratioList = parseSlashesBD(split[1]);
	String symbol = parseSymbol(split[3]);

	int i = startIndex(split, 4);

	LocalDate expiry = parseExpiry(split[4 + i], split[5 + i], split[6 + i]);
	List<BigDecimal> strikeList = parseSlashesBD(split[7 + i]);
	OptionType type = parseType(split[8 + i]);
	BigDecimal price = parsePrice(split[9 + i]);

	OrderModel order = new OrderModel(getSpread(), price, split[9 + i]);
	int qty1 = -quantity * ratioList.get(0).intValue();
	order.addPosition(new Position(tradeId, qty1, symbol, expiry, strikeList.get(0), type));
	int qty2 = quantity * ratioList.get(1).intValue();
	order.addPosition(new Position(tradeId, qty2, symbol, expiry, strikeList.get(1), type));

	return order;
    }

    @Override
    public String getSpread() {
	return BACKRATIO;
    }
}
