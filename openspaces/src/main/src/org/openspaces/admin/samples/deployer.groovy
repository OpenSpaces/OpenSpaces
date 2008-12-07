import org.openspaces.admin.AdminFactory
import org.openspaces.admin.space.SpaceDeployment

admin = new AdminFactory().addGroup("kimchy").createAdmin();
admin.gridServiceManagers.waitFor 1
admin.gridServiceContainers.waitFor 2
admin.gridServiceManagers.deploy(new SpaceDeployment("test"))