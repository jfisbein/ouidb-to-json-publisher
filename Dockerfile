# our base build image
FROM maven:3.5-jdk-8 as maven

# copy the project files
COPY ./pom.xml ./pom.xml

# build all dependencies
RUN mvn dependency:go-offline --batch-mode

# copy your other files
COPY ./src ./src

# build for release
RUN mvn clean package --batch-mode

# our final base image
FROM openjdk:8-jre-alpine

COPY --from=maven target/ouidb-to-json-publisher-jar-with-dependencies.jar /

ENTRYPOINT ["sh", "-c", "java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -jar /ouidb-to-json-publisher-jar-with-dependencies.jar '/var/data' \"${REPO_URL}\" \"${REPO_USERNAME}\" \"${REPO_PASSWORD}\""]