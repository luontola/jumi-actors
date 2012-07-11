#!/bin/sh
set -e
: ${GO_PIPELINE_COUNTER:?}
: ${GO_REVISION_SOURCES:?}
: ${GPG_KEYNAME:?}
: ${GPG_PASSPHRASE:?}
: ${PWD:?}
set -x

RELEASE_VERSION=`ruby scripts/get-release-version.rb $GO_PIPELINE_COUNTER`

mkdir build
echo "$RELEASE_VERSION" > build/version
echo "$GO_REVISION_SOURCES" > build/revision

mvn org.codehaus.mojo:versions-maven-plugin:1.3.1:set \
    --batch-mode \
    --errors \
    -DgenerateBackupPoms=false \
    -DnewVersion="$RELEASE_VERSION" \
    --file parent/pom.xml

mvn clean deploy \
    --batch-mode \
    --errors \
    -Psonatype-oss-release \
    -Dgpg.keyname="$GPG_KEYNAME" \
    -Dgpg.passphrase="$GPG_PASSPHRASE" \
    -DaltDeploymentRepository="staging::default::file://$PWD/staging"
