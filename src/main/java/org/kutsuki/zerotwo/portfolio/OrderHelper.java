package org.kutsuki.zerotwo.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.portfolio.spread.AbstractSpread;
import org.kutsuki.zerotwo.portfolio.spread.ButterflySpread;
import org.kutsuki.zerotwo.portfolio.spread.CondorSpread;
import org.kutsuki.zerotwo.portfolio.spread.DiagonalSpread;
import org.kutsuki.zerotwo.portfolio.spread.IronCondorSpread;
import org.kutsuki.zerotwo.portfolio.spread.RatioSpread;
import org.kutsuki.zerotwo.portfolio.spread.SingleSpread;
import org.kutsuki.zerotwo.portfolio.spread.UnbalancedButterflySpread;
import org.kutsuki.zerotwo.portfolio.spread.VerticalSpread;

public class OrderHelper {
    private static final String AT = "@ ";
    private static final String BOT = "BOT ";
    private static final String BUY = "BUY ";
    private static final String HUNDRED = " 100 ";
    private static final String SELL = "SELL ";
    private static final String SOLD = "SOLD ";

    private List<AbstractSpread> spreadList;

    public OrderHelper() {
	// order matters
	this.spreadList = new ArrayList<AbstractSpread>();
	this.spreadList.add(new IronCondorSpread());
	this.spreadList.add(new UnbalancedButterflySpread());
	this.spreadList.add(new DiagonalSpread());
	this.spreadList.add(new RatioSpread());
	this.spreadList.add(new VerticalSpread());
	this.spreadList.add(new ButterflySpread());
	this.spreadList.add(new CondorSpread());
	this.spreadList.add(new SingleSpread());
    }

    public List<OrderModel> createOrders(String message) throws Exception {
	List<OrderModel> orderList = new ArrayList<OrderModel>();

	// remove unecessary data
	message = StringUtils.replace(message, AT, Character.toString('@'));
	message = StringUtils.replace(message, HUNDRED, StringUtils.SPACE);

	// parse tradeId
	int tradeId = -1;
	if (StringUtils.startsWith(message, Character.toString('#'))) {
	    String id = StringUtils.substringBetween(message, Character.toString('#'), StringUtils.SPACE);
	    tradeId = Integer.parseInt(id);
	}

	for (String split : StringUtils.split(message, '&')) {
	    boolean skip = false;

	    if (StringUtils.contains(split, BOT)) {
		split = StringUtils.substringAfter(split, BOT);
	    } else if (StringUtils.contains(split, BUY)) {
		split = StringUtils.substringAfter(split, BUY);
	    } else if (StringUtils.contains(split, SELL)) {
		split = StringUtils.substringAfter(split, SELL);
	    } else if (StringUtils.contains(split, SOLD)) {
		split = StringUtils.substringAfter(split, SOLD);
	    } else {
		skip = true;
	    }

	    boolean found = false;
	    int i = 0;
	    while (!skip && !found && i < spreadList.size() - 1) {
		AbstractSpread spread = spreadList.get(i);

		if (StringUtils.contains(split, spread.getSpread())) {
		    orderList.add(spread.parseOrder(split, tradeId));
		    found = true;
		}

		i++;
	    }

	    // last one is the Single Spread
	    if (!skip && !found) {
		AbstractSpread spread = spreadList.get(spreadList.size() - 1);
		orderList.add(spread.parseOrder(split, tradeId));
	    }
	}

	return orderList;
    }
}
