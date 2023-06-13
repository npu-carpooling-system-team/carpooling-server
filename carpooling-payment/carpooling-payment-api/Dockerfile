FROM openjdk:17.0.2
LABEL authors="wangminan"
COPY target/carpooling-payment-api-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63060
ENV TZ Asia/Shanghai
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dspring.profiles.active=prod -Dxxl.job.executor.ip=60.204.153.158 -Dxxl.job.executor.port=63065 -Dxxl.job.executor.appname=carpooling-payment-service"
ENTRYPOINT java ${JAVA_OPTS} -jar /app.jar
