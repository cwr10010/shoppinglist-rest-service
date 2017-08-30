FROM java:8
ADD /build/libs /app
WORKDIR /app
CMD java -Dspring.datasource.username=${MYSQL_USER} -Dspring.datasource.password=${MYSQL_PASSWORD} -Dspring.datasource.url=${MYSQL_URL} -jar shoppinglist-rest-service-1.0-SNAPSHOT.jar

