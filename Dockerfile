FROM openjdk:8

ENV HOME=/app
ENV APP_HOME=/app
ENV APP_USER=app
ENV TZ=Europe/Berlin

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN useradd --create-home -d $APP_HOME -s /bin/nologin -c "Docker image user" $APP_USER

ADD /build/libs $APP_HOME

WORKDIR $APP_HOME

USER $APP_USER
RUN mkdir $APP_HOME/logs
RUN chown $APP_USER:$APP_USER $APP_HOME/logs

CMD java -Dspring.datasource.username=${MYSQL_USER} -Dspring.datasource.password=${MYSQL_PASSWORD} -Dspring.datasource.url=${MYSQL_URL} -jar $APP_HOME/shoppinglist-rest-service-1.0-SNAPSHOT.jar
