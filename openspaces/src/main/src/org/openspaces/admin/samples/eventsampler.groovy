import org.openspaces.admin.AdminFactory
import org.openspaces.admin.gsc.GridServiceContainer
import org.openspaces.admin.gsm.GridServiceManager
import org.openspaces.admin.lus.LookupService
import org.openspaces.admin.machine.Machine

admin = new AdminFactory().addGroup("kimchy").createAdmin();
while (true) {
  admin.machines.machineAdded << {Machine machine -> println "Machine [$machine.uid] Added" }
  admin.machines.machineRemoved << {Machine machine -> println "Machien [$machine.uid] Removed" }
  admin.lookupServices.lookupServiceAdded << {LookupService lookupService -> println "LUS [$lookupService.uid] Added" }
  admin.lookupServices.lookupServiceRemoved << {LookupService lookupService -> println "LUS [$lookupService.uid] Removed" }
  admin.gridServiceContainers.gridServiceContainerAdded << {GridServiceContainer gridServiceContainer -> println "GSC [$gridServiceContainer.uid] Added"}
  admin.gridServiceContainers.gridServiceContainerRemoved << {GridServiceContainer gridServiceContainer -> println "GSC [$gridServiceContainer.uid] Removed"}
  admin.gridServiceManagers.gridServiceManagerAdded << {GridServiceManager gridServiceManger -> println "GSM [$gridServiceManger.uid] Added"}
  admin.gridServiceManagers.gridServiceManagerRemoved << {GridServiceManager gridServiceManger -> println "GSM [$gridServiceManger.uid] Removed"}
  Thread.sleep 2000000
}