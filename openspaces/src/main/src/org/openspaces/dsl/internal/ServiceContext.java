package org.openspaces.dsl.internal;

import org.openspaces.dsl.Service;

public class ServiceContext {

	private Service service;
	public ServiceContext(Service service) {
		super();
		this.service = service;
	}

	private String text = "Some Text";

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}
	
}
