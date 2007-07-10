===============================
=== OpenSpaces Data Example ===
===============================

@author kimchy

1. MOTIVATION

The data example is an abstract example showing off most of OpenSpaces features, namely:

- The definition of two processing units, a processor and a feeder, sharing the same domain model.
- Definition of a Space within the processing unit, with different schemas and SLA.
- Show cases for OpenSpaces Events module, including the polling container and notify container.
- Usage of OpenSpaces Remoting.
- Usage of GigaSpaces local view within a the processing unit.

2. STRUCTURE

The example has three modules:

	a. The Common module includes the domain model and a data processor interface that are shared between
	    the feeder and the processor modules.
	b. The Processor module is a processing unit with the main task of processing unprocessed data objects.
	    The processing of data objects is done both using event container and remoting.
	c. The Feeder module is a processing unit (though it does not have to be one) that feeds unprocessed
	    data objects which are in turn processed by the processor module. It feeds unprocessed data objects
	    both by directly writing them to the space and by using OpenSpaces Remoting.
	    
3. BUILD AND DEPLOYMENT

The example uses ant as its build tool and uses a standard build.xml file. It comes with
a build script that runs ant automatically. Running the build script with no parameters within
the current directory will list all the relevant tasks that can be run with this example.

Running 'build.(sh/bat) build' will compile all the different modules. In case of the Processor
and Feeder modules, it will compile the classes directly into their respective PU structure.

Running 'build.(sh/bat) dist' will finalize the processing unit structure of both the Processor
and the Feeder by copying the Common module jar file into the 'shared-lib' directory within the 
processing unit structure. In case of the processor module, it will copy the jar file to
'processor/pu/data-processpr/shared-lib', and will make 'processor/pu/data-processor' a ready
to use processing unit.

In order to deploy the data example onto the Service Grid, a GSM and *two* GSCs will need to be
started (note, we need two GSCs because of the SLA defined within the processor module). Next,
'build.(sh/bat) deploy-local-processor' will need to be executed. The task will copy the processor 
processing unit directory under the 'deploy' directory located under the root of the product, and 
then run the deploy command against the running GSM. Run the GS-UI in order to see the 4 PU instances
deployed (two partitions, each with one backup). Once it has been deployed, the Feeder module can
be deployed using 'build.(sh/bat) deploy-local-feeder'. This will cause the feeder to be deployed
into one of the GSC and start feeding unprocessed data into the two processing units.

Another option to deploy the example can be using the GS CLI using the pudeploy option. An interesting
example of externally providing the SLA that applies to the deployed processing unit can be running
'gs.(sh/bat) pudeploy -sla ../examples/openspaces/data/partitioned-sla.xml data-processor'. This allows to 
deploy the data-processor example using a partitioned space (and not a partitioned-sync2backup) which
is defined in the pu.xml. In order to run the feeder using the GS CLI please execute 'gs.(sh/bat) pudeploy data-feeder'.

Some ways to play with the examples can be:

1. Start another GSC and relocate (click and drag on GS-UI) the feeder to the other GSC. This will
simplify the output since on the GSC that used to run both a processor and a feeder, you will only
have the processor now. And on the new GSC, you will see the feeder.
2. Kill one of the GSC that runs the Processor processing unit. Thanks to the SLA, each GSC will run
a primary partition, and a backup partition (of the other primary partition). This means that the 
feeder should keep on running, and the active GSC should have its backup partition space turn into
a primary one. While this is happening, the two other instances of the processor PU will get relocated
to the GSC that is running the Feeder PU.


