#!/bin/bash
#
# This script provides the command and control utility for the 
# GigaSpaces Technologies gs-memcached script.
# The gs-memcached script starts a memcached agent.

MEMCACHED_URL=$1
if [ "${MEMCACHED_URL}" = "" ] ; then 
  MEMCACHED_URL=/./memcached
fi

"`dirname $0`/puInstance.sh" -properties space embed://url=${MEMCACHED_URL} "`dirname $0`/../deploy/templates/memcached"
