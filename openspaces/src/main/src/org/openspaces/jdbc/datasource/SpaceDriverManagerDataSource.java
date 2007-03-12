package org.openspaces.jdbc.datasource;

import com.j_spaces.core.IJSpace;
import com.j_spaces.jdbc.driver.GConnection;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A simple Jdbc {@link javax.sql.DataSource} based on a {@link com.j_spaces.core.IJSpace space}.
 * Returns a new Jdbc {@link java.sql.Connection} for each <code>getConnection</code>.
 *
 * @author kimchy
 */
public class SpaceDriverManagerDataSource extends AbstractDataSource implements InitializingBean {

    private IJSpace space;

    public SpaceDriverManagerDataSource() {

    }

    public SpaceDriverManagerDataSource(IJSpace space) {
        this.space = space;
    }

    public void setSpace(IJSpace space) {
        this.space = space;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(space, "space proeprty must be set");
    }

    public Connection getConnection() throws SQLException {
        return GConnection.getInstance(space);
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return GConnection.getInstance(space);
    }
}
