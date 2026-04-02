#!/bin/bash
# Скрипт для загрузки проекта на новый GitHub аккаунт
# Использование: ./push-to-github.sh <GITHUB_USERNAME> <REPO_NAME>

USERNAME=$1
REPO_NAME=$2

if [ -z "$USERNAME" ] || [ -z "$REPO_NAME" ]; then
    echo "Использование: ./push-to-github.sh <GITHUB_USERNAME> <REPO_NAME>"
    echo "Пример: ./push-to-github.sh ivanpetrov lis-integration-backend"
    exit 1
fi

echo "=========================================="
echo "Загрузка проекта на GitHub"
echo "=========================================="
echo ""

# 1. Клонировать из bundle
mkdir -p temp-repo
cd temp-repo
git clone ../lis-backend.bundle .

# 2. Привязать новый remote
git remote add origin https://github.com/${USERNAME}/${REPO_NAME}.git

# 3. Пуш на новый репозиторий
echo ""
echo "Пушим код на github.com/${USERNAME}/${REPO_NAME}..."
git push -u origin main

cd ..

echo ""
echo "=========================================="
echo "✅ ГОТОВО!"
echo "Репозиторий загружен: https://github.com/${USERNAME}/${REPO_NAME}"
echo "=========================================="
