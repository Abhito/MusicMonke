FROM eclipse-temurin:25-jre-jammy
COPY target/MusicMonke.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080

ARG TOKEN
ENV TOKEN=$TOKEN
ENV JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED"

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "MusicMonke.jar"]
