package org.kutsuki.zerotwo.portfolio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Position;
import org.kutsuki.zerotwo.document.Skip;
import org.kutsuki.zerotwo.document.TdaPosition;
import org.kutsuki.zerotwo.repository.SkipRepository;
import org.kutsuki.zerotwo.repository.TdaPositionRepository;
import org.kutsuki.zerotwo.rest.post.PostGetOrder;
import org.kutsuki.zerotwo.rest.post.PostPlaceOrder;
import org.kutsuki.zerotwo.rest.post.PostToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderManager {
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String STATUS_QUEUED = "?status=QUEUED";

    @Autowired
    private SkipRepository skipRepository;

    @Autowired
    private TdaPositionRepository repository;

    @Autowired
    private EmailService service;

    @Autowired
    private OrderSheetHelper sheet;

    @Value("${tda.refreshToken}")
    private String refreshToken;

    @Value("${tda.clientId}")
    private String clientId;

    @Value("${tda.orderLink}")
    private String orderLink;

    @Value("${tda.tokenLink}")
    private String tokenLink;

    private List<Integer> skipList;
    private List<String> workingList;
    private LocalDateTime expires;
    private Map<String, TdaPosition> positionMap;
    private String token;

    // TODO remove
    public void setToken(String token) {
	this.token = token;
	this.expires = LocalDateTime.now().plusSeconds(1800);
    }

    @PostConstruct
    public void postConstruct() {
	this.expires = LocalDateTime.MIN;
	this.positionMap = new ConcurrentHashMap<String, TdaPosition>();
	this.skipList = new ArrayList<Integer>();
	this.workingList = new ArrayList<String>();

	reloadCache();
    }

    public void addSkip(int tradeId) {
	Skip skip = new Skip();
	skip.setTradeId(tradeId);
	skip = skipRepository.save(skip);
	skipList.add(tradeId);
    }

    public void placeStopOrder(OrderModel order) {
	int tradeId = 0;
	if (!order.getPositionList().isEmpty()) {
	    tradeId = order.getPositionList().get(0).getTradeId();
	}

	if (!skipList.contains(tradeId)) {
	    refreshToken();

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set(AUTHORIZATION, BEARER + token);

	    setOpens(order);
	    PostPlaceOrder post = new PostPlaceOrder(order);
	    HttpEntity<String> entity = new HttpEntity<String>(post.toString(), headers);
	    System.out.println(post.toString());

	    if (!workingList.contains(post.getKey())) {
		try {
		    RestTemplate restTemplate = new RestTemplate();
		    ResponseEntity<String> response = restTemplate.exchange(orderLink, HttpMethod.POST, entity,
			    String.class);
		    if (response.getStatusCodeValue() == 201) {
			System.out.println(response.getStatusCodeValue());
//			if (order.isWorking()) {
//			    workingList.add(post.getKey());
//			} else {
//			    queuedOrder(post, order);
//			}
		    } else {
			service.email("Error placing order: " + response.getStatusCodeValue(), response.getBody());
		    }
		} catch (RestClientException e) {
		    e.printStackTrace();
		    // service.emailException("Error placing order: " + order.getSpread(), e);
		}
	    } else {
		workingList.remove(post.getKey());
	    }
	}
    }

    public void placeOrder(OrderModel order) {
	int tradeId = 0;
	if (!order.getPositionList().isEmpty()) {
	    tradeId = order.getPositionList().get(0).getTradeId();
	}

	if (order.isStop()) {
	    service.email(order.getSymbol() + " Stop Order Unsupported!", order.getSpread());
	} else if (!skipList.contains(tradeId)) {
	    refreshToken();

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.set(AUTHORIZATION, BEARER + token);

	    setOpens(order);
	    PostPlaceOrder post = new PostPlaceOrder(order);
	    HttpEntity<String> entity = new HttpEntity<String>(post.toString(), headers);

	    if (!workingList.contains(post.getKey())) {
		try {
		    RestTemplate restTemplate = new RestTemplate();
		    ResponseEntity<String> response = restTemplate.exchange(orderLink, HttpMethod.POST, entity,
			    String.class);
		    if (response.getStatusCodeValue() == 201) {
			if (order.isWorking()) {
			    workingList.add(post.getKey());
			} else {
			    queuedOrder(post, order);
			}
		    } else {
			service.email("Error placing order: " + response.getStatusCodeValue(), response.getBody());
		    }
		} catch (RestClientException e) {
		    service.emailException("Error placing order: " + order.getSpread(), e);
		}
	    } else {
		workingList.remove(post.getKey());
	    }
	}
    }

    public void refreshToken() {
	if (StringUtils.isBlank(token) || LocalDateTime.now().isAfter(expires)) {
	    RestTemplate restTemplate = new RestTemplate();

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
	    TokenHelper helper = new TokenHelper();
	    HttpEntity<String> request = new HttpEntity<String>(helper.getToken(refreshToken, clientId), headers);

	    PostToken post = restTemplate.postForObject(tokenLink, request, PostToken.class);
	    this.token = post.getAccess_token();
	    this.expires = LocalDateTime.now().plusSeconds(post.getExpires_in());

	    // TODO remove
	    System.out.println("String token = \"" + token + "\";");
	    System.out.println("manager.setToken(token);");
	}
    }

    public void reloadCache() {
	this.skipList.clear();
	this.workingList.clear();
	for (Skip skip : skipRepository.findAll()) {
	    this.skipList.add(skip.getTradeId());
	}

	this.positionMap.clear();
	List<TdaPosition> removeList = new ArrayList<TdaPosition>();
	for (TdaPosition position : repository.findAll()) {
	    if (position.getExpiry().isAfter(LocalDate.now().minusDays(1))) {
		this.positionMap.put(position.getSymbol(), position);
	    } else {
		removeList.add(position);
	    }
	}

	repository.deleteAll(removeList);
    }

    public void removeSkip(int tradeId) {
	Skip skip = skipRepository.findByTradeId(tradeId);
	skipRepository.delete(skip);
	skipList.remove(Integer.valueOf(tradeId));
    }

    public void replaceOrder(PostPlaceOrder order) {
	refreshToken();

	HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.APPLICATION_JSON);
	headers.set(AUTHORIZATION, BEARER + token);

	try {
	    HttpEntity<String> entity = new HttpEntity<String>(order.toString(), headers);
	    RestTemplate restTemplate = new RestTemplate();

	    String link = orderLink + Character.toString('/') + order.getOrderId();
	    ResponseEntity<String> response = restTemplate.exchange(link, HttpMethod.PUT, entity, String.class);

	    if (response.getStatusCodeValue() != 201) {
		service.email("Error replacing order: " + response.getStatusCodeValue(), response.getBody());
	    }
	} catch (RestClientException e) {
	    service.emailException("Error replacing order: " + order.getComplexOrderStrategyType(), e);
	}
    }

    private void setOpens(OrderModel order) {
	for (Position position : order.getPositionList()) {
	    TdaPosition tda = positionMap.get(position.getFullSymbol());
	    if (tda != null) {
		if ((tda.getQuantity() > 0 && position.getQuantity() > 0)
			|| (tda.getQuantity() < 0 && position.getQuantity() < 0)) {
		    position.setOpen(true);
		} else {
		    position.setOpen(false);
		}
	    } else {
		position.setOpen(true);
	    }
	}
    }

    private void queuedOrder(PostPlaceOrder placeOrder, OrderModel order) {
	Thread thread = new Thread() {
	    public void run() {
		boolean queued = true;
		int i = 0;

		while (queued && i < 10) {
		    // TODO replace order?

		    HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_JSON);
		    headers.set(AUTHORIZATION, BEARER + token);

		    try {
			HttpEntity<String> entity = new HttpEntity<String>(headers);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<PostGetOrder[]> response = restTemplate.exchange(orderLink + STATUS_QUEUED,
				HttpMethod.GET, entity, PostGetOrder[].class);

			boolean found = false;
			for (PostGetOrder order : response.getBody()) {
			    StringBuilder key = new StringBuilder();
			    for (OrderLegCollection leg : order.getOrderLegCollection()) {
				key.append(leg.getInstrument().getSymbol());
			    }

			    if (StringUtils.equals(placeOrder.getKey(), key.toString())) {
				if (placeOrder.getOrderId() == 0) {
				    placeOrder.setOrderId(order.getOrderId());
				}

				found = true;
				sleep(2000);
			    }
			}

			if (found) {
			    queued = false;
			}
		    } catch (RestClientException | InterruptedException e) {
			service.emailException("Error getting Queued Orders!", e);
		    }

		    i++;
		}

		if (!queued) {
		    for (OrderLegCollection leg : placeOrder.getOrderLegCollection()) {
			TdaPosition position = positionMap.get(leg.getInstrument().getSymbol());
			if (position != null) {
			    position.updateQuantity(leg);
			} else {
			    position = new TdaPosition(leg);
			}

			position = repository.save(position);
			positionMap.put(position.getSymbol(), position);
			sheet.addOrder(order, placeOrder.getPrice());
		    }
		} else {
		    service.email(order.getSymbol(), "Still sitting in working!");
		}

	    }
	};

	thread.start();
    }

    public void checkOrders() {
	refreshToken();

	HttpHeaders headers = new HttpHeaders();
	headers.setContentType(MediaType.APPLICATION_JSON);
	headers.set(AUTHORIZATION, BEARER + token);

	try {
	    HttpEntity<String> entity = new HttpEntity<String>(headers);
	    RestTemplate restTemplate = new RestTemplate();
	    ResponseEntity<PostGetOrder[]> response = restTemplate.exchange(orderLink + STATUS_QUEUED, HttpMethod.GET,
		    entity, PostGetOrder[].class);

	    for (PostGetOrder order : response.getBody()) {
		StringBuilder key = new StringBuilder();
		for (OrderLegCollection leg : order.getOrderLegCollection()) {
		    key.append(leg.getInstrument().getSymbol());
		}

		System.out.println(key);
	    }
	} catch (RestClientException e) {
	    e.printStackTrace();

	}
    }
}
