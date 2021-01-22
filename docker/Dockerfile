FROM openjdk:8-jre-alpine
MAINTAINER MARIOCASH
ENV SPRING_PROFILES_ACTIVE=prod \
    RUN_SLEEP=0 \
    JAVA_OPTS=""
EXPOSE 8080 32918
VOLUME /.mariocash
ARG JAR_FILE
CMD echo "The MarioCash node will start in ${RUN_SLEEP}s..." && \
    sleep ${RUN_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar

COPY ${JAR_FILE} app.jar
