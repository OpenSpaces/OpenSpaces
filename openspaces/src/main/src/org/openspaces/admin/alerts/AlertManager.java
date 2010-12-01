package org.openspaces.admin.alerts;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.alerts.events.AlertEventManager;
import org.openspaces.admin.alerts.strategy.AlertStrategyConfig;
import org.openspaces.admin.strategy.StrategyManager;
import org.openspaces.admin.strategy.StrategyPropertiesManager;

public interface AlertManager extends StrategyManager<AlertStrategyConfig>, AlertEventManager, AdminAware  {
	
	StrategyPropertiesManager getStrategies();
	
	void fireAlert(Alert alert);
}
