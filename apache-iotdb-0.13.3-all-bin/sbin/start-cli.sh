#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

# You can put your env variable here
# export JAVA_HOME=$JAVA_HOME

if [ -z "${IOTDB_HOME}" ]; then
  export IOTDB_HOME="$(cd "`dirname "$0"`"/..; pwd)"
fi

if [ -z "${IOTDB_CLI_CONF}" ]; then
  IOTDB_CLI_CONF=${IOTDB_HOME}/conf
fi

MAIN_CLASS=org.apache.iotdb.cli.Cli


CLASSPATH=""
for f in ${IOTDB_HOME}/lib/*.jar; do
  CLASSPATH=${CLASSPATH}":"$f
done


if [ -n "$JAVA_HOME" ]; then
    for java in "$JAVA_HOME"/bin/amd64/java "$JAVA_HOME"/bin/java; do
        if [ -x "$java" ]; then
            JAVA="$java"
            break
        fi
    done
else
    JAVA=java
fi

PARAMETERS="$@"

# if [ $# -eq 0 ]
# then
# 	PARAMETERS="-h 127.0.0.1 -p 6667 -u root -pw root"
# fi

# Added parameters when default parameters are missing

# sh version
case "$PARAMETERS" in
*"-pw "*) PARAMETERS=$PARAMETERS ;;
*            ) PARAMETERS="-pw root $PARAMETERS" ;;
esac
case "$PARAMETERS" in
*"-u "*) PARAMETERS=$PARAMETERS ;;
*            ) PARAMETERS="-u root $PARAMETERS" ;;
esac
case "$PARAMETERS" in
*"-p "*) PARAMETERS=$PARAMETERS ;;
*            ) PARAMETERS="-p 6667 $PARAMETERS" ;;
esac
case "$PARAMETERS" in
*"-h "*) PARAMETERS=$PARAMETERS ;;
*            ) PARAMETERS="-h 127.0.0.1 $PARAMETERS" ;;
esac

# echo $PARAMETERS

set -o noglob
iotdb_cli_params="-Dlogback.configurationFile=${IOTDB_CLI_CONF}/logback-cli.xml"
exec "$JAVA" $iotdb_cli_params -cp "$CLASSPATH" "$MAIN_CLASS" $PARAMETERS

exit $?
