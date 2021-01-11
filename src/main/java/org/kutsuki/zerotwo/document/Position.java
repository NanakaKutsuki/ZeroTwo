package org.kutsuki.zerotwo.document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Position extends AbstractDocument implements Comparable<Position> {
    private static final DateTimeFormatter SYMBOL_DTF = DateTimeFormatter.ofPattern("yyMMdd");
    private static final DateTimeFormatter ORDER_DTF = DateTimeFormatter.ofPattern("d MMM yy");
    private static final String MARK = "<mark><b>";
    private static final String MARK_CLOSE = "</b></mark>";

    private int tradeId;
    private int quantity;
    private String symbol;
    private LocalDate expiry;
    private boolean am;
    private BigDecimal strike;
    private OptionType type;
    private String fullSymbol;

    @Transient
    @JsonIgnore
    private transient String side;

    public Position(int tradeId, int quantity, String symbol, LocalDate expiry, boolean am, BigDecimal strike,
	    OptionType type) {
	this.tradeId = tradeId;
	this.quantity = quantity;
	this.symbol = symbol;
	this.expiry = expiry;
	this.am = am;
	this.strike = strike;
	this.type = type;

	StringBuilder sb = new StringBuilder();
	sb.append(getSymbol());
	sb.append(SYMBOL_DTF.format(getExpiry()));
	sb.append(getType().toString().charAt(0));
	sb.append(getStrike());
	this.fullSymbol = sb.toString();
    }

    @Override
    public int compareTo(Position rhs) {
	int result = getSymbol().compareTo(rhs.getSymbol());

	if (result == 0) {
	    result = getExpiry().compareTo(rhs.getExpiry());

	    if (result == 0) {
		result = Boolean.compare(isAM(), rhs.isAM());

		if (result == 0) {
		    result = getStrike().compareTo(rhs.getStrike());

		    if (result == 0) {
			result = getType().compareTo(rhs.getType());
		    }
		}
	    }
	}

	return result;
    }

    @JsonIgnore
    public String getOrder() {
	StringBuilder sb = new StringBuilder();
	sb.append(getSide());
	sb.append(StringUtils.SPACE);

	if (getQuantity() > 0) {
	    sb.append('+');
	}

	sb.append(getQuantity());
	sb.append(StringUtils.SPACE);
	sb.append(getSymbol());
	sb.append(StringUtils.SPACE);
	sb.append('[');
	sb.append(ORDER_DTF.format(getExpiry()));
	if (isAM()) {
	    sb.append(StringUtils.SPACE);
	    sb.append('A');
	    sb.append('M');
	}
	sb.append(']');
	sb.append(StringUtils.SPACE);
	sb.append(getStrike());
	sb.append(StringUtils.SPACE);
	sb.append(getType());

	return sb.toString();
    }

    @JsonIgnore
    public String getStatement(int tradeId) {
	StringBuilder sb = new StringBuilder();
	if (tradeId == getTradeId()) {
	    sb.append(MARK);
	}

	sb.append(getSymbol());
	sb.append(StringUtils.SPACE);
	sb.append('#');
	sb.append(getTradeId());
	sb.append(StringUtils.SPACE);
	sb.append('[');
	sb.append(ORDER_DTF.format(getExpiry()));
	if (isAM()) {
	    sb.append(StringUtils.SPACE);
	    sb.append('A');
	    sb.append('M');
	}
	sb.append(']');
	sb.append(StringUtils.SPACE);
	sb.append(getStrike());
	sb.append(StringUtils.SPACE);
	sb.append(getType());
	sb.append(StringUtils.SPACE);

	if (getQuantity() > 0) {
	    sb.append('+');
	}
	sb.append(getQuantity());

	if (tradeId == getTradeId()) {
	    sb.append(MARK_CLOSE);
	}

	return sb.toString();
    }

    public String getFullSymbol() {
	return fullSymbol;
    }

    public int getQuantity() {
	return quantity;
    }

    public String getSide() {
	return side;
    }

    public String getSymbol() {
	return symbol;
    }

    public LocalDate getExpiry() {
	return expiry;
    }

    public boolean isAM() {
	return am;
    }

    public BigDecimal getStrike() {
	return strike;
    }

    public int getTradeId() {
	return tradeId;
    }

    public OptionType getType() {
	return type;
    }

    public void setQuantity(int quantity) {
	this.quantity = quantity;
    }

    public void setSide(String side) {
	this.side = side;
    }

    public void setTradeId(int tradeId) {
	this.tradeId = tradeId;
    }
}
