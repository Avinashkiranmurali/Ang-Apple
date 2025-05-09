pipeline {
  agent { label 'pipeline-nodes' }
  options { timestamps () }
  environment {
    APPLICATION_WORKSPACE = sh(returnStdout: true, script: 'pwd').trim()
    PARAMETERS_LIST = sh(returnStdout: true, script: 'env | egrep "GERRIT_BRANCH|GERRIT_CHANGE_ID|GERRIT_CHANGE_NUMBER|GERRIT_CHANGE_OWNER_NAME|GERRIT_CHANGE_SUBJECT|GERRIT_CHANGE_URL|GERRIT_EVENT_TYPE|GERRIT_PATCHSET_NUMBER|GERRIT_PATCHSET_REVISION|GERRIT_PATCHSET_UPLOADER_NAME|GERRIT_PROJECT|GERRIT_TOPIC" | awk \'{gsub(/ /,"__")}1\'| awk \'{gsub(/=/," ")}1\' |awk \'BEGIN{print "<table> <style> table, th, td { border: 1px solid black; border-collapse: collapse; } </style>"} {print "<tr>";for(i=1;i<=NF;i++)print "<td>" $i"</td>";print "</tr>"} END{print "</table>"}\'| awk \'{gsub(/__/," ")}1\' ').trim()
    IMAGE_NAME = 'adoptopenjdk/openjdk11:x86_64-alpine-jdk-11.0.7_10'
  }
  stages {
    stage('Verify released') {
      steps{
        echo "Verify if the application version is already released"
        script {
          try {
            echo sh (returnStdout: true, script: '''#!/bin/bash
            set +e
            LATEST_JAR_VERSION=$(cat gradle.properties| grep "version="|cut -d= -f2|cut -d- -f1)
            if [[ $LATEST_JAR_VERSION == *'SNAPSHOT'* ]]; then
              LATEST_JAR_VERSION=$(echo ${LATEST_JAR_VERSION}| sed 's/-SNAPSHOT//g')
            fi
            echo $LATEST_JAR_VERSION
            #Before Stage build, Check if the pom version has been released already to Platform-released
            python2 ${JENKINS_SCRIPTS_DIR}/verify_released.py -r 'platform-released' -g 'apple-gr' -a 'apple-gr' --version "${LATEST_JAR_VERSION}"
            rc=$?;
            if [[ $rc != 0 ]]; then
              exit $rc;
            fi
            ''')
          } catch (exc) {
            echo "Application version is already released"
            currentBuild.result = 'FAILED'
            sh 'exit -1'
          }
        }
      }
    }
    stage('Create Build and Run Unit Test Cases') {
      agent{
        docker{
          image '${IMAGE_NAME}'
          registryUrl 'http://nexus02.cp.bridge2solutions.net:8083'
          registryCredentialsId '81f6a892-6c9f-4373-80a1-a9af1358d8a2'
          label 'pipeline-nodes'
          args '-v $HOME/.gradle:/usr/share/jenkins/.gradle --net=host'
          reuseNode true
        }
      }
      steps {
        echo "Compiling code"
        script {
          try {
            env.LATEST_JAR_VERSION = sh(returnStdout: true, script: 'cat gradle.properties| grep "version="|cut -d= -f2|cut -d- -f1').trim()
            env.VERSION = "${LATEST_JAR_VERSION}-${BUILD_NUMBER}"
            sh 'java -version'
            sh 'chmod +x gradlew'
            sh './gradlew build -Pbranch=${GERRIT_BRANCH} -Pversion=${VERSION}'
          } catch (exc) {
            echo "Create build stage failed with exceptions"
            currentBuild.result = 'FAILED'
            sh 'exit -1'
          }
        }
      }
    }
    stage('Create Artifact Master') {
      when { environment ignoreCase: true, name: 'GERRIT_EVENT_TYPE', value: 'change-merged' }
      agent{
        docker{
          image '${IMAGE_NAME}'
          registryUrl 'http://nexus02.cp.bridge2solutions.net:8083'
          registryCredentialsId '81f6a892-6c9f-4373-80a1-a9af1358d8a2'
          label 'pipeline-nodes'
          args '-v $HOME/.gradle:/usr/share/jenkins/.gradle --net=host'
          reuseNode true
        }
      }
      steps{
        script {
          try {
            echo "Artifact creation step"
            sh './gradlew -Pversion=${VERSION} -PuploadRepoKey=releases publish -x test -PbranchName=${GERRIT_BRANCH}'
          } catch (exc) {
            echo "Artifact creation step failed"
            currentBuild.result = 'FAILED'
            sh 'exit -1'
          }
        }
      }
    }
    stage('Publish SonarQube Report') {
      when { environment ignoreCase: true, name: 'GERRIT_EVENT_TYPE', value: 'change-merged' }
      agent{
        docker{
          image '${IMAGE_NAME}'
          registryUrl 'http://nexus02.cp.bridge2solutions.net:8083'
          registryCredentialsId '81f6a892-6c9f-4373-80a1-a9af1358d8a2'
          label 'pipeline-nodes'
          args '-v $HOME/.gradle:/usr/share/jenkins/.gradle --net=host'
          reuseNode true
        }
      }
      steps {
        script {
          try {
            echo "Running sonarqube scan"
            withSonarQubeEnv('sonar-6.7.6') {
              sh './gradlew -Psonar.analysis.mode=publish sonarqube sonarqube_ui -x test'
            }
          } catch (exc) {
            echo "Running sonarqube scan failed"
            currentBuild.result = 'FAILED'
            sh 'exit -1'
          }
        }
      }
    }
  }
  post {
      success {
        cleanWs()
      }
      failure {
        mail bcc: '', body: "PROJECT: ${env.JOB_NAME} <br>BUILD NUMBER: ${env.BUILD_NUMBER} <br> BUILD URL: ${env.BUILD_URL} <br> <br> <u>ADDITIONAL JOB PARAMETERS:</u> <br> ${PARAMETERS_LIST} ", cc: '', charset: 'UTF-8', from: 'no-reply-jenkins-alerts@bakkt.com', mimeType: 'text/html', replyTo: '', subject: "${env.JOB_NAME} ${currentBuild.result} - Build number: ${env.BUILD_NUMBER}", to: "Apple_Dev@bakkt.com"
      }
      changed {
          echo "Build result changed ${currentBuild.result}"
          script {
              if(currentBuild.result == 'SUCCESS') {
                  echo 'Build has changed to SUCCESS status'
                  mail bcc: '', body: "PROJECT: ${env.JOB_NAME} <br>BUILD NUMBER: ${env.BUILD_NUMBER} <br> BUILD URL: ${env.BUILD_URL} <br> <br> <u>ADDITIONAL JOB PARAMETERS:</u> <br> ${PARAMETERS_LIST} ", cc: '', charset: 'UTF-8', from: 'no-reply-jenkins-alerts@bakkt.com', mimeType: 'text/html', replyTo: '', subject: "${env.JOB_NAME} ${currentBuild.result} again - Build number: ${env.BUILD_NUMBER}", to: "Apple_Dev@bakkt.com"
              }
          }
      }
  }
}
