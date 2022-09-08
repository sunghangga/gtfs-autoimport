FROM openjdk:8-jdk-alpine
COPY target/autoimportgtfs-0.0.1-SNAPSHOT.jar gtfs-auto-import.jar
ENTRYPOINT ["java","-Xmx1G","-jar","gtfs-auto-import.jar"]