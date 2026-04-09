# LIMS Dashboard - Deployment Scripts

Скрипты для автоматического развертывания LIMS Dashboard на Windows Server.

## Структура скриптов

| № | Скрипт | Описание |
|---|--------|----------|
| 1 | `01-install-prerequisites.ps1` | Установка Java 17, PostgreSQL 15, Node.js, nginx |
| 2 | `02-setup-database.ps1` | Создание базы данных и пользователя |
| 3 | `03-create-config.ps1` | Создание конфигурационных файлов |
| 4 | `04-build-and-deploy.ps1` | Сборка и копирование файлов (на dev машине) |
| 5 | `05-start-lims.bat` | Запуск всех компонентов |
| 6 | `06-stop-lims.bat` | Остановка всех компонентов |
| - | `setup-all.ps1` | Мастер-скрипт установки (запускает 1-3) |

## Быстрый старт (на сервере)

```powershell
# 1. Откройте PowerShell от Администратора
# 2. Перейдите в папку со скриптами
cd C:\LIMS\deployment-scripts

# 3. Запустите полную установку
.\setup-all.ps1 -PostgresPassword "postgres123" -AppDbPassword "lims123"

# 4. Скопируйте файлы приложения (вручную или через 04-build-and-deploy.ps1)
#    - Backend JAR → C:\LIMS\backend\
#    - Frontend dist → C:\LIMS\frontend\

# 5. Запустите приложение
.\05-start-lims.bat
```

## Пошаговая установка

### Шаг 1: Установка зависимостей
```powershell
.\01-install-prerequisites.ps1 -PostgresPassword "your_password"
```

Устанавливает:
- Eclipse Temurin JDK 17
- PostgreSQL 15
- Node.js LTS 20.x
- nginx 1.24

### Шаг 2: Настройка базы данных
```powershell
.\02-setup-database.ps1 -PostgresPassword "your_password" -AppDbPassword "app_password"
```

Создает:
- Базу данных `lis_dashboard`
- Пользователя `lims_user`
- Настраивает права доступа

### Шаг 3: Создание конфигурации
```powershell
.\03-create-config.ps1 -LimsPath "C:\LIMS" -DbPassword "app_password"
```

Создает:
- `C:\LIMS\config\application.properties` - конфиг backend
- `C:\LIMS\frontend\.env` - конфиг frontend
- `C:\LIMS\config\nginx.conf` - конфиг nginx
- `C:\LIMS\start-backend.bat` - скрипт запуска
- `C:\LIMS\install-service.bat` - установка Windows Service

### Шаг 4: Сборка и деплой (на dev машине)
```powershell
.\04-build-and-deploy.ps1 -ServerAddress "192.168.1.100" -ServerUsername "admin" -ServerPassword "pass"
```

Собирает:
- Backend JAR через Gradle
- Frontend через npm/vite
- Копирует файлы на сервер

Параметры:
- `-ServerAddress` - IP сервера (если не указан - локальный деплой)
- `-SkipBuild` - только копирование без сборки
- `-CreateZip` - создать ZIP архив для ручного переноса

## Управление приложением

### Запуск
```cmd
.\05-start-lims.bat
```
Проверяет PostgreSQL, запускает Backend и nginx.

### Остановка
```cmd
.\06-stop-lims.bat
```
Останавливает nginx и Backend (PostgreSQL оставляет запущенным).

### Windows Service (для production)
```cmd
C:\LIMS\install-service.bat
```
Устанавливает Backend как службу Windows для автозапуска.

## Структура директорий после установки

```
C:\LIMS
├── backend\                    # Backend JAR
│   ├── univ-0.0.1-SNAPSHOT.jar
│   └── LIMS-Backend.exe        # WinSW для службы
├── frontend\                    # Frontend статические файлы
│   ├── index.html
│   ├── assets\
│   └── .env
├── config\                     # Конфигурация
│   ├── application.properties  # Backend конфиг
│   └── nginx.conf              # nginx конфиг
├── logs\                       # Логи
├── backup\                      # Бэкапы БД
├── uploads\                     # Загруженные файлы
├── start-backend.bat           # Ручной запуск
├── install-service.bat         # Установка службы
└── README.txt                  # Инструкция

C:\Tools                        # Установленные инструменты
C:\nginx                        # nginx
C:\Program Files\PostgreSQL\15  # PostgreSQL
C:\Program Files\Eclipse Adoptium\jdk-17  # Java
```

## Параметры скриптов

### 01-install-prerequisites.ps1
```powershell
-InstallPath "C:\Tools"          # Куда скачивать установщики
-PostgresPassword "postgres123"  # Пароль для PostgreSQL
-SkipJava                        # Пропустить установку Java
-SkipPostgres                   # Пропустить установку PostgreSQL
-SkipNode                       # Пропустить установку Node.js
-SkipNginx                      # Пропустить установку nginx
```

### 02-setup-database.ps1
```powershell
-PostgresUser "postgres"         # Админ пользователь PostgreSQL
-PostgresPassword "postgres123"  # Пароль админа
-DatabaseName "lis_dashboard"   # Имя базы данных
-AppDbUser "lims_user"          # Имя пользователя приложения
-AppDbPassword "lims_password"  # Пароль пользователя приложения
```

### 03-create-config.ps1
```powershell
-LimsPath "C:\LIMS"             # Корневая директория приложения
-DatabaseName "lis_dashboard"   # Имя БД
-DbUser "lims_user"             # Пользователь БД
-DbPassword "lims_password"     # Пароль пользователя БД
-BackendPort 8080               # Порт backend
-FrontendApiUrl "http://localhost:8080"  # URL API для frontend
```

### 04-build-and-deploy.ps1
```powershell
-SourceBackend "..\univ"         # Путь к backend проекту
-SourceFrontend "..\..\lis-integration-front"  # Путь к frontend
-DeployTarget "C:\LIMS"         # Куда деплоить
-ServerAddress "192.168.1.100"  # IP сервера (для удаленного деплоя)
-ServerUsername "admin"         # Логин для сервера
-ServerPassword "pass"          # Пароль для сервера
-SkipBuild                      # Не собирать, только копировать
-SkipBackend                    # Пропустить сборку backend
-SkipFrontend                   # Пропустить сборку frontend
-CreateZip                      # Создать ZIP архив
```

## Устранение неполадок

### PostgreSQL не запускается
```powershell
# Проверить статус службы
Get-Service -Name "postgresql-x64-15"

# Запустить вручную
Start-Service -Name "postgresql-x64-15"

# Проверить логи
Get-Content "C:\Program Files\PostgreSQL\15\data\log\*.log" -Tail 50
```

### Backend не подключается к БД
```powershell
# Проверить подключение
& "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U lims_user -d lis_dashboard -c "SELECT 1;"

# Проверить конфиг
Get-Content C:\LIMS\config\application.properties
```

### nginx не запускается
```cmd
# Проверить конфиг
cd C:\nginx
nginx -t -c C:\LIMS\config\nginx.conf

# Запустить вручную с логами
nginx -c C:\LIMS\config\nginx.conf
```

### Порт занят
```powershell
# Найти процесс на порту 8080
netstat -ano | findstr :8080

# Завершить процесс
Stop-Process -Id <PID> -Force
```

## Безопасность

⚠️ **Важно для Production:**

1. Измените пароли по умолчанию
2. Настройте фаервол (открыты порты: 80, 443, 8080)
3. Настройте HTTPS/ssl для nginx
4. Создайте отдельного пользователя БД (не postgres)
5. Включите Windows Authentication для PostgreSQL
6. Настройте бэкапы БД
7. Установите антивирус

## Ссылки

- [Детальное руководство](../DEPLOYMENT_GUIDE_WINDOWS.md)
- PostgreSQL: https://www.postgresql.org/
- Eclipse Temurin: https://adoptium.net/
- nginx: https://nginx.org/
- WinSW: https://github.com/winsw/winsw
