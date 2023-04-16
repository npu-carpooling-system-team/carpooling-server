FROM openjdk:17.0.6
LABEL authors="wangminan"
COPY target/carpooling-auth-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63010
ENV TZ Asia/Shanghai
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]
