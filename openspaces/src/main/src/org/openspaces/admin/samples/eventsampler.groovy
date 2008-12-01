import org.openspaces.admin.AdminFactory
import org.openspaces.admin.lus.LookupService
import org.openspaces.admin.machine.Machine

admin = new AdminFactory().addGroup("kimchy").createAdmin();
while (true) {
  admin.machines.machineAdded << {Machine machine -> println "Machine [$machine.uid] Added" }
  admin.machines.machineRemoved << {Machine machine -> println "Machien [$machine.uid] Removed" }
  admin.lookupServices.lookupServiceAdded << {LookupService lookupService -> println "Lookup Service [$lookupService.uid] Added" }
  admin.lookupServices.lookupServiceRemoved << {LookupService lookupService -> println "Lookup Service [$lookupService.uid] Removed" }
  Thread.sleep 2000000
}