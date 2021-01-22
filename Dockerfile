# For docker hub automated build only

FROM openjdk:8 as builder
ADD . /mariocash/
RUN \
    cd /mariocash && \
    ./gradlew clean build -x test && \
    mv /zhihexireng/mariocash-node/build/libs/*.jar /app.jar

FROM openjdk:8-jre-alpine
MAINTAINER MARIOCASH
ENV SPRING_PROFILES_ACTIVE=prod \
    RUN_SLEEP=0 \
    JAVA_OPTS=""
EXPOSE 8080 32918
VOLUME /.mariocash
CMD echo "The MarioCash Node will start in ${RUN_SLEEP}s..." && \
    sleep ${RUN_SLEEP} && \
    java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar

COPY --from=builder /app.jar .
