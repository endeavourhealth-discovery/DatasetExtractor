#!/bin/bash
. /datagenerator/setenv.sh

if [ $# -eq 0 ] ; then
     REPORT_NAME="core"
  else
     REPORT_NAME=$1
fi
logger "report-generator.sh called for report $REPORT_NAME"

java -Dlogback.configurationFile=/datagenerator/reports/config/logback.xml -jar /datagenerator/reports/exec/ReportGenerator-1.0-SNAPSHOT.jar $REPORT_NAME
