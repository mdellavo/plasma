#!/bin/sh

MODULE="plasma"

TAG=$1

if [ -z "${TAG}" ]
then
    echo "USAGE: build-release.sh v1.0.1-2"
    exit 1
fi

VERSION=${TAG#v}
VERSION_NAME=${VERSION%-*}
VERSION_CODE=${VERSION#*-}

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

./gradlew -PversionCode=${VERSION_CODE} -PversionName={VERSION_NAME}clean assembleRelease
cp "${MODULE}/build/apk/${MODULE}-release.apk" ${APK_PATH}

git tag -d ${TAG} 2>&1 >& /dev/null

git tag -a ${TAG} -m "release - ${VERSION}"

echo
echo "Build Complete -> ${APK_PATH}"