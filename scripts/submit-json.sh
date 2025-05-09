#!/bin/bash
#
# Usage: CatalogId MerchantSimpleName FileName UsePlugin(Boolean 1/0)
#

function displayHelp {
  echo "Usage: CatalogId MerchantSimpleName FileName UsePlugin(Boolean 1/0)"
}

if [ -z $1 ]; then
  echo "CatalogId (String) is a required parameter"
  displayHelp
  exit -1
fi

if [ -z $2 ]; then
  echo "MerchantSimpleName (String) is a required parameter"
  displayHelp
  exit -1
fi

if [ -z $3 ]; then
  echo "FileName (String) is a required parameter"
  displayHelp
  exit -1
fi

if [ -z $4 ]; then
  echo "UsePlugin (Boolean 1/0) is a required parameter"
  displayHelp
  exit -1
fi

# Build the ENDPOINT (Determines whether we use the plugin or not)
if [ $4 == "1" ]; then
  PURGE=true
else
  PURGE=false
fi

## Archive old output files for 7 days
#Move all .out files for failed loads to ./output/failed
outputFiles=( $(find . -maxdepth 1 -name "*.out") )

for i in ${outputFiles[@]}; do
  count=$(grep -o 'failed with error' $i | wc -l)
  if [ $count -ge 4 ]; then
    mv $i ./output/failed/
  fi
done

# Move all remaining output files into ./output
find . -maxdepth 1 -name "*.out.csv" -exec mv {} ./output/ \;
find . -maxdepth 1 -name "*.out" -exec mv {} ./output/ \;
# Prune anything older than 7 days
find /usr/local/dropbox/output -mtime +7 -exec rm -f {} \;

# Find the file to load by listing the candidates backwards and loading only the most recent match
FILE=`ls -t *$3*.gz | head -n 1`

# Build the url and kick off the load
echo "Loading $FILE"
CURL_PATH="$ENDPOINT?fileName=$FILE"
#CURL_RESULT=$(curl -X PUT $CURL_PATH)
#curl -X PUT $CURL_PATH
curl -X PUT -H "Content-Type: application/json;charset=UTF-8" --data "{ \"importFile\": \"$FILE\",\"startFrom\": 0,\"jobType\": \"IMPORT\",\"purgeMerchantsExistingProducts\": $PURGE }" http://localhost:8080/maintenance/job


