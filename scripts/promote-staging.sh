#!/bin/sh
set -e
: ${GO_PIPELINE_LABEL:?}
: ${GO_DEPENDENCY_LOCATOR_JUMI:?}
: ${STAGING_USERNAME:?}
: ${STAGING_PASSWORD:?}
: ${RELEASE_USERNAME:?}
: ${RELEASE_PASSWORD:?}
set -x

ruby scripts/copy-staging-to-release-repo.rb

mvn org.sonatype.plugins:nexus-maven-plugin:2.0.6:staging-close \
    --batch-mode \
    --errors \
    -Dnexus.url=https://oss.sonatype.org/ \
    -Dnexus.username="$RELEASE_USERNAME" \
    -Dnexus.password="$RELEASE_PASSWORD" \
    -Dnexus.description="Jumi build $GO_PIPELINE_LABEL"
