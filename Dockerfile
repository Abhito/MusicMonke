FROM alpine:3.16
RUN apk add --no-cache openjdk11
COPY target/MusicMonke-1.3.1.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080

# Use build argument for the token
ARG TOKEN
ENV TOKEN=$TOKEN

ENTRYPOINT ["java", "-jar", "MusicMonke-1.3.1.jar"]
