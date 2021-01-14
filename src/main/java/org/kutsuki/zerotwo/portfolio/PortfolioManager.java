package org.kutsuki.zerotwo.portfolio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PortfolioManager {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mma");
    private static final String BOLD = "<b>";
    private static final String BOLD_CLOSE = "</b>";
    private static final String CONDITION = " Submit on Market Condition ";
    private static final String IMAGE = "image/";
    private static final String MARK_ABOVE = " MARK at or ABOVE ";
    private static final String MARK_BELOW = " MARK at or BELOW ";
    private static final String NEW = "NEW";
    private static final String PORTFOLIO = "[PORTFOLIO]";
    private static final String STP = "STP ";
    private static final String STOP = "STOP";
    private static final String STOP_EXPLAINATION = " - This trade is a STOP Limit order!";
    private static final String STOP_LIMIT = "Stop Limit (MARK)";
    private static final String ST_WEEKLY = "ST Weekly";
    private static final String SYMBOL_EXPLAINATION = "Symbol yymmdd C/P Strike";

    private static final String WORKING = "WORKING";
    private static final String WORKING_EXPLAINATION = " - This trade has not been filled!";
    private static final String UNOFFICIAL = "Unofficial:";

    private List<Position> deleteList;
    private List<Position> saveList;
    private Map<String, Position> portfolioMap;
    private OrderHelper helper;

    @Autowired
    private PositionRepository repository;

    @Autowired
    private EmailService service;

    @Value("${email.portfolio}")
    private String emailPortfolio;

    @PostConstruct
    public void postConstruct() {
	this.deleteList = new ArrayList<Position>();
	this.saveList = new ArrayList<Position>();
	this.helper = new OrderHelper();
	this.portfolioMap = new HashMap<String, Position>();
	reloadCache();
    }

    public void parseMessage(String message, String image) {
	parseMessage(message, image, false);
    }

    public void parseMessage(String message, String image, boolean test) {
	StringBuilder subject = new StringBuilder();
	StringBuilder body = new StringBuilder();
	body.append(message);
	int tradeId = -1;
	deleteList.clear();
	saveList.clear();

	// only # and unofficial have orders
	if (StringUtils.startsWith(message, Character.toString('#'))
		|| StringUtils.startsWithIgnoreCase(message, UNOFFICIAL)) {
	    boolean first = true;
	    boolean working = false;
	    boolean unofficial = false;

	    try {
		List<OrderModel> orderList = helper.createOrders(message);
		updatePortfolio(orderList, working || unofficial);

		if (orderList.size() > 0) {
		    tradeId = orderList.get(0).getPositionList().get(0).getTradeId();
		    subject.append('#');
		    subject.append(tradeId);
		    body.append(service.getLineBreak());
		    body.append(service.getLineBreak());
		} else {
		    subject.append(StringUtils.substringBefore(message, StringUtils.SPACE));
		}

		if (StringUtils.startsWithIgnoreCase(message, UNOFFICIAL)) {
		    subject.append(UNOFFICIAL);
		    unofficial = true;
		}

		String first25 = StringUtils.substring(message, 0, 25);
		if (StringUtils.containsIgnoreCase(first25, NEW)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(NEW);
		}

		if (StringUtils.containsIgnoreCase(first25, WORKING)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(WORKING);
		    body.append(BOLD);
		    body.append(WORKING);
		    body.append(StringUtils.SPACE);
		    body.append(WORKING);
		    body.append(StringUtils.SPACE);
		    body.append(WORKING);
		    body.append(StringUtils.SPACE);
		    body.append(WORKING_EXPLAINATION);
		    body.append(BOLD_CLOSE);
		    body.append(service.getLineBreak());
		    body.append(service.getLineBreak());
		    working = true;
		}

		if (StringUtils.containsIgnoreCase(message, STP)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(STOP);

		    // add stop
		    body.append(BOLD);
		    body.append(STOP);
		    body.append(StringUtils.SPACE);
		    body.append(STOP);
		    body.append(StringUtils.SPACE);
		    body.append(STOP);
		    body.append(StringUtils.SPACE);
		    body.append(STOP_EXPLAINATION);
		    body.append(BOLD_CLOSE);
		    body.append(service.getLineBreak());
		    body.append(service.getLineBreak());
		}

		for (OrderModel order : orderList) {
		    if (!first) {
			// only for seperate orders
			subject.append(StringUtils.SPACE);
			subject.append('&');
			body.append("--------------------------------------------------");
			body.append(service.getLineBreak());
		    }

		    subject.append(StringUtils.SPACE);
		    subject.append(order.getSymbol());
		    subject.append(StringUtils.SPACE);
		    subject.append(order.getSpread());

		    // append order
		    body.append(BOLD);
		    body.append(order.getSymbol());
		    body.append(StringUtils.SPACE);
		    body.append(order.getSpread());

		    if (order.getCondition() != 0) {
			if (order.isStop()) {
			    body.append(StringUtils.SPACE);
			    body.append(STOP);
			}

			body.append(CONDITION);
			body.append(order.getSymbol());

			if (order.getCondition() == 1) {
			    body.append(MARK_ABOVE);
			} else if (order.getCondition() == -1) {
			    body.append(MARK_BELOW);
			}

			body.append(order.getPrice());
		    } else {
			body.append(StringUtils.SPACE);
			body.append(order.getPrice());

			if (order.isStop()) {
			    body.append(StringUtils.SPACE);
			    body.append(STOP_LIMIT);
			}
		    }

		    body.append(BOLD_CLOSE);
		    body.append(service.getLineBreak());

		    // append order positions
		    for (Position position : order.getPositionList()) {
			body.append(position.getOrder());

			if ((StringUtils.equals(order.getSpread(), OptionType.CALL.toString())
				|| StringUtils.equals(order.getSpread(), OptionType.PUT.toString()))
				&& order.getCondition() == 0) {
			    body.append(StringUtils.SPACE);
			    body.append(order.getPriceBD());
			}

			body.append(service.getLineBreak());
		    }

		    first = false;
		}
	    } catch (Exception e) {
		e.printStackTrace();
		// service.emailException(message, e);
	    }

	    // append portfolio
	    body.append(service.getLineBreak());
	    body.append(service.getLineBreak());
	    body.append(getPortfolio(tradeId));

	    subject.append(StringUtils.SPACE);
	    subject.append(LocalTime.now().format(TIME_FORMATTER));
	} else

	{
	    subject.append(ST_WEEKLY);
	    subject.append(StringUtils.SPACE);
	    subject.append(LocalTime.now().format(TIME_FORMATTER));
	    body.append(service.getLineBreak());
	    body.append(service.getLineBreak());
	    body.append(getPortfolio(tradeId));
	}

	ByteArrayDataSource attachment = null;
	if (StringUtils.isNotBlank(image)) {
	    RestTemplate template = new RestTemplate();
	    byte[] response = template.getForObject(image, byte[].class);
	    String type = IMAGE + StringUtils.substringAfterLast(image, '.');
	    attachment = new ByteArrayDataSource(response, type);
	    attachment.setName(StringUtils.substringAfterLast(image, '/'));
	}

	if (!test) {
	    // email alert
	    service.email(emailPortfolio, subject.toString(), body.toString(), attachment);

	    // update portfolio repository
	    if (!deleteList.isEmpty()) {
		repository.deleteAll(deleteList);
	    }

	    if (!saveList.isEmpty()) {
		repository.saveAll(saveList);
	    }
	} else {
	    service.email(subject.toString(), body.toString(), attachment);
	}
    }

    private void updatePortfolio(List<OrderModel> orderList, boolean working) {
	LocalDate now = LocalDate.now();
	for (Position position : portfolioMap.values()) {
	    if (now.isAfter(position.getExpiry())) {
		deleteList.add(position);
	    }
	}

	for (Position position : deleteList) {
	    portfolioMap.remove(position.getFullSymbol());
	}

	for (OrderModel order : orderList) {
	    for (Position orderEntry : order.getPositionList()) {
		Position position = portfolioMap.get(orderEntry.getFullSymbol());
		if (position != null) {
		    int qty = position.getQuantity() + orderEntry.getQuantity();

		    if (!working) {
			position.setQuantity(qty);
			portfolioMap.put(position.getFullSymbol(), position);
			saveList.add(position);
		    }
		} else {
		    if (!working) {
			portfolioMap.put(orderEntry.getFullSymbol(), orderEntry);
			saveList.add(orderEntry);
		    }
		}
	    }
	}
    }

    public String getPortfolio(int tradeId) {
	StringBuilder sb = new StringBuilder();
	sb.append(BOLD);
	sb.append(PORTFOLIO);
	sb.append(BOLD_CLOSE);

	List<Position> portfolio = new ArrayList<Position>(portfolioMap.values());
	Collections.sort(portfolio);
	String symbol = StringUtils.EMPTY;
	for (Position position : portfolio) {
	    if (!symbol.equals(position.getSymbol())) {
		symbol = position.getSymbol();
		sb.append(service.getLineBreak());
	    }

	    sb.append(position.getStatement(tradeId));
	    sb.append(service.getLineBreak());
	}

	return sb.toString();
    }

    public Set<String> getSymbols() {
	return portfolioMap.keySet();
    }

    public void reloadCache() {
	portfolioMap.clear();
	for (Position position : repository.findAll()) {
	    portfolioMap.put(position.getFullSymbol(), position);
	}
    }

    public void sendPortfolio(int tradeId) {
	StringBuilder subject = new StringBuilder();
	subject.append(PORTFOLIO);
	subject.append(StringUtils.SPACE);
	subject.append(LocalTime.now().format(TIME_FORMATTER));
	service.email(subject.toString(), getPortfolio(tradeId));
    }

    public String updateTradeId(String symbol, String id) {
	String result = SYMBOL_EXPLAINATION;

	if (StringUtils.isNotBlank(symbol)) {
	    Position position = portfolioMap.get(symbol);
	    if (position != null) {
		try {
		    int tradeId = Integer.parseInt(id);
		    position.setTradeId(tradeId);

		    portfolioMap.put(position.getFullSymbol(), position);
		    repository.save(position);
		} catch (NumberFormatException e) {
		    service.emailException("Error updating trade id: " + symbol + StringUtils.SPACE + id, e);
		}
	    }

	    result = position.getFullSymbol();
	}

	return result;
    }

    public String updateQty(String symbol, String qty) {
	String result = SYMBOL_EXPLAINATION;

	if (StringUtils.isNotBlank(symbol)) {
	    Position position = portfolioMap.get(symbol);
	    if (position != null) {
		try {
		    int quantity = Integer.parseInt(qty);
		    position.setQuantity(quantity);

		    if (quantity == 0) {
			portfolioMap.remove(position.getFullSymbol());
			repository.delete(position);
		    } else {
			portfolioMap.put(position.getFullSymbol(), position);
			repository.save(position);
		    }
		} catch (NumberFormatException e) {
		    service.emailException("Error updating qty: " + symbol + StringUtils.SPACE + qty, e);
		}
	    }

	    result = position.getFullSymbol();
	}

	return result;
    }
}