#!/bin/sh
set -eu
: ${GO_PIPELINE_COUNTER:?}
: ${GPG_KEYNAME:?}
: ${PWD:?}
set -x

RELEASE_VERSION=`ruby scripts/get-release-version.rb $GO_PIPELINE_COUNTER`
RELEASE_NOTES=`ruby scripts/get-release-notes.rb RELEASE-NOTES.md`
TAG="v$RELEASE_VERSION"

ruby scripts/prepare-release-notes.rb RELEASE-NOTES.md "$RELEASE_VERSION"
git add RELEASE-NOTES.md
git commit -m "Release $RELEASE_VERSION"
git tag -u "$GPG_KEYNAME" -m "Jumi $RELEASE_VERSION" -m "$RELEASE_NOTES" "$TAG"
export RELEASE_REVISION=`git rev-parse HEAD`

mkdir build
echo "$RELEASE_VERSION" > build/version
echo "$RELEASE_REVISION" > build/revision
echo "$RELEASE_NOTES" > build/release-notes

mvn versions:set \
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

ruby scripts/bump-release-notes.rb RELEASE-NOTES.md
git add RELEASE-NOTES.md
git commit -m "Prepare for next development iteration"

git init --bare staging.git
git push staging.git "$TAG"
git push staging.git HEAD:master
