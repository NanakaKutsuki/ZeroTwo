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

public abstract class AbstractSpread {
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
	    .appendPattern("d MMM yy").toFormatter(Locale.ENGLISH);
    private static final String AM = "[AM]";
    private static final String STOP = "STP";
    private static final String WHEN = "WHEN";
    private static final String MARK_AT_OR_ABOVE = "MARK AT OR ABOVE ";
    private static final String MARK_AT_OR_BELOW = "MARK AT OR BELOW ";

    public abstract String getSpread();

    protected abstract OrderModel parseOrder(List<String> dataList, int tradeId, boolean am, boolean stop,
	    BigDecimal condition) throws Exception;

    public OrderModel parseOrder(String split, int tradeId) throws Exception {
	BigDecimal condition = BigDecimal.ZERO;
	if (StringUtils.containsIgnoreCase(split, WHEN)) {
	    if (StringUtils.containsIgnoreCase(split, MARK_AT_OR_ABOVE)) {
		String price = StringUtils.substringAfter(split, MARK_AT_OR_ABOVE);
		price = StringUtils.substringBefore(price, StringUtils.SPACE);
		condition = parsePrice(price, BigDecimal.ZERO);
	    } else if (StringUtils.containsIgnoreCase(split, MARK_AT_OR_BELOW)) {
		String price = StringUtils.substringAfter(split, MARK_AT_OR_BELOW);
		price = StringUtils.substringBefore(price, StringUtils.SPACE);
		condition = parsePrice(price, BigDecimal.ZERO).negate();
	    }
	}

	List<String> dataList = new ArrayList<String>(Arrays.asList(StringUtils.split(split, StringUtils.SPACE)));
	boolean am = dataList.remove(AM);
	boolean stop = dataList.remove(STOP);

	return parseOrder(dataList, tradeId, am, stop, condition);
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

    protected BigDecimal parsePrice(String val, BigDecimal condition) throws Exception {
	BigDecimal price = condition.abs();

	if (!StringUtils.contains(val, '.') && condition.compareTo(BigDecimal.ZERO) == 0) {
	    throw new Exception("Missing . from price: " + val);
	}

	try {
	    val = StringUtils.remove(val, '@');
	    price = new BigDecimal(StringUtils.substring(val, 0, StringUtils.indexOf(val, '.') + 3));
	} catch (NumberFormatException e) {
	    if (condition.compareTo(BigDecimal.ZERO) == 0) {
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
