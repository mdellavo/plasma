#!/bin/sh

MODULE="plasma"
VERSION=$1

if [ -z "${VERSION}" ]
then
    echo "USAGE: build-release.sh VERSION"
    exit 1
fi

BUILD_PATH="./builds"

if [ -e "${BUILD_PATH}" ]
then
    mkdir "${BUILD_PATH}"
fi

APK_PATH="${BUILD_PATH}/{$MODULE}-release-${VERSION}.apk"

./gradlew installRelease
cp "${MODULE}/build/apk/${MODULE}-release.apk" ${APK_PATH}

git tag -a ${VERSION} -m "release - ${VERSION}"

echo
echo ${APK_PATH}