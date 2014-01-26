#!/bin/sh

MODULE="plasma"
VERSION=$1

if [ -z "${VERSION}" ]
then
    echo "USAGE: build-release.sh VERSION"
    exit 1
fi

git diff --exit-code > /dev/null
CLEAN=$?

if [ $CLEAN -ne 0 ]
then
    echo "ERROR: branch is not clean"
    exit 1
fi

BUILD_PATH="./builds"

if [ ! -e "${BUILD_PATH}" ]
then
    mkdir "${BUILD_PATH}"
fi

APK_PATH="${BUILD_PATH}/${MODULE}-${VERSION}.apk"

./gradlew clean assembleRelease
cp "${MODULE}/build/apk/${MODULE}-release.apk" ${APK_PATH}

TAGNAME="v${VERSION}"
git tag -d ${TAGNAME}
git tag -a ${TAGNAME} -m "release - ${VERSION}"

echo
echo ${APK_PATH}