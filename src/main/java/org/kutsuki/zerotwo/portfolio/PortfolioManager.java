package org.kutsuki.zerotwo.portfolio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.portfolio.spread.AbstractSpread;
import org.kutsuki.zerotwo.portfolio.spread.ButterflySpread;
import org.kutsuki.zerotwo.portfolio.spread.CondorSpread;
import org.kutsuki.zerotwo.portfolio.spread.DiagonalSpread;
import org.kutsuki.zerotwo.portfolio.spread.IronCondorSpread;
import org.kutsuki.zerotwo.portfolio.spread.RatioSpread;
import org.kutsuki.zerotwo.portfolio.spread.SingleSpread;
import org.kutsuki.zerotwo.portfolio.spread.UnbalancedButterflySpread;
import org.kutsuki.zerotwo.portfolio.spread.VerticalSpread;
import org.kutsuki.zerotwo.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PortfolioManager {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mma");
    private static final String AM = "[AM] ";
    private static final String BOLD = "<b>";
    private static final String BOLD_CLOSE = "</b>";
    private static final String BOT = "BOT ";
    private static final String BUY = "BUY ";
    private static final String IMAGE = "image/";
    private static final String NEW = "NEW";
    private static final String PORTFOLIO = "[PORTFOLIO]";
    private static final String SELL = "SELL ";
    private static final String SOLD = "SOLD ";
    private static final String STOP = "STP ";
    private static final String STOP_EXPLAINATION = " - This trade is a STOP order!";
    private static final String ST_WEEKLY = "ST Weekly";
    private static final String SYMBOL_EXPLAINATION = "Symbol yymmdd C/P Strike";
    private static final String WORKING = "WORKING";
    private static final String WORKING_EXPLAINATION = " - This trade has not been filled!";
    private static final String UNOFFICIAL = "Unofficial:";

    private List<Position> deleteList;
    private List<Position> saveList;
    private List<AbstractSpread> spreadList;
    private Map<String, Position> portfolioMap;

    @Autowired
    private PortfolioRepository repository;

    @Autowired
    private EmailService service;

    @Value("${email.portfolio}")
    private String emailPortfolio;

    @PostConstruct
    public void postConstruct() {
	this.deleteList = new ArrayList<Position>();
	this.saveList = new ArrayList<Position>();
	this.portfolioMap = new HashMap<String, Position>();

	// order matters
	this.spreadList = new ArrayList<AbstractSpread>();
	this.spreadList.add(new IronCondorSpread());
	this.spreadList.add(new UnbalancedButterflySpread());
	this.spreadList.add(new DiagonalSpread());
	this.spreadList.add(new RatioSpread());
	this.spreadList.add(new VerticalSpread());
	this.spreadList.add(new ButterflySpread());
	this.spreadList.add(new CondorSpread());
	this.spreadList.add(new SingleSpread());

	reloadCache();
    }

    public void parseMessage(String message, String image) {
	parseMessage(message, image, false);
    }

    public void parseMessage(String message, String image, boolean test) {
	try {
	    StringBuilder subject = new StringBuilder();
	    StringBuilder body = new StringBuilder();
	    body.append(message);
	    deleteList.clear();
	    saveList.clear();

	    // only # and unofficial have orders
	    if (StringUtils.startsWith(body, Character.toString('#'))
		    || StringUtils.startsWithIgnoreCase(body, UNOFFICIAL)) {
		boolean stop = false;
		boolean working = false;
		boolean unofficial = false;
		body.append(service.getLineBreak());
		body.append(service.getLineBreak());

		int tradeId = -1;
		if (StringUtils.startsWith(body, Character.toString('#'))) {
		    try {
			String tid = StringUtils.substringBetween(message, Character.toString('#'), StringUtils.SPACE);
			tradeId = Integer.parseInt(tid);
			subject.append('#');
			subject.append(tradeId);
		    } catch (NumberFormatException e) {
			service.emailException("Error parsing Trade Id: " + body, e);
		    }
		}

		if (StringUtils.startsWithIgnoreCase(message, UNOFFICIAL)) {
		    subject.append(UNOFFICIAL);
		    unofficial = true;
		}

		if (StringUtils.containsIgnoreCase(message, NEW)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(NEW);
		}

		if (StringUtils.containsIgnoreCase(message, WORKING)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(WORKING);
		    working = true;
		}

		if (StringUtils.containsIgnoreCase(message, STOP)) {
		    subject.append(StringUtils.SPACE);
		    subject.append(STOP);
		    message = StringUtils.remove(message, STOP);
		    stop = true;
		}

		boolean first = true;
		List<OrderModel> orderList = createOrders(message, tradeId);
		String portfolio = getPortfolio(orderList, tradeId, working || unofficial);
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

		    // add working
		    if (working) {
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
		    }

		    // add stop
		    if (stop) {
			body.append(BOLD);
			body.append(STOP);
			body.append(STOP);
			body.append(STOP);
			body.append(STOP_EXPLAINATION);
			body.append(BOLD_CLOSE);
			body.append(service.getLineBreak());
			body.append(service.getLineBreak());
		    }

		    // append order
		    body.append(BOLD);
		    body.append(order.getSymbol());
		    body.append(StringUtils.SPACE);
		    body.append(order.getSpread());
		    body.append(StringUtils.SPACE);
		    body.append(order.getPrice());
		    body.append(BOLD_CLOSE);
		    body.append(service.getLineBreak());

		    // append order positions
		    for (Position position : order.getPositionList()) {
			body.append(position.getOrder());

			if (StringUtils.equals(order.getSpread(), OptionType.CALL.toString())
				|| StringUtils.equals(order.getSpread(), OptionType.PUT.toString())) {
			    body.append(StringUtils.SPACE);
			    body.append(order.getPriceBD());
			}

			body.append(service.getLineBreak());
		    }

		    first = false;
		}

		// append portfolio
		body.append(service.getLineBreak());
		body.append(service.getLineBreak());
		body.append(portfolio);

		subject.append(StringUtils.SPACE);
		subject.append(LocalTime.now().format(TIME_FORMATTER));
	    } else {
		subject.append(ST_WEEKLY);
		subject.append(StringUtils.SPACE);
		subject.append(LocalTime.now().format(TIME_FORMATTER));
		body.append(service.getLineBreak());
		body.append(service.getLineBreak());
		body.append(getPortfolio(Collections.emptyList(), -1, false));
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
	} catch (Exception e) {
	    service.emailException(message, e);
	}
    }

    private List<OrderModel> createOrders(String body, int tradeId) {
	List<OrderModel> orderList = new ArrayList<OrderModel>();

	if (StringUtils.contains(body, OptionType.CALL.toString())
		|| StringUtils.contains(body, OptionType.PUT.toString())) {
	    try {
		boolean am = false;
		if (StringUtils.contains(body, AM)) {
		    body = StringUtils.remove(body, AM);
		    am = true;
		}

		for (String split : StringUtils.split(body, '&')) {
		    if (StringUtils.contains(split, BOT)) {
			split = StringUtils.substringAfter(split, BOT);
		    } else if (StringUtils.contains(split, BUY)) {
			split = StringUtils.substringAfter(split, BUY);
		    } else if (StringUtils.contains(split, SELL)) {
			split = StringUtils.substringAfter(split, SELL);
		    } else if (StringUtils.contains(split, SOLD)) {
			split = StringUtils.substringAfter(split, SOLD);
		    }

		    String[] split2 = StringUtils.split(split, StringUtils.SPACE);
		    boolean found = false;
		    Iterator<AbstractSpread> itr = spreadList.iterator();
		    while (!found && itr.hasNext()) {
			AbstractSpread spread = itr.next();

			if (!itr.hasNext() || StringUtils.contains(split, spread.getSpread())) {
			    orderList.add(spread.parseOrder(split2, tradeId, am));
			    found = true;
			}
		    }
		}
	    } catch (Exception e) {
		service.emailException("Error parsing text", e);
	    }
	}

	return orderList;
    }

    private String getPortfolio(List<OrderModel> orderList, int tradeId, boolean working) {
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

		    if (orderEntry.getQuantity() > 0) {
			orderEntry.setSide(StringUtils.trim(BUY));
		    } else {
			orderEntry.setSide(StringUtils.trim(SELL));
		    }

		    if (!working) {
			position.setQuantity(qty);
			portfolioMap.put(position.getFullSymbol(), position);
			saveList.add(position);
		    }
		} else {
		    if (orderEntry.getQuantity() > 0) {
			orderEntry.setSide(StringUtils.trim(BUY));
		    } else {
			orderEntry.setSide(StringUtils.trim(SELL));
		    }

		    if (!working) {
			portfolioMap.put(orderEntry.getFullSymbol(), orderEntry);
			saveList.add(orderEntry);
		    }
		}
	    }
	}

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
	service.email(subject.toString(), getPortfolio(Collections.emptyList(), tradeId, false));
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