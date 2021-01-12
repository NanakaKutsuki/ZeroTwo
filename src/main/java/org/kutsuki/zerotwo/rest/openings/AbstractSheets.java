package org.kutsuki.zerotwo.rest.openings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.kutsuki.zerotwo.EmailService;
import org.kutsuki.zerotwo.document.Opening;
import org.kutsuki.zerotwo.repository.OpeningsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

@Component
public abstract class AbstractSheets {
    private static final int PORT = 8888;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "ZeroTwo";
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String OFFLINE = "offline";
    private static final String LAST_UPDATED = "Last Updated";
    private static final String TOKENS_DIRECTORY_PATH = "/tokens";
    private static final String USER = "user";
    private static final String USER_ENTERED = "USER_ENTERED";

    private NetHttpTransport transport;
    private Sheets sheets;

    @Autowired
    private EmailService service;

    @Autowired
    private OpeningsRepository repository;

    @Value("${vacancies.path}")
    private String path;

    @Value("${vacancies.sheetId}")
    private String sheetId;

    public AbstractSheets() {
	try {
	    this.transport = GoogleNetHttpTransport.newTrustedTransport();
	    this.sheets = new Sheets.Builder(transport, JSON_FACTORY, getCredentials())
		    .setApplicationName(APPLICATION_NAME).build();
	} catch (GeneralSecurityException | IOException e) {
	    service.emailException("Error connecting with Google Sheets!", e);
	}
    }

    // protected EmailService getEmailService() {
    // return service;
    // }

    protected String getLastUpdated() {
	return LAST_UPDATED;
    }

    public String getPath() {
	return path;
    }

    protected void clearSheet(String range) {
	int retries = 0;
	ClearValuesResponse response = null;

	while (response == null) {
	    try {
		response = sheets.spreadsheets().values().clear(sheetId, range, new ClearValuesRequest()).execute();
	    } catch (IOException e) {
		retries++;

		if (retries == 10) {
		    service.emailException("Error clearing sheet: " + range, e);
		}
	    }
	}
    }

    protected List<List<Object>> readSheet(String range) {
	int retries = 0;
	List<List<Object>> result = null;

	while (result == null) {
	    try {
		ValueRange response = sheets.spreadsheets().values().get(sheetId, range).execute();
		result = response.getValues();
	    } catch (IOException e) {
		retries++;

		if (retries == 10) {
		    service.emailException("Error reading sheet: " + range, e);
		}
	    }
	}

	return result;
    }

    protected void setLastChecked(String project, String lastChecked) {
	Opening opening = repository.findByProject(project);
	opening.setLastChecked(lastChecked);
	repository.save(opening);
    }

    protected void writeSheet(String range, ValueRange body) {
	boolean completed = false;
	int retries = 0;

	while (!completed) {
	    try {
		sheets.spreadsheets().values().update(sheetId, range, body).setValueInputOption(USER_ENTERED).execute();
		completed = true;
	    } catch (IOException e) {
		retries++;

		if (retries == 10) {
		    service.emailException("Error writing sheet: " + range, e);
		}
	    }
	}
    }

    private Credential getCredentials() throws IOException {
	// Load client secrets.
	InputStream in = AbstractSheets.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
	if (in == null) {
	    throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
	}

	GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	List<String> scopes = new ArrayList<String>();
	scopes.add(SheetsScopes.SPREADSHEETS);

	// Build flow and trigger user authorization request.
	GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(transport, JSON_FACTORY,
		clientSecrets, scopes).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
			.setAccessType(OFFLINE).build();
	LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(PORT).build();

	return new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER);
    }
}
