#!/bin/sh
set -eu
: ${DEPLOY_USERNAME:?}
: ${DEPLOY_PASSWORD:?}
set -x

VERSION=`cat build/version`

ruby scripts/upload-maven-repository.rb staging https://oss.sonatype.org/service/local/staging/deploy/maven2

mvn nexus:staging-close \
    --batch-mode \
    --errors \
    -Dnexus.url=https://oss.sonatype.org/ \
    -Dnexus.username="$DEPLOY_USERNAME" \
    -Dnexus.password="$DEPLOY_PASSWORD" \
    -Dnexus.groupId=fi.jumi \
    -Dnexus.artifactId=parent \
    -Dnexus.version="$VERSION" \
    -Dnexus.description="Jumi $VERSION"
