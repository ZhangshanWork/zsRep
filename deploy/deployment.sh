#!/usr/bin/env bash
echo "deployment.sh been executed!!!! $(date '+%Y-%m-%d %H:%M:%S')">>./trace.log
if [  $# -ne 2 -a $# -ne 3 ]; then
    echo "The num of arguments is not correct"
    echo "usage: deploy.sh [projectVersion] [env] [specified_git_repository]"
    echo "projectVersion----the version of the project"
    echo "env----the deployment environment"
    echo "specified_git_repository----the git repository name is required when the env value is intg"
    exit 1
fi

PROJECT_VERSION=$1
VINCI_ENV=$2
echo "project_version:${PROJECT_VERSION},VINCI_ENV:${VINCI_ENV}"

echo "Build the project"
/opt/gradle/bin/gradle --project-dir=/opt/develop/vinci/vinci-server clean build -PprojVersion=${PROJECT_VERSION}

echo "Change the project to be executable"
chmod 755 /opt/develop/vinci/vinci-server/build/libs/vinci_server-${PROJECT_VERSION}.jar

processId=$(ps -ef | grep vinci_server | grep -v grep | tr -s ' ' | cut -d ' ' -f2)

if [ ! -z "$processId" ]; then
    echo "Stopping server"
    kill -9 $processId
fi

VINCI_SERVER_STARTUP_LOG=/var/log/nlp/vinci_server.log
echo "Starting server"
echo "java_home is ${JAVA_HOME}"
nohup ${JAVA_HOME}/bin/java -jar -Dspring.profiles.active=${VINCI_ENV} /opt/develop/vinci/vinci-server/build/libs/vinci_server-${PROJECT_VERSION}.jar -XX:+PrintGCDetails \
-Xms1g -Xmx1g -XX:MaxTenuringThreshold=5 -XX:+UseConcMarkSweepGC -XX:CMSFullGCsBeforeCompaction=5 im.vinci.VinciApplication >> ${VINCI_SERVER_STARTUP_LOG} 2>&1 &
echo "Server started"

exit 0
