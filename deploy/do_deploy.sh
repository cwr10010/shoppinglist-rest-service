#!/bin/env sh

if [ -z "$TRAVIS_BUILD_DIR" ]; then
    export TRAVIS_BUILD_DIR="../"
fi

if [ -z "$1" ]; then
    export PROFILE=deploy
else
    export PROFILE=$1
fi

SRC_DIR=$TRAVIS_BUILD_DIR/deploy/compose

TARGET_USER=cwrosede
TARGET_HOST=cwrose.de
TARGET_DIR=subdomains/shoppinglist/$PROFILE
TARGET=$TARGET_USER@$TARGET_HOST:$TARGET_DIR

rsync -r --delete-after --quiet $SRC_DIR $TARGET

ssh $TARGET_USER@$TARGET_HOST git pull git@server01:/srv/git/shoppinglist-config.git $TARGET_DIR/shoppinglist-config
ssh $TARGET_USER@$TARGET_HOST docker-compose  -f $TARGET_DIR/compose/docker-compose-$PROFILE.yml stop srs_$PROFILE
ssh $TARGET_USER@$TARGET_HOST docker-compose  -f $TARGET_DIR/compose/docker-compose-$PROFILE.yml pull srs_$PROFILE
ssh $TARGET_USER@$TARGET_HOST docker-compose  -f $TARGET_DIR/compose/docker-compose-$PROFILE.yml up -d srs_$PROFILE
