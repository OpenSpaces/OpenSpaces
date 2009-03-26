#!/bin/sh

##############################################################################
##                                                                          ##
##  GS Shell script for UN*X                                                ##
##                                                                          ##
##############################################################################

GROOVY_APP_NAME=GShell
DIRNAME=`dirname "$0"`
GS_GROOVY_HOME=$DIRNAME/../groovy
CP=$DIRNAME/groovy
. "$DIRNAME/../groovy/bin/startGroovy"

startGroovy groovy.ui.GroovyMain $DIRNAME/groovy/org/openspaces/gshell/Main.groovy "$@"
