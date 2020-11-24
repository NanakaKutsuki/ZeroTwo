package org.kutsuki.zerotwo.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractSpread {
    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder().parseCaseInsensitive()
	    .appendPattern("d MMM yy").toFormatter(Locale.ENGLISH);
    private static final String HUNDRED = "100";
    private static final String WEEKLYS = "(Weeklys)";

    public abstract String getSpread();

    public abstract OrderModel parseOrder(String[] split) throws Exception;

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

    protected BigDecimal parsePrice(String val) throws Exception {
	BigDecimal price = null;

	if (!StringUtils.contains(val, '@') || !StringUtils.contains(val, '.')) {
	    throw new Exception("Missing @ or . from price: " + val);
	}

	try {
	    price = new BigDecimal(StringUtils.substring(val, 1, StringUtils.indexOf(val, '.') + 3));
	} catch (NumberFormatException e) {
	    throw new Exception("Error parsing price: " + val, e);
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

    protected List<BigDecimal> parseSlashes(String slashes) throws Exception {
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
	return parseSlashes(strike).get(0);
    }

    protected String parseSymbol(String symbol) throws Exception {
	if (StringUtils.length(symbol) > 4 || StringUtils.length(symbol) == 0) {
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

    protected int startIndex(String[] split, int start) {
	int i = 0;
	if (StringUtils.equalsIgnoreCase(split[start], HUNDRED)) {
	    i++;
	}

	if (StringUtils.equalsIgnoreCase(split[start + 1], WEEKLYS)) {
	    i++;
	}

	return i;
    }
}
