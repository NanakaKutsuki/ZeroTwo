package org.kutsuki.zerotwo.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.rest.openings.AbstractSheets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.services.sheets.v4.model.ValueRange;

@Component
public class OrderSheetHelper extends AbstractSheets {
    private static final String BACKRATIO = "BACKRATIO";
    private static final String BOUGHT = "Bought";
    private static final String IRON_CONDOR = "IRON CONDOR";
    private static final String NET_DEBIT = "NET_DEBIT";
    private static final String NET_CREDIT = "NET_CREDIT";
    private static final String RANGE = "Trading!A2:I";
    private static final String SINGLE = "SINGLE";
    private static final String SOLD = "Sold";
    private static final String SUM = "=SUM(J";
    private static final String WRITE_RANGE = "Trading!A2:K";

    @Value("${tda.sheetId}")
    private String sheetId;

    private List<Object> dashList;
    private List<Object> emptyList;

    public OrderSheetHelper() {
	this.dashList = new ArrayList<Object>();
	this.dashList.add(Character.toString('-'));

	this.emptyList = new ArrayList<Object>();
	for (int j = 0; j < 10; j++) {
	    this.emptyList.add(StringUtils.EMPTY);
	}
    }

    public void addOrder(OrderModel order, String finalPrice) {
	Position position = order.getPositionList().get(0);
	String tradeId = Integer.toString(position.getTradeId());

	List<List<Object>> rowList = readSheet(sheetId, RANGE);
	boolean found = false;
	boolean startFound = false;
	int i = 0;
	int insertIndex = 0;
	while (!found && i < rowList.size()) {
	    char first = String.valueOf(rowList.get(i).get(0)).charAt(0);
	    if (first != '-') {
		String value = String.valueOf(rowList.get(i).get(0));

		if (!startFound && StringUtils.equals(value, tradeId)) {
		    startFound = true;
		}
	    } else if (startFound && insertIndex == 0 && first == '-') {
		insertIndex = i;
		found = true;
	    }

	    i++;
	}

	List<Object> dataList = new ArrayList<Object>();
	BigDecimal price = getPrice(order, order.getPrice());
	BigDecimal fillPrice = getPrice(order, new BigDecimal(finalPrice));

	dataList.add(tradeId);
	dataList.add(LocalDate.now().toString());
	dataList.add(getAction(order));
	dataList.add(Integer.toString(Math.abs(position.getQuantity())));
	dataList.add(position.getSymbol());
	dataList.add(position.getExpiry().toString());
	dataList.add(getSpread(order));
	dataList.add(fillPrice.subtract(price));
	dataList.add(fillPrice);

	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	if (!found) {
	    writeRowList.add(dataList);
	    writeRowList.add(dashList);
	    writeRowList.addAll(rowList);
	} else {
	    writeRowList.addAll(rowList);
	    writeRowList.add(insertIndex, dataList);
	}

	found = false;
	for (i = 0; i < writeRowList.size(); i++) {
	    List<Object> newDataList = writeRowList.get(i);
	    char value = String.valueOf(newDataList.get(0)).charAt(0);

	    if (value != '-') {
		newDataList.add(getCost(i + 2));

		if (!found) {
		    int j = i + 1;
		    while (!found && j < writeRowList.size()) {
			value = String.valueOf(writeRowList.get(j).get(0)).charAt(0);
			if (value == '-') {
			    newDataList.add(getSum(i + 2, j + 1));
			    found = true;
			}

			j++;
		    }

		    if (j == writeRowList.size()) {
			newDataList.add(getSum(i + 2, j + 1));
			found = true;
		    }
		} else {
		    newDataList.add(StringUtils.EMPTY);
		}
	    } else {
		newDataList.addAll(emptyList);
		found = false;
	    }
	}

	// write sheet
	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(sheetId, WRITE_RANGE, body);

	sendEmail(dataList);
    }

    private void sendEmail(List<Object> dataList) {
	StringBuilder sb = new StringBuilder();
	sb.append('#');
	sb.append(dataList.get(0));
	sb.append(StringUtils.SPACE);
	sb.append(dataList.get(5));
	sb.append(StringUtils.SPACE);
	sb.append('@');
	sb.append(dataList.get(8));
	getEmailService().email(sb.toString(), dataList.get(3) + " Filled");
    }

    private String getAction(OrderModel order) {
	String action = BOUGHT;

	if (StringUtils.equals(order.getSpread(), BACKRATIO) && order.getPositionList().get(1).getQuantity() < 0) {
	    action = SOLD;
	} else if (order.getPositionList().get(0).getQuantity() < 0) {
	    action = SOLD;
	}

	return action;
    }

    private String getSpread(OrderModel order) {
	StringBuilder sb = new StringBuilder();

	for (int i = 0; i < order.getPositionList().size(); i++) {
	    if (i > 0) {
		sb.append('/');
	    }

	    sb.append(order.getPositionList().get(i).getStrike());
	}

	if (!StringUtils.equals(order.getSpread(), IRON_CONDOR)) {
	    String type = order.getPositionList().get(0).getType().toString();
	    sb.append(StringUtils.SPACE);
	    sb.append(StringUtils.capitalize(StringUtils.lowerCase(type)));
	}

	if (!StringUtils.equals(order.getSpread(), SINGLE)) {
	    sb.append(StringUtils.SPACE);
	    sb.append(StringUtils.capitalize(StringUtils.lowerCase(order.getSpread())));
	}

	return sb.toString();
    }

    private BigDecimal getPrice(OrderModel order, BigDecimal price) {
	BigDecimal result = BigDecimal.ZERO;

	if (StringUtils.equals(order.getSpread(), SINGLE)) {
	    if (order.getPositionList().get(0).getQuantity() < 0) {
		result = price.abs().negate();
	    } else {
		result = price.abs();
	    }
	} else if (StringUtils.equals(order.getOrderType(), NET_CREDIT)) {
	    result = price.abs().negate();
	} else if (StringUtils.equals(order.getOrderType(), NET_DEBIT)) {
	    result = price.abs();
	}

	return result;
    }

    private String getCost(int row) {
	StringBuilder sb = new StringBuilder();
	sb.append('=');
	sb.append('D');
	sb.append(row);
	sb.append('*');
	sb.append('-');
	sb.append('I');
	sb.append(row);
	sb.append('*');
	sb.append(100);
	return sb.toString();
    }

    private String getSum(int start, int end) {
	StringBuilder sb = new StringBuilder();
	sb.append(SUM);
	sb.append(start);
	sb.append(':');
	sb.append('J');
	sb.append(end);
	sb.append(')');
	return sb.toString();
    }
}
