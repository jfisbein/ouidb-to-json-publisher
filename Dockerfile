# base build image
FROM maven:3.6-jdk-11 as maven

# copy the project files
COPY ./pom.xml ./pom.xml

# build all dependencies
RUN mvn dependency:go-offline --batch-mode

# copy source files
COPY ./src ./src

# build for release
RUN mvn package --batch-mode

# final base image
#FROM openjdk:8-jre-alpine
FROM openjdk:11-jre-slim

COPY --from=maven target/ouidb-to-json-publisher-jar-with-dependencies.jar /

ENTRYPOINT ["sh", "-c", "java -jar /ouidb-to-json-publisher-jar-with-dependencies.jar '/var/data' \"${REPO_URL}\" \"${REPO_USERNAME}\" \"${REPO_PASSWORD}\""]
