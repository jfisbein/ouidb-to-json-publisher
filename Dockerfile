FROM openjdk:8

COPY target/ouidb-to-json-publisher-jar-with-dependencies.jar /

ENTRYPOINT ["sh", "-c", "java -jar /ouidb-to-json-publisher-jar-with-dependencies.jar '/var/data' \"${REPO_URL}\" \"${REPO_USERNAME}\" \"${REPO_PASSWORD}\""]