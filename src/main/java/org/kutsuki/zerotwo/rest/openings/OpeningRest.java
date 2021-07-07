package org.kutsuki.zerotwo.rest.openings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.kutsuki.zerotwo.document.Opening;
import org.kutsuki.zerotwo.repository.OpeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.sheets.v4.model.ValueRange;

@RestController
public class OpeningRest extends AbstractSheets {
    private static final String CLEAR_RANGE = "LastChecked!A2:B";
    private static final String RANGE = "LastChecked!A2";
    private static final String DISABLED = "Disabled";
    private static final String THIS_LAST_CHECKED = "(THIS) Last Checked";

    @Autowired
    private OpeningRepository repository;

    @Value("${sheets.sheetId}")
    private String sheetId;

    @Scheduled(cron = "0 30 6 * * *")
    public void writeLastChecked() {
	clearSheet(sheetId, CLEAR_RANGE);
	List<List<Object>> writeRowList = new ArrayList<List<Object>>();
	for (Opening opening : repository.findAll()) {
	    List<Object> dataList = new ArrayList<Object>();
	    dataList.add(opening.getProject());
	    dataList.add(opening.getLastChecked());
	    writeRowList.add(dataList);
	}

	List<Object> dataList = new ArrayList<Object>();
	dataList.add(THIS_LAST_CHECKED);
	dataList.add(LocalDate.now().toString());
	writeRowList.add(dataList);

	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(sheetId, RANGE, body);
    }

    public String getLastChecked(String project) {
	Opening opening = repository.findByProject(project);
	return opening.getLastChecked();
    }

    public void setLastChecked(String project, String lastChecked) {
	Opening opening = repository.findByProject(project);
	opening.setLastChecked(lastChecked);
	repository.save(opening);
    }

    public void setLastCheckedNow(String project) {
	setLastChecked(project, LocalDate.now().toString());
    }

    public void setLastCheckedDisabled(String project) {
	setLastChecked(project, DISABLED);
    }
}
