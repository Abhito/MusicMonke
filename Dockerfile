FROM eclipse-temurin:25-jre-jammy
COPY target/MusicMonke.jar /usr/app/
WORKDIR /usr/app
EXPOSE 8080

ARG TOKEN
ENV TOKEN=$TOKEN

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "MusicMonke.jar"]
