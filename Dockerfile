FROM java:8
ADD /build/libs /app
WORKDIR /app
CMD java -jar shoppinglist-rest-service-1.0-SNAPSHOT.jar -Ddb.passwd=${MYSQL_PASSWORD}
