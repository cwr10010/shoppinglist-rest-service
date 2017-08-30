#!/bin/env sh

if [ -z "$TRAVIS_BUILD_DIR" ]; then
    export TRAVIS_BUILD_DIR="."
fi

SRC_DIR=$TRAVIS_BUILD_DIR/deploy/compose

TARGET_USER=cwrosede
TARGET_HOST=cwrose.de
export TARGET_DIR=subdomains/shoppinglist/
TARGET=$TARGET_USER@$TARGET_HOST:$TARGET_DIR

rsync -r --delete-after --quiet $SRC_DIR $TARGET

ssh $TARGET_USER@$TARGET_HOST <<'ENDSSH'
docker-compose  -f subdomains/shoppinglist/compose/docker-compose.yml up -d srs_deploy
ENDSSH