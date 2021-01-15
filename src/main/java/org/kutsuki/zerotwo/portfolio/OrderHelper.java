package org.kutsuki.zerotwo.portfolio;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.spread.AbstractSpread;
import org.kutsuki.zerotwo.portfolio.spread.ButterflySpread;
import org.kutsuki.zerotwo.portfolio.spread.CondorSpread;
import org.kutsuki.zerotwo.portfolio.spread.DiagonalSpread;
import org.kutsuki.zerotwo.portfolio.spread.IronCondorSpread;
import org.kutsuki.zerotwo.portfolio.spread.RatioSpread;
import org.kutsuki.zerotwo.portfolio.spread.SingleSpread;
import org.kutsuki.zerotwo.portfolio.spread.UnbalancedButterflySpread;
import org.kutsuki.zerotwo.portfolio.spread.VerticalSpread;
import org.kutsuki.zerotwo.rest.post.tda.PostPlaceOrder;

public class OrderHelper {
    private static final DateTimeFormatter SYMBOL_DTF = DateTimeFormatter.ofPattern("MMddyy");

    private static final String AT = "@ ";
    private static final String BOT = "BOT ";
    private static final String BUY = "BUY ";
    private static final String HUNDRED = " 100 ";
    private static final String QUARTERLYS = " (Quarterlys) ";
    private static final String SELL = "SELL ";
    private static final String SOLD = "SOLD ";
    private static final String WEEKLYS = " (Weeklys) ";

    private List<AbstractSpread> spreadList;
    private Map<String, String> spreadComplexMap;

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

	this.spreadComplexMap = new HashMap<String, String>();
	this.spreadComplexMap.put("IRON CONDOR", "IRON_CONDOR");
	this.spreadComplexMap.put("~BUTTERFLY", "UNBALANCED_BUTTERFLY");
	this.spreadComplexMap.put("DIAGONAL", "DIAGONAL");
	this.spreadComplexMap.put("BACKRATIO", "BACK_RATIO");
	this.spreadComplexMap.put("VERTICAL", "VERTICAL");
	this.spreadComplexMap.put("BUTTERFLY", "BUTTERFLY");
	this.spreadComplexMap.put("CONDOR", "CONDOR");
	this.spreadComplexMap.put("SINGLE", "NONE");
    }

    public List<OrderModel> createOrders(String message) throws Exception {
	List<OrderModel> orderList = new ArrayList<OrderModel>();

	// remove unecessary data
	message = StringUtils.replace(message, HUNDRED, StringUtils.SPACE);
	message = StringUtils.replace(message, QUARTERLYS, StringUtils.SPACE);
	message = StringUtils.replace(message, WEEKLYS, StringUtils.SPACE);
	message = StringUtils.replace(message, AT, Character.toString('@'));

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

    // TODO determine limit/net debit/net credit/market
    // determine day or GTC
    // change working to LMT
    public PostPlaceOrder createPostOrder(OrderModel order) {
	String complex = spreadComplexMap.get(order.getSpread());

	PostPlaceOrder post = new PostPlaceOrder(complex, "LIMIT", order.getPriceBD().toString(), "DAY");
	// add legs
	return post;
    }

    public String getOrderSymbol(Position position) {
	StringBuilder sb = new StringBuilder();
	sb.append(position.getSymbol());
	sb.append('_');
	sb.append(SYMBOL_DTF.format(position.getExpiry()));
	sb.append(position.getType().toString().charAt(0));
	sb.append(position.getStrike());
	return sb.toString();
    }
}
