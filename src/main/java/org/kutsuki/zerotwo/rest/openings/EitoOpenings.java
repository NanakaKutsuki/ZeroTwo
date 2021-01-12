package org.kutsuki.zerotwo.rest.openings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.google.api.services.sheets.v4.model.ValueRange;

@Component
public class EitoOpenings extends AbstractSheets {
    private static final String EITO_RANGE = "EITO!A:K";

    public void parseVacancies(String filename) throws IOException {
	File file = new File(getPath() + filename);

	try (FileInputStream fis = new FileInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(fis);) {
	    Sheet firstSheet = workbook.getSheetAt(0);
	    Iterator<Row> rowIterator = firstSheet.iterator();
	    List<List<Object>> writeRowList = new ArrayList<List<Object>>();

	    while (rowIterator.hasNext()) {
		Row nextRow = rowIterator.next();
		Iterator<Cell> cellIterator = nextRow.cellIterator();
		List<Object> dataList = new ArrayList<Object>();

		while (cellIterator.hasNext()) {
		    Cell cell = cellIterator.next();
		    String value = cell.getStringCellValue();
		    dataList.add(value);
		}

		writeRowList.add(dataList);
	    }

	    // clear sheet
	    clearSheet(EITO_RANGE);

	    // write sheet
	    ValueRange body = new ValueRange();
	    body.setValues(writeRowList);
	    writeSheet(EITO_RANGE, body);
	} catch (IOException e) {
	    throw e;
	}
    }
}
