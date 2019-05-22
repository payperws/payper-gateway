FROM openjdk:11-jdk-stretch
VOLUME /tmp
COPY target/gateway-1.0.0-SNAPSHOT.jar gateway.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/gateway.jar"]
