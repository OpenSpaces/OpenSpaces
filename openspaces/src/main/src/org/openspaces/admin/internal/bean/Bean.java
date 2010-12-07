package org.openspaces.admin.internal.bean;

import java.util.Map;

import org.openspaces.admin.Admin;

public interface Bean {
	void setAdmin(Admin admin);
	
	void setProperties(Map<String, String> properties);
	Map<String, String> getProperties();

	void afterPropertiesSet();
	void destroy();
}
