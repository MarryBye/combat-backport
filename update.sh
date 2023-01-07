#!/bin/bash

SRC_FOLDER=$(readlink -f ${BASH_SOURCE%/*})
SRC_FOLDER=$(cd "$SRC_FOLDER" && pwd -P)
TARGET_FOLDER=$(cd "$1" && pwd -P)

echo "Source at ${SRC_FOLDER}"
echo "Target at $TARGET_FOLDER"

if [[ "$SRC_FOLDER" == "$TARGET_FOLDER" ]]; then
  echo "Src=Target"
  exit 1
fi

if [[ ! -f "$TARGET_FOLDER"/gradlew ]]; then
  echo "Target at $TARGET_FOLDER is not a gradle project"
fi

cp -av "$SRC_FOLDER/gradle" "$SRC_FOLDER/build.gradle" "$TARGET_FOLDER/"
cp -av "$SRC_FOLDER/settings.gradle.example" "$TARGET_FOLDER/settings.gradle"
