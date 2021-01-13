package org.kutsuki.zerotwo.document;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Account extends AbstractDocument {
    private String project;
    private String username;
    private String password;

    public String getProject() {
	return project;
    }

    public void setProject(String project) {
	this.project = project;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }
}
