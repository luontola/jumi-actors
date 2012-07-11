#!/bin/sh
set -eu
set -x

mvn clean verify \
    --batch-mode \
    --errors \
    -Pcoverage-report \
    -DskipTests \
    -Dinvoker.skip

ruby scripts/generate-coverage-report-index.rb coverage-reports/index.html
