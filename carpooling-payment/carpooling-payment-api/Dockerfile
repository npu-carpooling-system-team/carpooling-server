FROM openjdk:17.0.7
LABEL authors="wangminan"
COPY target/carpooling-payment-api-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63060
ENV TZ Asia/Shanghai
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=prod -Dxxl.job.excutor.ip=60.204.153.158 -Dxxl.job.excutor.port=63065"
ENTRYPOINT java ${JAVA_OPTS} -jar /app.jar --spring.profiles.active=prod
