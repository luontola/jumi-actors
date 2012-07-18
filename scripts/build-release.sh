#!/bin/sh
set -eu
: ${GO_PIPELINE_COUNTER:?}
: ${GO_REVISION_SOURCES:?}
: ${GPG_KEYNAME:?}
: ${PWD:?}
set -x

RELEASE_VERSION=`ruby scripts/get-release-version.rb $GO_PIPELINE_COUNTER`
RELEASE_NOTES=`ruby scripts/get-release-notes.rb RELEASE-NOTES.md`
TAG="v$RELEASE_VERSION"

mkdir build
echo "$RELEASE_VERSION" > build/version
echo "$GO_REVISION_SOURCES" > build/revision
echo "$RELEASE_NOTES" > build/release-notes

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
    -Dgpg.passphrase="" \
    -DaltDeploymentRepository="staging::default::file://$PWD/staging"

git tag -u "$GPG_KEYNAME" -m "Jumi $RELEASE_VERSION" -m "$RELEASE_NOTES" "$TAG" "$GO_REVISION_SOURCES"

ruby scripts/bump-release-notes.rb RELEASE-NOTES.md "$RELEASE_VERSION"
git add RELEASE-NOTES.md
git commit -m "Release notes for Jumi $RELEASE_VERSION"

git init --bare staging.git
git push staging.git "$TAG"
git push staging.git HEAD:master
