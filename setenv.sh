#!/bin/sh

CONFIG_DIR="/usr/local/tomcat/environment"
CONFIG_URL="file://${CONFIG_DIR}"
CATALINA_OPTS="${CATALINA_OPTS} -DconfigUrl=${CONFIG_URL}"

for i in ${CONFIG_DIR}/*; do
    if [ -d $i -a -f $i/setenv.sh ] ; then
        . $i/setenv.sh
    fi
done
