# base build image
FROM maven:3.6-jdk-11 as maven

# copy the project files
COPY ./pom.xml ./pom.xml

# build all dependencies
RUN mvn dependency:go-offline --batch-mode

# copy source files
COPY ./src ./src

# build for release
RUN mvn package --batch-mode --quiet -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Djacoco.skip=true

# final base image
FROM openjdk:11-jre-slim

COPY --from=maven target/ouidb-to-json-publisher-jar-with-dependencies.jar /
ENV DATA='/var/data'

CMD java -jar /ouidb-to-json-publisher-jar-with-dependencies.jar
