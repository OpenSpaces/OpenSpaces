import org.openspaces.admin.AdminFactory

admin = new AdminFactory().addGroup("kimchy").createAdmin();
while (true) {
//  admin.gridServiceManagers.each { println "GSM: $it.uid" }
  println "$admin.machines.size"
  admin.virtualMachines.each { println "VM: $it.uid" }
  Thread.sleep 2000
}