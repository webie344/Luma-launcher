#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi

if ! command -v "$JAVA" >/dev/null 2>&1; then
    echo "Java 17 is required. Install a JDK and set JAVA_HOME." >&2
    exit 1
fi

exec "$JAVA" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
