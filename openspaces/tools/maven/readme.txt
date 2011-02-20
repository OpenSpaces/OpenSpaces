OPENSPACES MAVEN INTEGRATION
----------------------------

OpenSpaces Maven (http://maven.apache.org) integration allows to use GigaSpaces with
maven. The integration includes the ability to install GigaSpaces into the maven local
repository as well as includes a maven plugin allowing to simplify the build/run/deploy
cycle.

ENVIRONMENT
-----------

M2_HOME should be added as an environment variable and point to the home installation of
Maven. M2_HOME/bin should be added to the PATH.

GigaSpaces comes with a maven installation which is located under tools/maven/apache-maven-3.0.2


INSTALLING GIGASPACES INTO MAVEN REPOSITORY
-------------------------------------------

Running installmavenrep.(sh/bat) will install GigaSpaces different jar files into Maven local 
repository (defaults to USER_HOME/.m2/repository).

NEXT STEPS
----------

Use 'mvn os:create' in order to create a project template. Once a certain project is created,
the project includes a readme.txt that explains how to run it.

USING OPENSPACES MAVEN PLUGIN
-----------------------------

(*) mvn os:create

  Creates a built in project that can be used to either show or use as a starting point
  for a GigaSpaces project. Running it without any parameters shows the different project 
  templates and how they can be used.
  
  -DartifactId: Controls the name of the project. Defaults to my-app.
  -DgroupId: Controls the package name. Defaults to com.mycompany.app.
  
  Once executed, cd into the [artifactId] directory in order to work within the 
  application. For example, run 'mvn test'.

(*) mvn compile os:run

  Runs the given project (all the modules that are marked as PU) without packaging using
  just the classpath and the compiled classes. Can be configured to run just a specific 
  processing unit module using -Dmodule=[module name] parameter.
  
(*) mvn package

  Not an OpenSpaces goal, but important as it actually ends up (as configured in the 
  different projects template) packaging the different processing units projects into a 
  valid structure and "jars" them up.

(*) mvn os:run-standalone

  Runs the given project (all the modules that are marked as PU) using the packaged 
  processing unit jars. Can be configured to run just a specific processing unit module using 
  -Dmodule=[module name] parameter.

(*) mvn os:deploy

  Deploys the packaged jars of the different processing unit modules into the Service Grid.
  Can be configured to deploy just a specific module using -Dmodule=[module name]. Can be
  configured to deploy using a specific lookup group using -Dgroups=[group name].

(*) mvn os:undeploy

  Undeploys the different processing units modules from the Service Grid. Can to configured
  to undeploy just a specific module using -Dmodule=[module name]. Can be configured to
  undeploy using a specific lookup groups using -Dgroups=[group name].