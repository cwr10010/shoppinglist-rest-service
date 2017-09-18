FROM openjdk:8

ENV HOME=/app
ENV APP_HOME=/app
ENV APP_USER=app
ENV APP_LOGS=$APP_HOME/logs
ENV TZ=Europe/Berlin

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN useradd -u 7777 --create-home -d $APP_HOME -s /bin/nologin -c "Docker image user" $APP_USER
RUN apt-get update && apt-get -y install sudo

ADD /build/libs $APP_HOME

WORKDIR $APP_HOME

RUN mkdir $APP_LOGS
RUN chmod -R 770 $APP_LOGS
RUN chown -R $APP_USER:$APP_USER $APP_LOGS

VOLUME $APP_LOGS

CMD sudo -u $APP_USER java -jar -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE $APP_HOME/shoppinglist-rest-service-1.0-SNAPSHOT.jar
