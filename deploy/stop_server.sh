#!/usr/bin/env bash
processId=$(ps -ef | grep vinci_server | grep -v grep | tr -s ' ' | cut -d ' ' -f2)

if [ -z "$processId" ]; then
    echo "No process is found"
else
    kill -9 $processId
    echo "Server is stopped"
fi
