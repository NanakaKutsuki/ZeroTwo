package org.kutsuki.zerotwo.portfolio.spread;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.portfolio.OptionType;
import org.kutsuki.zerotwo.portfolio.OrderModel;

// TODO check netzero, add stop, add stop limt, add market
public abstract class AbstractSpread {
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
	    .appendPattern("d MMM yy").toFormatter(Locale.ENGLISH);
    private static final String AM = "[AM]";
    private static final String CREDIT = "cr";
    private static final String DEBIT = "db";
    private static final String GTC = "GTC";
    private static final String LIMIT = "LMT";
    private static final String MARKET = "MARKET";
    private static final String NET_DEBIT = "NET_DEBIT";
    private static final String NET_CREDIT = "NET_CREDIT";
    private static final String STOP = "STP";
    private static final String WHEN = "WHEN";
    private static final String MARK_AT_OR_ABOVE = "MARK AT OR ABOVE ";
    private static final String MARK_AT_OR_BELOW = "MARK AT OR BELOW ";

    private BigDecimal conditionPrice;
    private boolean am;
    private boolean gtc;
    private boolean stop;
    private boolean working;
    private int tradeId;
    private List<String> dataList;
    private int condition;

    public abstract String getComplex();

    public abstract String getSpread();

    protected abstract OrderModel parseOrder() throws Exception;

    public OrderModel parseOrder(String split, int tradeId) throws Exception {
	this.condition = 0;
	this.tradeId = tradeId;

	if (StringUtils.containsIgnoreCase(split, WHEN)) {
	    if (StringUtils.containsIgnoreCase(split, MARK_AT_OR_ABOVE)) {
		String price = StringUtils.substringAfter(split, MARK_AT_OR_ABOVE);
		price = StringUtils.substringBefore(price, StringUtils.SPACE);
		this.conditionPrice = parsePrice(price);
		this.condition = 1;
	    } else if (StringUtils.containsIgnoreCase(split, MARK_AT_OR_BELOW)) {
		String price = StringUtils.substringAfter(split, MARK_AT_OR_BELOW);
		price = StringUtils.substringBefore(price, StringUtils.SPACE);
		this.conditionPrice = parsePrice(price);
		this.condition = -1;
	    }
	}

	this.dataList = new ArrayList<String>(Arrays.asList(StringUtils.split(split, StringUtils.SPACE)));
	this.am = getDataList().remove(AM);
	this.gtc = getDataList().remove(GTC);
	this.stop = getDataList().remove(STOP);
	this.working = getDataList().remove(LIMIT) || this.stop || condition != 0;

	return parseOrder();
    }

    protected OrderModel createOrder(String orderType, BigDecimal price) {
	return createOrder(getSpread(), orderType, price);
    }

    protected OrderModel createOrder(String spread, String orderType, BigDecimal price) {
	return new OrderModel(spread, getComplex(), orderType, price, gtc, stop, working, condition);
    }

    protected boolean isAM() {
	return am;
    }

    protected List<String> getDataList() {
	return dataList;
    }

    protected int getTradeId() {
	return tradeId;
    }

    protected LocalDate parseExpiry(String day, String month, String year) throws Exception {
	LocalDate exp = null;

	try {
	    String date = day + StringUtils.SPACE + month + StringUtils.SPACE + year;
	    exp = LocalDate.parse(date, FORMATTER);
	} catch (DateTimeParseException e) {
	    throw new Exception("Error parsing Date: " + day + StringUtils.SPACE + month + StringUtils.SPACE + year, e);
	}

	return exp;
    }

    protected String parseOrderType(String val, int quantity) throws Exception {
	String orderType = MARKET;

	if (condition == 0) {
	    if (!StringUtils.contains(val, '.')) {
		throw new Exception("Missing . from price: " + val);
	    }

	    String price = StringUtils.substring(val, StringUtils.indexOf(val, '.') + 3);
	    if (StringUtils.equalsIgnoreCase(price, CREDIT)) {
		orderType = NET_CREDIT;
	    } else if (StringUtils.equalsIgnoreCase(price, DEBIT)) {
		orderType = NET_DEBIT;
	    } else {
		if (quantity > 0) {
		    orderType = NET_DEBIT;
		} else if (quantity < 0) {
		    orderType = NET_CREDIT;
		}
	    }
	}

	return orderType;
    }

    protected BigDecimal parsePrice(String val) throws Exception {
	BigDecimal price = conditionPrice;

	if (condition == 0) {
	    if (!StringUtils.contains(val, '.')) {
		throw new Exception("Missing . from price: " + val);
	    }

	    try {
		val = StringUtils.remove(val, '@');
		price = new BigDecimal(StringUtils.substring(val, 0, StringUtils.indexOf(val, '.') + 3));
	    } catch (NumberFormatException e) {
		throw new Exception("Error parsing price: " + val + " condition: " + condition, e);
	    }
	}

	return price;
    }

    protected int parseQuantity(String val) throws Exception {
	int qty = 0;

	try {
	    qty = Integer.parseInt(val);
	} catch (NumberFormatException e) {
	    throw new Exception("Error parsing quantity: " + val, e);
	}

	return qty;
    }

    protected List<String> parseSlashes(String slashes) {
	return Arrays.asList(StringUtils.split(slashes, '/'));
    }

    protected List<BigDecimal> parseSlashesBD(String slashes) throws Exception {
	List<BigDecimal> slashList = new ArrayList<BigDecimal>();

	for (String slash : StringUtils.split(slashes, '/')) {
	    try {
		slashList.add(new BigDecimal(slash));
	    } catch (NumberFormatException e) {
		throw new Exception("Error parsing slashes: " + slashes, e);
	    }
	}

	return slashList;
    }

    protected BigDecimal parseStrike(String strike) throws Exception {
	return parseSlashesBD(strike).get(0);
    }

    protected String parseSymbol(String symbol) throws Exception {
	if (StringUtils.length(symbol) == 0) {
	    throw new Exception("Error parsing symbol: " + symbol);
	}

	return symbol;
    }

    protected OptionType parseType(String type) throws Exception {
	return parseTypes(type).get(0);
    }

    protected List<OptionType> parseTypes(String slashes) throws Exception {
	List<OptionType> slashList = new ArrayList<OptionType>();
	for (String split : StringUtils.split(slashes, '/')) {
	    try {
		slashList.add(OptionType.valueOf(split));
	    } catch (IllegalArgumentException e) {
		throw new Exception("Error parsing type: " + split);
	    }
	}

	return slashList;
    }
}
