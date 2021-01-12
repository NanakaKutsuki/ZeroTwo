package org.kutsuki.zerotwo.rest.openings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kutsuki.zerotwo.rest.post.PostTuple;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.sheets.v4.model.ValueRange;

@RestController
public class PlatinumReefRest extends AbstractSheets {
    private static final String PLATINUM_REEF = "PlatinumReef";
    private static final String RANGE = PLATINUM_REEF + "!A";
    private static final String CLEAR_RANGE = RANGE + ":A";

    @GetMapping("/rest/platinumReef/clear")
    public ResponseEntity<String> clear() {
	clearSheet(CLEAR_RANGE);
	return ResponseEntity.ok().build();
    }

    @PostMapping("rest/platinumReef/addOpening")
    public ResponseEntity<String> addOpening(@RequestBody PostTuple postData) {
	String opening = postData.getData();
	if (StringUtils.equals(opening, getLastUpdated())) {
	    String lastChecked = LocalDate.now().toString();
	    opening += StringUtils.SPACE + lastChecked;
	    setLastChecked(PLATINUM_REEF, lastChecked);
	}

	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	List<Object> dataList = new ArrayList<Object>();
	dataList.add(opening);
	writeRowList.add(dataList);

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(RANGE + postData.getId(), body);
	return ResponseEntity.ok().build();
    }
}
