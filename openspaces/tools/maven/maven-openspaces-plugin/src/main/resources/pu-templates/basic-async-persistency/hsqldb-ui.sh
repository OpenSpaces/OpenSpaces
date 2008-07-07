# Specify the location of hsqldb.jar
CLASSPATH=/export/utils/hsqldb/lib/hsqldb.jar

# Specify the database URL
HSQLDB_URL=jdbc:hsqldb:hsql://localhost/testDB

java -cp ${CLASSPATH} org.hsqldb.util.DatabaseManagerSwing -url ${HSQLDB_URL}