package org.openspaces.itest.jdbc.simple;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author kimchy
 */
public class SimpleJdbcTests extends AbstractTransactionalDataSourceSpringContextTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/jdbc/simple/jdbc.xml"};
    }

    public void testSimpleOperation() {
        jdbcTemplate.execute("create table Person(FirstName varchar2 INDEX, LastName varchar2)");
        jdbcTemplate.execute("insert into Person values(?,?)", new PreparedStatementCallback() {
            public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                for (int i = 0; i < 10; i++) {
                    preparedStatement.setString(1, "FirstName" + i);
                    preparedStatement.setString(2, "LastName" + i);
                    preparedStatement.executeUpdate();
                }
                return null;
            }
        });
        long count = jdbcTemplate.queryForLong("select count(*) from Person");
        assertEquals(10, count);
    }
}
