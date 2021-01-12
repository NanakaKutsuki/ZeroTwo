package org.kutsuki.zerotwo.rest.openings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.rest.post.PostArray;
import org.kutsuki.zerotwo.rest.post.PostRoadRally;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.sheets.v4.model.ValueRange;

@RestController
public class RoadRallyRest extends AbstractSheets {
    private static final String ROAD_RALLY = "RoadRally";
    private static final String RANGE = ROAD_RALLY + "!A";
    private static final String CLEAR_RANGE = RANGE + "2:E";

    private boolean footer;
    private int index;
    private int rowNum;
    private List<String> linkList;

    public void clear() {
	clearSheet(CLEAR_RANGE);
    }

    public RoadRallyRest() {
	this.linkList = new ArrayList<String>();
	this.footer = false;
	this.index = 0;
	this.rowNum = 2;
    }

    @GetMapping("rest/roadrally/getNextLink")
    public String getNextLink() {
	String link = StringUtils.EMPTY;

	if (index < linkList.size()) {
	    link = linkList.get(index);
	    index++;
	} else if (index >= linkList.size() && !footer) {
	    String lastChecked = LocalDate.now().toString();
	    setLastChecked(ROAD_RALLY, lastChecked);

	    List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(lastChecked);
	    dataList.add(getLastUpdated());
	    writeRowList.add(dataList);

	    ValueRange body = new ValueRange();
	    body.setValues(writeRowList);
	    writeSheet(RANGE + rowNum, body);
	    rowNum++;

	    footer = true;
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
	    this.rowNum = 2;
	    this.footer = false;
	    clearSheet(CLEAR_RANGE);
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
	writeSheet(RANGE + rowNum, body);
	rowNum++;

	return ResponseEntity.ok().build();
    }

}
