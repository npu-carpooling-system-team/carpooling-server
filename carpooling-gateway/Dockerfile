FROM openjdk:17.0.6
COPY target/carpooling-gateway-1.0-SNAPSHOT.jar /app.jar
EXPOSE 63010
ENV TZ Asia/Shanghai
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]
