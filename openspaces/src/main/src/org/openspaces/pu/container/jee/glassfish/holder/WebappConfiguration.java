package org.openspaces.pu.container.jee.glassfish.holder;

/**
 * @author kimchy
 */
public class WebappConfiguration {

    private String contextPath;

    private String war;

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getWar() {
        return war;
    }

    public void setWar(String war) {
        this.war = war;
    }
}
