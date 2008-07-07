rem Specify the location of hsqldb.jar
set CLASSPATH=D:/hsqldb/lib/hsqldb.jar

rem Specify the database URL
set HSQLDB_URL=jdbc:hsqldb:hsql://localhost/testDB

java -cp "%CLASSPATH%" org.hsqldb.util.DatabaseManagerSwing -url %HSQLDB_URL%