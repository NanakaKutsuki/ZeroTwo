package org.kutsuki.zerotwo.rest.openings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.rest.post.PostArray;
import org.kutsuki.zerotwo.rest.post.PostRoadRally;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.sheets.v4.model.ValueRange;

@RestController
public class RoadRallyRest extends AbstractSheets {
    private static final String ROAD_RALLY = "RoadRally";
    private static final String RANGE = "RoadRally!A";
    private static final String CLEAR_RANGE = "RoadRally!A2:E";

    private int index;
    private int lastIndex;
    private int rowNum;
    private List<String> linkList;

    @Autowired
    private OpeningRest openingRest;

    @Value("${roadrally.link}")
    private String link;

    @Value("${sheets.sheetId}")
    private String sheetId;

    public RoadRallyRest() {
	this.linkList = new ArrayList<String>();
	this.index = 0;
	this.lastIndex = -1;
	this.rowNum = 2;
    }

    @Scheduled(cron = "0 15 6 * * *")
    public void openBrowser() {
	try {
	    openChrome(link);
	} catch (IOException e) {
	    getEmailService().emailException("Unable to open: " + link, e);
	}
    }

    @GetMapping("rest/roadrally/getNextLink")
    public String getNextLink() {
	String link = StringUtils.EMPTY;

	if (index < linkList.size() && index != lastIndex) {
	    link = linkList.get(index);
	    lastIndex = index;
	} else if (index >= linkList.size()) {
	    link = Character.toString('X');
	}

	return link;
    }

    @PostMapping("rest/roadrally/addLinks")
    public ResponseEntity<String> addLinks(@RequestBody PostArray postData) {
	if (postData != null) {
	    this.linkList = Arrays.asList(postData.getData());
	    this.index = 0;
	    this.lastIndex = -1;
	    this.rowNum = 2;
	    clearSheet(sheetId, CLEAR_RANGE);
	}

	return ResponseEntity.ok().build();
    }

    @PostMapping("rest/roadrally/addOpening")
    public ResponseEntity<String> addOpening(@RequestBody PostRoadRally postData) {
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	List<Object> dataList = new ArrayList<Object>();
	dataList.add(postData.getOpenedDate());
	dataList.add(postData.getTTO());
	dataList.add(postData.getTitle());
	dataList.add(postData.getSkill());
	dataList.add(postData.getDescription());
	writeRowList.add(dataList);

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(sheetId, RANGE + rowNum, body);
	index++;
	rowNum++;

	if (index >= linkList.size()) {
	    openingRest.setLastCheckedNow(ROAD_RALLY);
	}

	return ResponseEntity.ok().build();
    }
}
