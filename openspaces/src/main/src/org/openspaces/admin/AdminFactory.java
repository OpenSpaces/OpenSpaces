package org.openspaces.admin;

import com.j_spaces.kernel.PlatformVersion;
import net.jini.core.discovery.LookupLocator;
import org.jini.rio.boot.BootUtil;
import org.openspaces.admin.internal.admin.DefaultAdmin;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author kimchy
 */
public class AdminFactory {

    private List<String> groups = null;

    private String locators = null;

    public AdminFactory addGroup(String group) {
        if (groups == null) {
            groups = new ArrayList<String>();
        }
        groups.add(group);
        return this;
    }

    public AdminFactory addLocator(String locator) {
        if (locators == null) {
            locators = locator;
        } else {
            locators += "," + locator;
        }
        return this;
    }

    public Admin getAdmin() {
        return new DefaultAdmin(getGroups(), getLocators());
    }

    private String[] getGroups() {
        String[] groups;
        if (this.groups == null) {
            String groupsProperty = System.getProperty("com.gs.jini_lus.groups");
            if (groupsProperty == null) {
                groupsProperty = System.getenv("LOOKUPGROUPS");
            }
            if (groupsProperty != null) {
                StringTokenizer tokenizer = new StringTokenizer(groupsProperty);
                int count = tokenizer.countTokens();
                groups = new String[count];
                for (int i = 0; i < count; i++) {
                    groups[i] = tokenizer.nextToken();
                }
            } else {
                groups = new String[]{"gigaspaces-" + PlatformVersion.getVersionNumber()};
            }
        } else {
            groups = this.groups.toArray(new String[this.groups.size()]);
        }
        return groups;
    }

    private LookupLocator[] getLocators() {
        if (locators == null) {
            String locatorsProperty = System.getProperty("com.gs.jini_lus.locators");
            if (locatorsProperty == null) {
                locatorsProperty = System.getenv("LOOKUPLOCATORS");
            }
            if (locatorsProperty != null) {
                locators = locatorsProperty;
            }
        }
        return BootUtil.toLookupLocators(locators);
    }
}
