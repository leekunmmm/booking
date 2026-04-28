#!/bin/bash
# Tạo thư mục nộp bài ProgAndTest_GroupXY
# Đổi XY thành số nhóm của bạn trước khi chạy

GROUP="07"  # <-- ĐỔI SỐ NHÓM TẠI ĐÂY
FOLDER="ProgAndTest_Group${GROUP}"

echo "Tạo thư mục $FOLDER ..."
rm -rf "../$FOLDER"
mkdir -p "../$FOLDER/src"

# Copy source code
cp -r src "../$FOLDER/src"
cp pom.xml "../$FOLDER/"
cp -r .mvn "../$FOLDER/" 2>/dev/null || true
cp mvnw "../$FOLDER/" 2>/dev/null || true

# Copy các file bắt buộc
cp Dockerfile "../$FOLDER/"
cp evidence.md "../$FOLDER/"
cp -r docs "../$FOLDER/"

echo "Nén thành $FOLDER.zip ..."
cd ..
zip -r "${FOLDER}.zip" "$FOLDER"

echo "Hoàn thành! File nộp: ../${FOLDER}.zip"
