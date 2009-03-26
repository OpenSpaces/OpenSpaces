#!/bin/sh

##############################################################################
##                                                                          ##
##  GS Shell script for UN*X                                                ##
##                                                                          ##
##############################################################################

GROOVY_APP_NAME=GShell
DIRNAME=`dirname "$0"`
GS_GROOVY_HOME=$DIRNAME/../groovy
CP=$DIRNAME/gshell.jar
. "$DIRNAME/../groovy/bin/startGroovy"

startGroovy org.openspaces.gshell.Main "$@"
