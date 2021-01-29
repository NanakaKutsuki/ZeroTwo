package org.kutsuki.zerotwo.rest.post;

import java.util.ArrayList;
import java.util.List;

import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.OrderLegCollection;
import org.kutsuki.zerotwo.portfolio.OrderModel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class PostPlaceOrder {
    private static final String BUY_TO_CLOSE = "BUY_TO_CLOSE";
    private static final String BUY_TO_OPEN = "BUY_TO_OPEN";
    private static final String DAY = "DAY";
    private static final String GTC = "GOOD_TILL_CANCEL";
    private static final String NORMAL = "NORMAL";
    private static final String SELL_TO_CLOSE = "SELL_TO_CLOSE";
    private static final String SELL_TO_OPEN = "SELL_TO_OPEN";
    private static final String SINGLE = "SINGLE";

    private String complexOrderStrategyType;
    private String orderType;
    private String session;
    private String price;
    private String duration;
    private String orderStrategyType;
    private List<OrderLegCollection> orderLegCollection;

    private String stopPrice;
    private String stopType;

    @JsonIgnore
    private boolean working;

    @JsonIgnore
    private int orderId;

    @JsonIgnore
    private String key;

    public PostPlaceOrder(OrderModel order) {
	this.complexOrderStrategyType = order.getComplex();
	this.orderType = order.getOrderType();
	this.duration = order.isGTC() ? GTC : DAY;
	this.session = NORMAL;
	this.orderStrategyType = SINGLE;
	this.orderLegCollection = new ArrayList<OrderLegCollection>();
	this.working = order.isWorking();

	if (order.isStop()) {
	    this.price = order.getPrice().toString();
	    this.stopPrice = order.getPrice().toString();
	    this.stopType = "MARK";
	} else {
	    this.price = order.getPrice().toString();
	}

	for (Position position : order.getPositionList()) {
	    createLeg(position.getFullSymbol(), position.getQuantity(), position.isOpen());
	    this.key += position.getFullSymbol();
	}
    }

    public void createLeg(String symbol, int quantity, boolean open) {
	OrderLegCollection leg = new OrderLegCollection();

	if (open) {
	    if (quantity < 0) {
		leg.setInstruction(SELL_TO_OPEN);
	    } else {
		leg.setInstruction(BUY_TO_OPEN);
	    }
	} else {
	    if (quantity < 0) {
		leg.setInstruction(SELL_TO_CLOSE);
	    } else {
		leg.setInstruction(BUY_TO_CLOSE);
	    }
	}

	leg.setQuantity(Math.abs(quantity));
	leg.createInstrument(symbol);
	orderLegCollection.add(leg);
    }

    @Override
    public String toString() {
	String json = null;

	try {
	    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
	    json = ow.writeValueAsString(this);
	} catch (JsonProcessingException e) {
	    e.printStackTrace();
	}

	return json;
    }

    public String getComplexOrderStrategyType() {
	return complexOrderStrategyType;
    }

    public String getOrderType() {
	return orderType;
    }

    public String getSession() {
	return session;
    }

    public String getPrice() {
	return price;
    }

    public String getDuration() {
	return duration;
    }

    public String getOrderStrategyType() {
	return orderStrategyType;
    }

    public List<OrderLegCollection> getOrderLegCollection() {
	return orderLegCollection;
    }

    public String getStopPrice() {
	return stopPrice;
    }

    public String getStopType() {
	return stopType;
    }

    public int getOrderId() {
	return orderId;
    }

    public String getKey() {
	return key;
    }

    public boolean isWorking() {
	return working;
    }

    public void setOrderId(int orderId) {
	this.orderId = orderId;
    }
}
