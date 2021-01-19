package org.kutsuki.zerotwo.rest.post.tda;

import java.util.ArrayList;
import java.util.List;

import org.kutsuki.zerotwo.portfolio.OrderLegCollection;
import org.kutsuki.zerotwo.portfolio.OrderModel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class PostPlaceOrder {
    private static final String DAY = "DAY";
    private static final String GTC = "GOOD_TILL_CANCEL";
    private static final String NORMAL = "NORMAL";
    private static final String SINGLE = "SINGLE";

    private String complexOrderStrategyType;
    private String orderType;
    private String session;
    private String price;
    private String duration;
    private String orderStrategyType;
    private List<OrderLegCollection> orderLegCollection;

    public PostPlaceOrder(String complex, String orderType, String price, String duration) {
	this.complexOrderStrategyType = complex;
	this.orderType = orderType;
	this.price = price;
	this.duration = duration;
	this.session = NORMAL;
	this.orderStrategyType = SINGLE;
	this.orderLegCollection = new ArrayList<OrderLegCollection>();
    }

    public PostPlaceOrder(OrderModel order) {
	this.complexOrderStrategyType = order.getComplex();
	this.orderType = order.getOrderType();
	this.price = order.getPrice().toString();
	this.duration = order.isGTC() ? GTC : DAY;
	this.session = NORMAL;
	this.orderStrategyType = SINGLE;

	this.orderLegCollection = new ArrayList<OrderLegCollection>();
	// TODO position to order leg
    }

    public void createOrderLegCollection(String instruction, int quantity, String symbol) {
	OrderLegCollection leg = new OrderLegCollection();
	leg.setInstruction(instruction);
	leg.setQuantity(quantity);
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
}
