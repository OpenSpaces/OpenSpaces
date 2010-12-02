package org.openspaces.admin.internal.alerts;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.openspaces.admin.alerts.Alert;

public class DefaultAlertGroup implements AlertGroup {

	private final List<Alert> alertsList;
	
	public DefaultAlertGroup(Alert[] alerts) {
		alertsList = Arrays.asList(alerts);
	}
	
	public boolean isOpen() {
		//a group is open if it does not end with a 'positive' alert.
		return alertsList.get(alertsList.size()-1).isPositive() == false;
	}
	
	public String getName() {
		return alertsList.get(0).getAlertType();
	}

	public Iterator<Alert> iterator() {
		return alertsList.iterator();
	}
}
