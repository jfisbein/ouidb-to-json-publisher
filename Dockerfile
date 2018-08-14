FROM maven:3.5-jdk-8-alpine

# COPY target/ouidb-to-json-publisher-jar-with-dependencies.jar /
ADD . /src
RUN cd src && mvn clean package && mv target/ouidb-to-json-publisher-jar-with-dependencies.jar / && rm /root/.m2

ENTRYPOINT ["sh", "-c", "java -jar /ouidb-to-json-publisher-jar-with-dependencies.jar '/var/data' \"${REPO_URL}\" \"${REPO_USERNAME}\" \"${REPO_PASSWORD}\""]