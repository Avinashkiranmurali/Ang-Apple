#!/bin/bash

#rsync -rlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@ps01.apluat.bridge2solutions.net:/usr/local/dropbox/*.gz /usr/local/dropbox

rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-hk-en*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-hk-zh*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-ph-en*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-tw-zh*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-mx-es*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-sg-en*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-us-en*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-ca-en*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-ca-fr*.gz /usr/local/dropbox
rsync -rvlptz --rsh="ssh -i /home/tomcat/.ssh/id_rsa" tomcat@gretl01.aplqa1.bridge2solutions.net:/usr/local/gretl/data/gretl-product-feeds/clients/apple/*apple-pc-fullFeed-gb-en*.gz /usr/local/dropbox

# Delete any load files older than 7 days ago
find /usr/local/dropbox/*.gz -daystart -mtime +7 -exec rm -f {} \;
