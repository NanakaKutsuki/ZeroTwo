package org.kutsuki.zerotwo.document;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.portfolio.OrderLegCollection;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TdaPosition extends AbstractDocument {
    private static transient final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("MMddyy");
    private static transient final String SELL = "SELL";

    private String symbol;
    private int quantity;
    private LocalDate expiry;

    public TdaPosition() {
	// default constructor
    }

    public TdaPosition(String symbol, int quantity, LocalDate expiry) {
	this.quantity = quantity;
	this.symbol = symbol;
	this.expiry = expiry;
    }

    public TdaPosition(OrderLegCollection leg) {
	this.symbol = leg.getInstrument().getSymbol();

	if (StringUtils.startsWith(leg.getInstruction(), SELL)) {
	    this.quantity = -leg.getQuantity();
	} else {
	    this.quantity = leg.getQuantity();
	}

	int start = StringUtils.indexOf(symbol, '_') + 1;
	int end = start + 6;
	this.expiry = LocalDate.parse(StringUtils.substring(symbol, start, end), DTF);
    }

    public void updateQuantity(OrderLegCollection leg) {
	if (StringUtils.startsWith(leg.getInstruction(), SELL)) {
	    this.quantity -= leg.getQuantity();
	} else {
	    this.quantity += leg.getQuantity();
	}
    }

    public int getQuantity() {
	return quantity;
    }

    public String getSymbol() {
	return symbol;
    }

    public LocalDate getExpiry() {
	return expiry;
    }

    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }
}
