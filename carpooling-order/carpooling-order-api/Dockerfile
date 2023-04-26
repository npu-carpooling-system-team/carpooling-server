FROM openjdk:17.0.6
LABEL authors="wangminan"
COPY target/carpooling-order-api-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63050
ENV TZ Asia/Shanghai
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=prod -Dxxl.job.excutor.ip=60.204.153.158 -Dxxl.job.excutor.port=63055"
ENTRYPOINT java ${JAVA_OPTS}  -jar /app.jar
