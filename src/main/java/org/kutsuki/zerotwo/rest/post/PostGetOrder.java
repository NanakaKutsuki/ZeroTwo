package org.kutsuki.zerotwo.rest.post;

import org.kutsuki.zerotwo.portfolio.OrderLegCollection;

public class PostGetOrder {
    private String complexOrderStrategyType;
    private OrderLegCollection[] orderLegCollection;
    private int orderId;

    public String getComplexOrderStrategyType() {
	return complexOrderStrategyType;
    }

    public OrderLegCollection[] getOrderLegCollection() {
	return orderLegCollection;
    }

    public int getOrderId() {
	return orderId;
    }
}
