import org.openspaces.admin.AdminFactory
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent

admin = new AdminFactory().addGroup("kimchy").createAdmin();
while (true) {

  admin.startStatisticsMonitor()
  admin.spaces.spaceInstanceStatisticsChanged << {SpaceInstanceStatisticsChangedEvent event -> println "Instance [$event.spaceInstance.uid] Stats: write [$event.statistics.writeCount]"}
  Thread.sleep 2000000
}