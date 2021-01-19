package org.kutsuki.zerotwo.portfolio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.document.Position;

public class OrderModel {
    private boolean gtc;
    private boolean stop;
    private boolean working;
    private BigDecimal price;
    private List<Position> positionList;
    private int condition;
    private String complex;
    private String orderType;
    private String spread;

    public OrderModel(String spread, String complex, String orderType, BigDecimal price, boolean gtc, boolean stop,
	    boolean working, int condition) {
	this.complex = complex;
	this.condition = condition;
	this.gtc = gtc;
	this.orderType = orderType;
	this.positionList = new ArrayList<Position>();
	this.price = price;
	this.spread = spread;
	this.stop = stop;
	this.working = working;
    }

    public void addPosition(Position model) {
	positionList.add(model);
    }

    public boolean isGTC() {
	return gtc;
    }

    public boolean isStop() {
	return stop;
    }

    public boolean isWorking() {
	return working;
    }

    public String getComplex() {
	return complex;
    }

    public int getCondition() {
	return condition;
    }

    public String getOrderType() {
	return orderType;
    }

    public List<Position> getPositionList() {
	return positionList;
    }

    public BigDecimal getPrice() {
	return price;
    }

    public String getSpread() {
	return spread;
    }

    public String getSymbol() {
	String symbol = StringUtils.EMPTY;

	if (positionList.size() > 0) {
	    symbol = positionList.get(0).getSymbol();
	}

	return symbol;
    }
}
