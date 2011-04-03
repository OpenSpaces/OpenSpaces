Alert Logging Gateway Example


GENERAL DESCRIPTION:
--------------------

The Alert interface exposes the XAP application's health state, 
and allows user to register listeners on one or more alert types.

This allows for custom integration of third party monitoring 
products with XAP alerting system.

A recommended a simple use case for integration (described below)
is using a listener that writes the chosen types of alerts to a 
log (using log4j or commons-logging).

The main advantage of using this approach is the ability 
to use an extensive set of out-of-box log appenders that 
translates log messages into different protocols and APIs 
to be consumed by third party products.



WIKI PAGE
---------------------------------

For more information about the Alert Logging Gateway Example please refer to:
http://www.gigaspaces.com/wiki/display/XAP8/SNMP+Connectivity+via+Alert+Logging+Gateway
