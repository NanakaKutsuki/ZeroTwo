package org.kutsuki.zerotwo.rest.openings;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kutsuki.zerotwo.document.Account;
import org.kutsuki.zerotwo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.sheets.v4.model.ValueRange;

@RestController
public class ImagineDragonRest extends AbstractSheets {
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("M.d.yy");
    private static final String CLEAR_RANGE = "ImagineDragon!A:G";
    private static final String IMAGINE_DRAGON = "ImagineDragon";
    private static final String INBOX = "INBOX";
    private static final String POP3S = "pop3s";
    private static final String POP3_HOST = "mail.pop3.host";
    private static final String POP3_PORT = "mail.pop3.port";
    private static final String POP3_START_TTLS = "mail.pop3.starttls.enable";
    private static final String RANGE = "ImagineDragon!A1";
    private static final String TABLE = "Table";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OpeningRest openingRest;

    @Value("${imaginedragon.host}")
    private String host;

    @Value("${imaginedragon.port}")
    private String port;

    @Scheduled(cron = "0 9 6 * * *")
    public void parseEmail() {
	try {
	    Properties properties = new Properties();
	    properties.put(POP3_HOST, host);
	    properties.put(POP3_PORT, port);
	    properties.put(POP3_START_TTLS, Boolean.TRUE.toString());
	    Session emailSession = Session.getDefaultInstance(properties);

	    Store store = emailSession.getStore(POP3S);

	    Account account = accountRepository.findByProject(IMAGINE_DRAGON);
	    store.connect(host, account.getUsername(), account.getPassword());

	    Folder emailFolder = store.getFolder(INBOX);
	    emailFolder.open(Folder.READ_ONLY);

	    Message[] messages = emailFolder.getMessages();

	    boolean found = false;
	    int i = messages.length - 1;
	    while (i >= 0 && !found) {
		found = parseMessage(messages[i]);
		i--;
	    }

	    // close email
	    emailFolder.close(false);
	    store.close();
	} catch (MessagingException | IOException e) {
	    getEmailService().emailException("Error parsing ImagineDragon!", e);
	}
    }

    private boolean parseMessage(Message message) throws MessagingException, IOException {
	boolean found = false;

	if (StringUtils.contains(message.getSubject(), TABLE)) {
	    MimeMultipart multipart = (MimeMultipart) message.getContent();

	    int i = 0;
	    while (i < multipart.getCount() && !found) {
		BodyPart bodyPart = multipart.getBodyPart(i);
		if (StringUtils.equals(bodyPart.getDisposition(), BodyPart.ATTACHMENT)
			&& StringUtils.contains(bodyPart.getFileName(), TABLE)) {
		    String filename = StringUtils.substringAfterLast(bodyPart.getFileName(), StringUtils.SPACE);
		    filename = StringUtils.substringBeforeLast(filename, Character.toString('.'));

		    LocalDate date = LocalDate.parse(filename, DTF);
		    LocalDate lastDate = LocalDate.parse(openingRest.getLastChecked(IMAGINE_DRAGON));

		    if (date.isAfter(lastDate)) {
			parseWorkbook(bodyPart.getInputStream());
			openingRest.setLastChecked(IMAGINE_DRAGON, date.toString());
		    }

		    found = true;
		}

		i++;
	    }
	}

	return found;
    }

    private void parseWorkbook(InputStream is) throws IOException {
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
		dataList.add(getCell(cell));
	    }

	    writeRowList.add(dataList);
	}

	// close workbook
	workbook.close();

	// clear sheet
	clearSheet(CLEAR_RANGE);

	// write sheet
	ValueRange body = new ValueRange();
	body.setValues(writeRowList);
	writeSheet(RANGE, body);
    }

    private String getCell(Cell cell) {
	String value = null;

	switch (cell.getCellType()) {
	case BLANK:
	    break;
	case NUMERIC:
	    value = Double.toString(cell.getNumericCellValue());
	    break;
	case STRING:
	    value = cell.getStringCellValue();
	    break;
	case FORMULA:
	    value = Character.toString('=') + cell.getCellFormula();
	    break;
	default:
	    value = cell.getCellType().toString();
	    break;
	}

	return value;
    }
}
