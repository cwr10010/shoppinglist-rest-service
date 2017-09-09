FROM openjdk:8

ENV HOME=/app
ENV APP_HOME=/app
ENV APP_USER=app
ENV APP_LOGS=$APP_HOME/logs
ENV TZ=Europe/Berlin

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN useradd -u 7777 --create-home -d $APP_HOME -s /bin/nologin -c "Docker image user" $APP_USER

ADD /build/libs $APP_HOME

WORKDIR $APP_HOME

USER $APP_USER
RUN rmdir $APP_LOGS && mkdir $APP_LOGS
RUN chmod -rf 777 $APP_LOGS
RUN chown -R $APP_USER:$APP_USER $APP_LOGS

VOLUME $APP_LOGS

CMD java -Dspring.datasource.username=${MYSQL_USER} -Dspring.datasource.password=${MYSQL_PASSWORD} -Dspring.datasource.url=${MYSQL_URL} -jar $APP_HOME/shoppinglist-rest-service-1.0-SNAPSHOT.jar
