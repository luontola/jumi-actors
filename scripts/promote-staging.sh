#!/bin/sh
set -eu
: ${DEPLOY_USERNAME:?}
: ${DEPLOY_PASSWORD:?}
set -x

VERSION=`cat build/version`

ruby scripts/upload-maven-repository.rb staging https://oss.sonatype.org/service/local/staging/deploy/maven2

# The POM is not checked out to working directory when this script is run,
# so we must say manually that which artifact's open staging repository to close.
mvn org.sonatype.plugins:nexus-maven-plugin:2.1:staging-close \
    --batch-mode \
    --errors \
    -Dnexus.url=https://oss.sonatype.org/ \
    -Dnexus.username="$DEPLOY_USERNAME" \
    -Dnexus.password="$DEPLOY_PASSWORD" \
    -Dnexus.groupId=fi.jumi \
    -Dnexus.artifactId=parent \
    -Dnexus.version="$VERSION" \
    -Dnexus.description="Jumi $VERSION"
