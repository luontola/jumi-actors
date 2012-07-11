#!/bin/sh
set -e
: ${GPG_KEYNAME:?}
: ${GPG_PASSPHRASE:?}
set -x

VERSION=`cat build/version`
REVISION=`cat build/revision`
CHANGELOG=`cat build/changelog`
TAG="jumi-$VERSION"

git clone git@github.com:orfjackal/jumi.git work
cd work

git tag -u "$GPG_KEYNAME" -m "Jumi $VERSION" -m "$CHANGELOG" "$TAG" "$REVISION"
git push origin "$TAG"

git checkout -b release "$REVISION"
ruby scripts/bump-release-changelog.rb CHANGELOG.md "$VERSION"
git add CHANGELOG.md
git commit -m "Updated changelog for Jumi $VERSION release"
git checkout master
git merge release
git push origin master
