#!/bin/sh
set -eu
: ${GIT_REPOSITORY:?}
set -x

CHANGELOG=`cat build/changelog`

# Require the changelog to contain something else than just the TBD placeholder
if echo "$CHANGELOG" | grep --line-regexp --quiet "\- TBD"
then
    echo "Changelog not ready for release"
    exit 1
fi

git clone "$GIT_REPOSITORY" work
cd work
git remote add staging ../staging.git
git fetch staging
git merge staging/master
git push origin master
git push origin --tags
