package org.kutsuki.zerotwo.document;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Opening extends AbstractDocument {
    private String project;
    private String lastChecked;

    public String getProject() {
	return project;
    }

    public void setProject(String project) {
	this.project = project;
    }

    public String getLastChecked() {
	return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
	this.lastChecked = lastChecked;
    }
}
