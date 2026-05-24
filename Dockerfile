FROM eclipse-temurin:25-jre-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    libstdc++6 \
    && rm -rf /var/lib/apt/lists/*

COPY target/MusicMonke.jar /usr/app/MusicMonke.jar

WORKDIR /usr/app
RUN mkdir -p /usr/app/tmp && chmod 1777 /usr/app/tmp

EXPOSE 8080

ARG TOKEN
ENV TOKEN=$TOKEN
ENV JAVA_IO_TMPDIR=/usr/app/tmp
ENV JAVA_TOOL_OPTIONS="-Djava.io.tmpdir=/usr/app/tmp"

ENTRYPOINT ["java", \
    "-Djava.io.tmpdir=/usr/app/tmp", \
    "-jar", "MusicMonke.jar"]
