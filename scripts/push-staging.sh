#!/bin/sh
set -eu
: ${GIT_REPOSITORY:?}
set -x

RELEASE_NOTES=`cat build/release-notes`

# Require the release notes to contain something else than just the TBD placeholder
if echo "$RELEASE_NOTES" | grep --line-regexp --quiet "\- TBD"
then
    echo "Release notes not filled in"
    exit 1
fi

git clone "$GIT_REPOSITORY" work
cd work
git remote add staging ../staging.git
git fetch staging
git merge staging/master
git push origin master
git push origin --tags
