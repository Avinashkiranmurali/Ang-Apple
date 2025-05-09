#!/bin/bash


# Test Run:
#       $ JMETER_HOME=/Users/vprasanna/Development/Workspace/apple/jmeter/apache-jmeter-5.1.1 ./run.sh uat
# or
#       $ export JMETER_HOME=/Users/vprasanna/Development/Workspace/apple/jmeter/apache-jmeter-5.1.1
#       $ ./run.sh bf

if [ -z ${JMETER_HOME+x} ]; then
    echo "JMETER_HOME is not set";
    echo "Please set the variable like export JMETER_HOME=/usr/local/jmeter";
    exit 1
else
    echo "JMETER_HOME is set to '$JMETER_HOME'";
fi

ENVIRONMENT=$1
FILE_NAME=Pricing-API-Test-UAT.jmx

case $ENVIRONMENT in
  (bf)
	FILE_NAME=Pricing-API-Test-BF.jmx
	   ;;
   (uat)
        FILE_NAME=Pricing-API-Test-UAT.jmx
           ;;
   (*)
      echo "$0 bf|uat"
      echo "Defaulting to UAT"
    echo "Example:"
    echo "$0 uat"
esac

#./apache-jmeter-5.1.1/bin/jmeter -n -t $FILE_NAME
echo "executing $JMETER_HOME/bin/jmeter -n -t $FILE_NAME"

$JMETER_HOME/bin/jmeter -n -t $FILE_NAME


