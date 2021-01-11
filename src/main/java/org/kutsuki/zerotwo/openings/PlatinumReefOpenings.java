package org.kutsuki.zerotwo.openings;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.api.services.sheets.v4.model.ValueRange;

@Component
public class PlatinumReefOpenings extends AbstractSheets {
    private static final String PLATINUM_REEF_RANGE = "PlatinumReef!A";
    private static final String CLEAR_RANGE = "PlatinumReef!A:A";

    public int rowNum;

    public PlatinumReefOpenings() {
	this.rowNum = 1;
    }

    public void clear() {
	clearSheet(CLEAR_RANGE);
	this.rowNum = 1;
    }

    public void addOpening(String row) {
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	List<Object> dataList = new ArrayList<Object>();
	dataList.add(row);
	writeRowList.add(dataList);

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(PLATINUM_REEF_RANGE + rowNum, body);
    }
}
