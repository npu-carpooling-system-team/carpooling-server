FROM openjdk:17.0.7
LABEL authors="wangminan"
COPY target/carpooling-payment-api-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63060
ENV TZ Asia/Shanghai
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dxxl.job.excutor.ip=60.204.153.158 -Dxxl.job.excutor.port=63065"
ENTRYPOINT java -jar ${JAVA_OPTS} /app.jar --spring.profiles.active=prod
