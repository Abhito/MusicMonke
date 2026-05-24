FROM eclipse-temurin:25-jdk-jammy AS native-extract

COPY target/MusicMonke.jar /build/MusicMonke.jar
WORKDIR /build

RUN ARCH=$(uname -m) && \
    case "$ARCH" in \
      x86_64)  PLATFORM="linux-x86-64" ;; \
      aarch64) PLATFORM="linux-aarch64" ;; \
      *) echo "Unsupported architecture: $ARCH" && exit 1 ;; \
    esac && \
    NATIVE_JAR=$(jar tf MusicMonke.jar | grep "BOOT-INF/lib/jdave-native-${PLATFORM}" | head -1) && \
    jar xf MusicMonke.jar "$NATIVE_JAR" && \
    jar xf "$NATIVE_JAR" "natives/${PLATFORM}/libdave.so" && \
    install -Dm755 "natives/${PLATFORM}/libdave.so" /out/libdave.so

FROM eclipse-temurin:25-jre-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    libstdc++6 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=native-extract /out/libdave.so /usr/lib/libdave.so
COPY target/MusicMonke.jar /usr/app/MusicMonke.jar

WORKDIR /usr/app
RUN mkdir -p /usr/app/tmp && chmod 1777 /usr/app/tmp

EXPOSE 8080

ARG TOKEN
ENV TOKEN=$TOKEN
ENV JAVA_IO_TMPDIR=/usr/app/tmp
ENV JAVA_TOOL_OPTIONS="--enable-native-access=ALL-UNNAMED -Djdave.library.path=/usr/lib/libdave.so -Djava.io.tmpdir=/usr/app/tmp"

ENTRYPOINT ["java", \
    "--enable-native-access=ALL-UNNAMED", \
    "-Djdave.library.path=/usr/lib/libdave.so", \
    "-Djava.io.tmpdir=/usr/app/tmp", \
    "-jar", "MusicMonke.jar"]
