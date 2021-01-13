package org.kutsuki.zerotwo.rest.openings;

import java.net.Authenticator;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.NtlmAuthenticator;
import org.kutsuki.zerotwo.document.Account;
import org.kutsuki.zerotwo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.api.services.sheets.v4.model.ValueRange;

@RestController
public class AmalgamRest extends AbstractSheets {
    private static final String AMALGAM = "Amalgam";
    private static final String CLEAR_RANGE = "Amalgam!A2:I";
    private static final String RANGE = "Amalgam!A2";

    private static final String EXTERNAL_REQNO = "<td headers=\"EXTERNAL_REQNO\" class=\"data\">";
    private static final String TITLE = "<td headers=\"POSITION_TITLE\" class=\"data\">";
    private static final String DESCRIPTION = "<td headers=\"POSITION_DESCRIPTION\" class=\"data\">";
    private static final String LABOR = "<td headers=\"LABOR_CATEGORY\" class=\"data\">";
    private static final String SECURITY = "<td headers=\"SECURITY_CLEARANCE\" class=\"data\">";
    private static final String SHIFT = "<td headers=\"SHIFT\" class=\"data\">";
    private static final String SCHEDULE = "<td headers=\"SCHEDULE\" class=\"data\">";
    private static final String LAST_UPDATED = "<td headers=\"LAST_UPDATED\" class=\"data\">";
    private static final String STATUS = "<td headers=\"STATUS\" class=\"data\">";
    private static final String TD_END = "</td>";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OpeningRest openingRest;

    @Value("${amalgam.link}")
    private String link;

    @Scheduled(cron = "0 0 6 * * *")
    public void getOpenings() {
	Account account = accountRepository.findByProject(AMALGAM);
	NtlmAuthenticator authenticator = new NtlmAuthenticator(account.getUsername(), account.getPassword());
	Authenticator.setDefault(authenticator);

	RestTemplate restTemplate = new RestTemplate();
	HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	restTemplate.setRequestFactory(requestFactory);

	String response = restTemplate.getForObject(link, String.class);

	String[] split = StringUtils.splitByWholeSeparator(response, EXTERNAL_REQNO);
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	for (int i = 1; i < split.length; i++) {
	    writeRowList.add(parseSplitOpening(split[i]));
	}

	// clear sheet
	clearSheet(CLEAR_RANGE);

	// write sheet
	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(RANGE, body);

	Authenticator.setDefault(null);

	openingRest.setLastCheckedNow(AMALGAM);
    }

    private List<Object> parseSplitOpening(String split) {
	List<Object> dataList = new ArrayList<Object>();
	dataList.add(StringUtils.substringBefore(split, TD_END));
	dataList.add(StringUtils.substringBetween(split, TITLE, TD_END));
	dataList.add(StringUtils.substringBetween(split, DESCRIPTION, TD_END));
	dataList.add(StringUtils.substringBetween(split, LABOR, TD_END));
	dataList.add(StringUtils.substringBetween(split, SECURITY, TD_END));
	dataList.add(StringUtils.substringBetween(split, SHIFT, TD_END));
	dataList.add(StringUtils.substringBetween(split, SCHEDULE, TD_END));
	dataList.add(StringUtils.substringBetween(split, LAST_UPDATED, TD_END));
	dataList.add(StringUtils.substringBetween(split, STATUS, TD_END));
	return dataList;
    }

}
