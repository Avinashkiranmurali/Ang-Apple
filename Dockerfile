# Use a minimal image as parent
#FROM --platform=linux/amd64 adoptopenjdk/openjdk11
FROM --platform=linux/amd64 adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.7_10

RUN apk upgrade --no-cache && \
    apk add --no-cache postgresql-client curl bash openssl libgcc libstdc++ ncurses-libs tzdata

#Add a non-root user tomcat
RUN addgroup -S tomcat && adduser -S tomcat -G tomcat

# Environment variables
ENV TOMCAT_MAJOR=9 \
    TOMCAT_VERSION=9.0.73 \
    CATALINA_HOME=/usr/local/tomcat \
    TZ="America/New_York"
    #DD_API_KEY=1a0bc2607e1a7d626bbe1671bfc2b637 \
    #DD_AGENT_MAJOR_VERSION=7 \
    #DD_LOGS_ENABLED=true

# Install tomcat
RUN curl -jkSL -o /tmp/apache-tomcat.tar.gz http://archive.apache.org/dist/tomcat/tomcat-${TOMCAT_MAJOR}/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz && \
    gunzip /tmp/apache-tomcat.tar.gz && \
    tar -C /usr/local -xf /tmp/apache-tomcat.tar && \
    ln -s /usr/local/apache-tomcat-$TOMCAT_VERSION $CATALINA_HOME && \
    rm -rf /usr/local/apache-tomcat-$TOMCAT_VERSION/webapps/ && \
    mkdir /usr/local/apache-tomcat-$TOMCAT_VERSION/environment && \
    mkdir /usr/local/apache-tomcat-$TOMCAT_VERSION/datadog

ENV LOGGING_CONFIG=/usr/local/tomcat/environment/logback.xml
COPY setenv.sh /usr/local/tomcat/bin/
#COPY dd-java-agent.jar /usr/local/apache-tomcat-$TOMCAT_VERSION/datadog/

#RUN bash -c "$(curl -L https://raw.githubusercontent.com/DataDog/datadog-agent/master/cmd/agent/install_script.sh)"
#RUN chmod -R 777 /etc/datadog-agent
#COPY conf.yaml /etc/datadog-agent/conf.d/tomcat.d/

# Deploy application onto tomcat server
ARG WAR_FILE=build/libs/apple-gr.war
COPY ${WAR_FILE} /usr/local/tomcat/webapps/apple-gr.war
VOLUME /usr/local/tomcat/environment/

ARG OAUTH_WAR_FILE=build/libs/auth.war
COPY ${OAUTH_WAR_FILE} /usr/local/tomcat/webapps/auth.war
VOLUME /usr/local/tomcat/environment/

RUN chown -R tomcat:tomcat /usr/local/apache-tomcat-$TOMCAT_VERSION
RUN chmod 755 /usr/local/apache-tomcat-$TOMCAT_VERSION/environment

#Run Container as tomcat (non-root user)
USER tomcat

EXPOSE 8080

ENTRYPOINT ["/usr/local/tomcat/bin/catalina.sh","run"]

WORKDIR $CATALINA_HOME
