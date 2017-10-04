FROM openjdk:8-alpine

ENV HOME=/app
ENV APP_HOME=/app
ENV APP_USER=app
ENV APP_LOGS=$APP_HOME/logs
ENV APP_CONFIG=$APP_HOME/config
ENV TZ=Europe/Berlin

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN adduser -D -u 7777 -h $APP_HOME -s /bin/nologin $APP_USER
RUN apk --update add sudo

ADD /build/libs $APP_HOME

WORKDIR $APP_HOME

RUN mkdir $APP_LOGS
RUN chmod -R 770 $APP_LOGS
RUN chown -R $APP_USER:$APP_USER $APP_LOGS

CMD sudo -u $APP_USER java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE $APP_HOME/shoppinglist-rest-service-1.0-SNAPSHOT.jar
