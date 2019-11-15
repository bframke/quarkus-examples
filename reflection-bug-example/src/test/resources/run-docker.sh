#!/bin/bash
version=$(grep "version *=" build.gradle|sed -e "s/version *= *//" -e "s/'//g")
VERSION=${VERSION:-$version}
docker container rm bug-example || true
docker run -d --name bug-example --network host \
 -e CONFIG_FILE_LOCATION=$(dirname "${BASH_SOURCE[0]}")/ \
  bug-example:$VERSION
