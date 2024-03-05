# base build image
FROM maven:3.6-openjdk-17 AS maven

# copy the project files
COPY ./pom.xml ./pom.xml

# build all dependencies
RUN mvn dependency:go-offline --batch-mode

# copy source files
COPY ./src ./src

# build for release
RUN mvn package --batch-mode --quiet -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Djacoco.skip=true

# final base image
FROM eclipse-temurin:17-jre-jammy

# Build-time metadata as defined at https://github.com/opencontainers/image-spec/blob/master/annotations.md
LABEL org.opencontainers.image.title="OUIDB to JSON publisher" \
      org.opencontainers.image.description="Converts the OUI Database to Json and publish to GIT repo" \
      org.opencontainers.image.source="https://github.com/jfisbein/ouidb-to-json-publisher"

COPY --from=maven target/ouidb-to-json-publisher-jar-with-dependencies.jar /
ENV DATA='/var/data'

CMD java -jar /ouidb-to-json-publisher-jar-with-dependencies.jar
