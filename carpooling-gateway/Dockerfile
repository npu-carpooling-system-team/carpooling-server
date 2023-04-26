FROM openjdk:17.0.7
LABEL authors="wangminan"
COPY target/carpooling-gateway-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63010
ENV TZ Asia/Shanghai
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=prod"
ENTRYPOINT java ${JAVA_OPTS}  -jar /app.jar
