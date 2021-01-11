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
    public OrderModel parseOrder(String[] split, int tradeId, boolean am) throws Exception {
	int quantity = parseQuantity(split[0]);
	String symbol = parseSymbol(split[2]);

	int i = startIndex(split, 3);

	List<String> splitDate = parseSlashes(split[5 + i]);

	LocalDate expiry = parseExpiry(split[3 + i], split[4 + i], splitDate.get(0));
	LocalDate expiry2 = parseExpiry(splitDate.get(1), split[6 + i], split[7 + i]);
	List<BigDecimal> strikeList = parseSlashesBD(split[8 + i]);
	OptionType type = parseType(split[9 + i]);
	BigDecimal price = parsePrice(split[10 + i]);

	OrderModel order = new OrderModel(getSpread(), price, split[10 + i]);
	order.addPosition(new Position(tradeId, quantity, symbol, expiry, am, strikeList.get(0), type));
	order.addPosition(new Position(tradeId, -quantity, symbol, expiry2, am, strikeList.get(1), type));

	return order;
    }

    @Override
    public String getSpread() {
	return DIAGONAL;
    }
}
