package org.kutsuki.zerotwo.rest;

import java.io.IOException;

public abstract class AbstractChrome {
    private static final String CHROME = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";
    private static final String DEV_TOOLS = "--auto-open-devtools-for-tabs";
    private static final String INCOGNITO = "--incognito";

    protected void openChrome(String link) throws IOException {
	Runtime.getRuntime().exec(new String[] { CHROME, DEV_TOOLS, link });
    }

    protected void openIngcognitoChrome(String link) throws IOException {
	Runtime.getRuntime().exec(new String[] { CHROME, INCOGNITO, DEV_TOOLS, link });
    }

    protected void closeChrome(String name) {
	WindowCloser.closeWindows(name);
    }
}
