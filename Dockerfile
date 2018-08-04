FROM openjdk:10-slim

ENV HOME=/app
ENV APP_HOME=$HOME
ENV APP_USER=app
ENV APP_USER_ID=7777
ENV APP_LOGS=$APP_HOME/logs
ENV APP_CONFIG=$APP_HOME/config
ENV APP_PORT=8080

ENV TZ=Europe/Berlin

ENV JVM_OPTIONS="--illegal-access=deny"

EXPOSE $APP_PORT

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN apt-get update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends sudo
RUN sudo adduser --system --disabled-login --uid $APP_USER_ID --group --home $APP_HOME $APP_USER

ADD /build/libs $APP_HOME

WORKDIR $APP_HOME

RUN mkdir $APP_LOGS
RUN chmod -R 770 $APP_LOGS
RUN chown -R $APP_USER:$APP_USER $APP_LOGS

CMD sudo -u $APP_USER java $JVM_OPTIONS -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE $APP_HOME/shoppinglist-rest-service-1.0-SNAPSHOT.jar
