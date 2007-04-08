======================================
=== OpenSpaces Hello World Example ===
======================================

@author kimchy

1. MOTIVATION

This simple OpenSpaces application illustrates a couple of OpenSpaces core features, namely:

- Introduction to the Processing Unit concept, which uses Spring as its container.
- The definition of a Space within a processing unit.
- The definition of the GigaSpace simplified interface based on a Space.
- The definition of a simple bean that interacts with the Space using the GigaSpace API.

3. BUILD AND DEPLOYMENT

The example uses ant as its build tool and uses a standard build.xml file. Running ant with
no parameters within the current directory will list all the relevant tasks that can be run
with this example.

Running 'ant build' will compile the source code into the 'pu/helloworld' directory. The 
'pu/helloworld' directory already follows the formal structure of a Processing Unit. This 
means that all the classes will reside under the root of the processing unit. Additional
libraries will be either under 'lib' or 'shared-lib' directory. The Spring configuration
file will be under META-INF/spring and is called pu.xml.

Running 'ant dist' will finalize the Processing Unit structure by copying the commons math
jar file into the 'pu/helloworld/lib' directory thus creating a self sufficient deployable
unit. Note, in this simple example, the commons math library resides under the lib directory
and not the shared-lib directory.

There are two options running/deploying the processing unit example. The first is by using
the puInstance script located under the bin directory of the product installation. In order
to use it please navigate to the bin directory and execute: 
'puInstance -pu ../examples/opensapces/helloworld/pu/helloworld'. This will start a standalone
processing unit container that will run the processing unit.

Another option for deploying the hello world example is to use the Service Grid. The ant build
file comes with an example of how to define a macro allowing to deploy a processing unit to
the Service Grid. In order to deploy the processing unit, please start a single gsm and a single
gsc (located under the bin directory) and then execute 'and deploy-local-helloworl'. The taks
will copy the processing unit directory under the 'deploy' directory located under the root
of the product, and then run the deploy command against the running GSM.

For more advance example, including different cluster topologies, please see the data example.