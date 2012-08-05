#!/bin/sh
set -eu
: ${GIT_REPOSITORY:?}
set -x

git clone "$GIT_REPOSITORY" work
cd work
git remote add staging ../staging.git
git fetch staging
git merge staging/master
git push origin master
git push origin --tags
