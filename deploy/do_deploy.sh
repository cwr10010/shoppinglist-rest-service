#!/bin/env sh

if [ -z "$TRAVIS_BUILD_DIR" ]; then
    export TRAVIS_BUILD_DIR="../"
fi

if [ -z "$1" ]; then
    export PROFILE=dev
else
    export PROFILE=$1
fi

SRC_DIR=$TRAVIS_BUILD_DIR/deploy/compose

TARGET_USER=cwrosede
TARGET_HOST=cwrose.de
TARGET_DIR=subdomains/shoppinglist/
TARGET=$TARGET_USER@$TARGET_HOST:$TARGET_DIR

ssh $TARGET_USER@$TARGET_HOST "hostname; cd $TARGET_DIR/shoppinglist-config && git pull"
ssh $TARGET_USER@$TARGET_HOST docker-compose  -f $TARGET_DIR/shoppinglist-config/compose/docker-compose-$PROFILE.yml stop srs_api_${PROFILE}
ssh $TARGET_USER@$TARGET_HOST docker-compose  -f $TARGET_DIR/shoppinglist-config/compose/docker-compose-$PROFILE.yml pull srs_api_${PROFILE}
ssh $TARGET_USER@$TARGET_HOST docker-compose  -f $TARGET_DIR/shoppinglist-config/compose/docker-compose-$PROFILE.yml up -d srs_api_${PROFILE}
