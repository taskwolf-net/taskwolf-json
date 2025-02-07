FROM alpine

COPY /build/libs/json-1.0.0-SNAPSHOT.jar json.jar
COPY /locale/ /locale/