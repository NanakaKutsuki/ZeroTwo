package org.kutsuki.zerotwo.rest.openings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
public class EitoRest extends AbstractSheets {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("M_d_yyyy");
    private static final String EITO = "EITO";
    private static final String CLEAR_RANGE = "EITO!A:K";
    private static final String RANGE = "EITO!A1";
    private static final String DEFAULT_ASPX = "default.aspx";
    private static final String ANNOUNCEMENTS = "Lists/Announcements/DispForm.aspx?ID=";
    private static final String ATTACHMENTS = "Lists/Announcements/Attachments/";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OpeningRest openingRest;

    @Value("${eito.link}")
    private String link;

    @Scheduled(cron = "0 3 6 * * *")
    public void getOpenings() {
	Account account = accountRepository.findByProject(EITO);
	NtlmAuthenticator authenticator = new NtlmAuthenticator(account.getUsername(), account.getPassword());
	Authenticator.setDefault(authenticator);

	RestTemplate restTemplate = new RestTemplate();
	HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
	restTemplate.setRequestFactory(requestFactory);

	String response = restTemplate.getForObject(link + DEFAULT_ASPX, String.class);
	String id = StringUtils.substringBetween(response, ANNOUNCEMENTS, Character.toString('"'));

	response = restTemplate.getForObject(link + ANNOUNCEMENTS + id, String.class);
	String xlsx = StringUtils.substringBetween(response, ATTACHMENTS, Character.toString('"'));

	String filename = StringUtils.substringAfter(xlsx, Character.toString('_'));
	filename = StringUtils.substringBeforeLast(filename, Character.toString('.'));

	LocalDate date = LocalDate.parse(filename, DTF);
	LocalDate lastDate = LocalDate.parse(openingRest.getLastChecked(EITO));

	if (date.isAfter(lastDate)) {
	    byte[] byteResponse = restTemplate.getForObject(link + ATTACHMENTS + xlsx, byte[].class);
	    InputStream is = new ByteArrayInputStream(byteResponse);
	    parseWorkbook(is);
	    openingRest.setLastChecked(EITO, date.toString());
	}

	Authenticator.setDefault(null);
    }

    private void parseWorkbook(InputStream is) {
	try {
	    XSSFWorkbook workbook = new XSSFWorkbook(is);

	    Sheet firstSheet = workbook.getSheetAt(0);
	    Iterator<Row> rowIterator = firstSheet.iterator();
	    List<List<Object>> writeRowList = new ArrayList<List<Object>>();

	    while (rowIterator.hasNext()) {
		Row nextRow = rowIterator.next();
		Iterator<Cell> cellIterator = nextRow.cellIterator();
		List<Object> dataList = new ArrayList<Object>();

		while (cellIterator.hasNext()) {
		    Cell cell = cellIterator.next();
		    dataList.add(cell.getStringCellValue());
		}

		writeRowList.add(dataList);
	    }

	    // clear sheet
	    clearSheet(CLEAR_RANGE);

	    // write sheet
	    ValueRange body = new ValueRange();
	    body.setValues(writeRowList);
	    writeSheet(RANGE, body);

	    // close workbook
	    workbook.close();
	} catch (IOException e) {
	    getEmailService().emailException("Error with EITO Workbook! ", e);
	}
    }
}
