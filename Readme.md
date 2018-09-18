# oui-to-json-publisher
[![CircleCI](https://circleci.com/gh/jfisbein/ouidb-to-json-publisher.svg?style=svg)](https://circleci.com/gh/jfisbein/ouidb-to-json-publisher)

Simple tool that does the following steps:

+ Clones specified git repo.
+ Downloads latest oui-db from https://linuxnet.ca/ieee/oui/
+ Parses and converts it to json.
+ If the json file has any changes from the one in the repo, it uploads it to the git repo.

### Usage

#### Directly
`java -jar ouidb-to-json-publisher-jar-with-dependencies.jar {DATA_FOLDER} {REMOTE_REPO_URI} {REMOTE_REPO_USERNAME} {REMOTE_REPO_PASSWORD}`

#### Using docker
```
docker run -e "REPO_URL={THE_REPO_TO_UPLOAD_THE_JSON_FILE}" \
 -e "REPO_USERNAME={YOUR_GIT_USERNAME}" \
 -e "REPO_PASSWORD={YOUR_REPO_PASSWORD}" \
 jfisbein/ouidb-to-json-publisher
 ```
 
 ##### Example
For GitHub:
 ```
docker run -e "REPO_URL=https://github.com/jfisbein/ouidb-json" \
 -e "REPO_USERNAME={YOUR_GITHUB_USER_TOKEN}" \
 jfisbein/ouidb-to-json-publisher
 
 ```
 note: on GitHub, when using a GitHub user token, you don't have to set the password.