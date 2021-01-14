package org.kutsuki.zerotwo.portfolio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.document.Position;

public class OrderModel {
    private boolean stop;
    private BigDecimal priceBD;
    private List<Position> positionList;
    private int condition;
    private String price;
    private String spread;

    public OrderModel(String spread, BigDecimal priceBD, String price, boolean stop, BigDecimal condition) {
	this.condition = condition.compareTo(BigDecimal.ZERO);
	this.positionList = new ArrayList<Position>();
	this.priceBD = priceBD;
	this.spread = spread;
	this.stop = stop;

	if (this.condition == 0) {
	    this.price = price;
	} else {
	    this.price = priceBD.toString();
	}
    }

    public void addPosition(Position model) {
	positionList.add(model);
    }

    public boolean isStop() {
	return stop;
    }

    public int getCondition() {
	return condition;
    }

    public List<Position> getPositionList() {
	return positionList;
    }

    public BigDecimal getPriceBD() {
	return priceBD;
    }

    public String getPrice() {
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
