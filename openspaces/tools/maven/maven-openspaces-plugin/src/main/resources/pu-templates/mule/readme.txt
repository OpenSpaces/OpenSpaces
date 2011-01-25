Creates a SBA application with two processing units that use mule as an ESB.
The Feeder processing unit writes Data objects to the Space.
The Processor processing unit uses the extended SEDA model to defines 3 services.
A Verifier service that verifies unprocessed Data objects, an Approver service that
approves verified Data objects and a Processor service that processes approved
Data objects. 
The Space and the Processor are collocated in the same processing unit.
JVM: >= 5.

GENERAL DESCRIPTION:
--------------------

  The project consists of three modules: common, processor and feeder. The common
module includes all the shared resources and classes between both the processor
and the feeder. In our case, the common module includes the "Data" class which
is written and taken from the Space.

  The processor module, which is a processing unit, starts up a Space and 3 services.
A Verifier service gets unprocessed Data objects from the space verifies them and
writes the verified objects to an internal SEDA queue - queue1.
An Approver service gets verified unprocessed Data objects from queue1 approves them 
and writes the approved object to a second internal SEDA queue - queue2.
A Processor service gets approved Data object from queue2 processes them and writes
the processed objects to the space. 

  The feeder module, which is also a processing unit, connects to a Space through mule
and writes unprocessed Data objects to the Space. It uses mule quartz service for 
scheduling.

BUILDING, PACKAGING, RUNNING, DEPLOYING
---------------------------------------

*Note:

  In order to use Mule with GigaSpaces, mule jar files must be copied to the GigaSpaces
installation under GSHOME/lib/platform/mule (if the mule directory does not exists, create it).
In order to obtain the mule jar files, please download mule 2.1.2 from http://mule.mulesource.org.

The following needs to be copied:
  - From MULEHOME/lib/mule and into GSHOME/lib/platform/mule: mule-core, mule-module-client,
mule-module-spring-config, mule-module-spring-extras, mule-transport-quartz, mule-transport-stdio,
mule-transport-vm, mule-transport-http.
  - From MULEHOME/lib/opt and into GSHOME/lib/platform/mule: commons-beanutils, commons-collections,
commons-io, commons-lang, commons-pool, jug.osgi-2.0.0, quartz-all, backport-util-concurrent.

Libraries Required:
The following libraries should be located at your GigaSpaces Root\lib\platform\mule folder before deploying the example. 
 commons-beanutils.osgi-1.7.0.jar
 commons-collections-3.2.jar
 commons-io-1.3.1.jar
 commons-lang.osgi-2.4.jar
 commons-pool-1.4.jar
 dom4j.osgi-1.6.1.jar
 jaxen.osgi-1.1.1.jar
 jug.osgi-2.0.0.jar
 mule-core-2.2.1.jar
 mule-module-builders-2.2.1.jar
 mule-module-client-2.2.1.jar
 mule-module-management-2.2.1.jar
 mule-module-spring-config-2.2.1.jar
 mule-module-spring-extras-2.2.1.jar
 mule-module-xml-2.2.1.jar
 mule-transport-http-2.2.1.jar
 mule-transport-quartz-2.2.1.jar
 mule-transport-ssl-2.2.1.jar
 mule-transport-stdio-2.2.1.jar
 mule-transport-tcp-2.2.1.jar
 mule-transport-vm-2.2.1.jar
 quartz-all.osgi-1.6.0.jar
 backport-util-concurrent.osgi-3.1.jar

The above creates the ability to deploy a mule processing unit that does not have the mule jars files
in it. It is also possible to package the mule jar files into the processing unit "lib" directory, without
the need to create the GSHOME/lib/mule directory at all.

Quick list:

* mvn compile: Compiles the project.
* mvn os:run: Runs the project.
* mvn test: Runs the tests in the project.
* mvn package: Compiles and packages the project.
* mvn os:run-standalone: Runs a packaged application (from the jars).
* mvn os:deploy: Deploys the project onto the Service Grid.
* mvn os:undeploy: Removes the project from the Service Grid.

  In order to build the example, a simple "mvn compile" executed from the root of the 
project will compile all the different modules.

  Packaging the application can be done using "mvn package" (note, by default, it also
runs the tests, in order to disable it, use -DskipTests). The packaging process jars up 
the common module. The feeder and processor modules packaging process creates a 
"processing unit structure" directory within the target directory called [app-name]-[module].
It also creates a jar from the mentioned directory called [app-name]-[module].jar.

  In order to simply run both the processor and the feeder (after compiling), "mvn os:run" can be used.
This will run a single instance of the processor and a single instance of the feeder within
the same JVM using the compilation level classpath (no need for packaging). 
  A specific module can also be executed by itself, which in this case, executing more than 
one instance of the processing unit can be done. For example, running the processor module with 
a cluster topology of 2 partitions, each with one backup, the following command can be used:
mvn os:run -Dmodule=processor -Dcluster="total_members=2,1".

  In order to run a packaged processing unit, "mvn package os:run-standalone" can be used (if
"mvn package" was already executed, it can be omitted). This operation will run the processing units
using the packaged jar files. Running a specific module with a cluster topology can be executed using:
mvn package os:run-standalone -Dmodule=processor -Dcluster="total_members=2,1".

  Deploying the application requires starting up a GSM and at least 2 GSCs (scripts located under
the bin directory within the GigaSpaces installation). Once started, running "mvn package os:deploy"
will deploy the two processing units. 
  When deploying, the SLA elements within each processing unit descriptor (pu.xml) are taken into 
account. This means that by default when deploying the application, 2 partitions, each with 
one backup will be created for the processor, and a single instance of the feeder will be created.
  A special note regarding groups and deployment: If the GSM and GSCs were started under a specific 
group, the -Dgroups=[group-name] will need to be used in the deploy command.

WORKING WITH ECLIPSE
--------------------

  In order to generate eclipse project the following command need to be executed from the root of
the application: "mvn eclipse:eclipse". Pointing the Eclipse import existing project wizard
to the application root directory will result in importing the three modules.
If this is a fresh Eclipse installation, the M2_REPO needs be defined and pointed to the local 
maven repository (which resides under USER_HOME/.m2/repository).

  The application itself comes with built in launch targets allowing to run the processor and the 
feeder using Eclipse run (or debug) targets.

A NOTE OF CLUSTERING
--------------------

  This application focus on showing how SBA is used. The processor starts up an embedded Space and 
works directly on it. When deploying 2 partitions of the processor, two embedded spaces (within the
same cluster) will be created, with each polling container working only on the cluster member it 
started in an in memory and transactional manner. This is the power of such an architecture, where
the processing of the Data happens in a collocated manner with the Data. If we want to add High
Availability to the processor, we can deploy 2 partitions, each with one backup (2,1). In this 
case, the processor instances that ends up starting a cluster member Space which is the backup
will not perform any processing since the polling container identifies the Space state and won't
perform the take operation. If one of the processor primaries instances will fail, the backup
instance will become primary (with an up to date data), and its polling container will start
processing all the relevant Data. Note, when deploying on top of the Service Grid, the Service
Grid will also identify that one instance failed, and will automatically start it over in another
container (GSC).

  The feeder works with a clustered view of the Space (the 2,1 cluster topology looking as one), and 
simply writes unprocessed Data objects to the Space. The routing (@SpaceRouting) controls to which
partition the unprocessed Data will be written and consequently which instance will process it.

MAVEN PLUGIN WIKI PAGE
---------------------------------

  For more information about the Maven Plugin please refer to:
http://www.gigaspaces.com/wiki/display/XAP8/Maven+Plugin

  For more information about the Mule ESB please refer to:
http://www.gigaspaces.com/wiki/display/XAP8/Mule+ESB