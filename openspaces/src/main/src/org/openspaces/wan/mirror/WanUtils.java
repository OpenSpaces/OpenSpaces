package org.openspaces.wan.mirror;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;

import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.SpaceURLParser;

public class WanUtils {

    private WanUtils() {
        // private Constructor to avoid instantiation
    }

    public static boolean checkPortAvailable(final int port) {
        ServerSocket sock = null;

        try {
            sock = new ServerSocket(port);
            sock.setReuseAddress(true);
            return true;
        } catch (final IOException e) {
            return false;
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (final IOException e) {
                    // ignore
                }
            }
        }

    }
    
    public static Admin getAdminForLocalCluster(String clusterUrl) {

        SpaceURL url;
        try {
            url = SpaceURLParser.parseURL(clusterUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse url " + clusterUrl, e);
        }

        
        final AdminFactory factory = new AdminFactory();
        if (url.getLookupLocators() != null && url.getLookupLocators().length > 0) {
            factory.addLocators(getURLString(url.getLookupLocators()));
        }
        if (url.getLookupGroups() != null && url.getLookupGroups().length > 0) {
            factory.addGroups(getURLString(url.getLookupGroups()));
        }

        final Admin admin = factory.createAdmin();
        return admin;

    }
    
    private static String getURLString(final Object[] params) {
        final StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (final Object param : params) {

            if (first) {
                builder.append(param.toString());
                first = false;
            } else {
                builder.append(",");
                builder.append(param.toString());
            }
        }

        final String res = builder.toString();
        return res;
    }

}
