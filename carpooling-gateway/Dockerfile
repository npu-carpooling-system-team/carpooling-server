FROM openjdk:17.0.7
LABEL authors="wangminan"
COPY target/carpooling-gateway-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63010
ENV TZ Asia/Shanghai
ENV JAVA_OPTS="-Dfile.encoding=UTF-8"
ENTRYPOINT java -jar ${JAVA_OPTS} /app.jar --spring.profiles.active=prod
