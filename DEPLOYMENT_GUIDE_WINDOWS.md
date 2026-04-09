# LIMS Dashboard - Deployment Guide for Windows Server

## Общая информация

Проект состоит из двух частей:
- **Backend**: Kotlin + Spring Boot (порт 8080)
- **Frontend**: React + Vite (порт 5173 в dev, или статические файлы через nginx/IIS)
- **Database**: PostgreSQL 15+

---

## Шаг 1: Установка необходимого ПО на сервер

### 1.1 Установка Java JDK 17

1. Скачайте OpenJDK 17 с сайта: https://adoptium.net/temurin/releases/?version=17&package=jdk
   - Выберите: Windows x64 MSI Installer
2. Установите JDK (запомните путь установки, например `C:\Program Files\Eclipse Adoptium\jdk-17`)
3. Проверьте установку в командной строке:
   ```cmd
   java -version
   javac -version
   ```

### 1.2 Установка PostgreSQL

1. Скачайте PostgreSQL с: https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
   - Версия: 15.x (Windows x86-64)
2. Установите PostgreSQL:
   - Установите пароль для пользователя `postgres` (запомните его!)
   - Порт по умолчанию: 5432
   - Оставьте галочку на Stack Builder (дополнительные инструменты)
3. После установки откройте pgAdmin 4 (устанавливается вместе с PostgreSQL)
4. Создайте базу данных:
   - Откройте pgAdmin → Servers → PostgreSQL 15 → Databases
   - ПКМ на Databases → Create → Database
   - Имя базы: `lis_dashboard`
   - Владелец: `postgres`

### 1.3 Установка Node.js (для сборки frontend)

**Вариант A: Сборка на локальной машине (рекомендуется)**
- Соберите frontend на своём компьютере, скопируйте готовые файлы на сервер

**Вариант B: Сборка на сервере**
1. Скачайте Node.js LTS с: https://nodejs.org/
2. Установите с настройками по умолчанию
3. Проверьте:
   ```cmd
   node --version
   npm --version
   ```

---

## Шаг 2: Сборка Backend (на вашей машине разработки)

### 2.1 Проверка конфигурации

Проверьте файл `univ/src/main/resources/application.properties` или создайте его:

```properties
# Database Configuration
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/lis_dashboard
spring.r2dbc.username=postgres
spring.r2dbc.password=ВАШ_ПАРОЛЬ

# Flyway (database migrations)
spring.flyway.url=jdbc:postgresql://localhost:5432/lis_dashboard
spring.flyway.user=postgres
spring.flyway.password=ВАШ_ПАРОЛЬ

# Server
server.port=8080
```

### 2.2 Сборка JAR файла

1. Откройте терминал в папке `d:\lis-integration-backend\univ`
2. Выполните сборку:
   ```cmd
   .\gradlew.bat bootJar
   ```
3. После успешной сборки, JAR файл будет находиться в:
   ```
   univ\build\libs\univ-0.0.1-SNAPSHOT.jar
   ```

---

## Шаг 3: Сборка Frontend (на вашей машине разработки)

### 3.1 Установка зависимостей (если ещё не установлены)

```cmd
cd d:\lis-integration-front
npm install
```

### 3.2 Настройка API URL

Создайте/отредактируйте файл `.env` в папке `lis-integration-front`:

```env
VITE_API_BASE_URL=http://ВАШ_СЕРВЕР:8080
```

Например:
```env
VITE_API_BASE_URL=http://192.168.1.100:8080
```

### 3.3 Сборка production-версии

```cmd
npm run build
```

После сборки, готовые файлы будут в папке `dist\`

---

## Шаг 4: Копирование файлов на сервер

### 4.1 Создайте структуру папок на сервере

```
C:\LIMS\
├── backend\
│   └── univ-0.0.1-SNAPSHOT.jar
├── frontend\
│   └── (содержимое папки dist\)
├── logs\
└── config\
    └── application.properties (опционально)
```

### 4.2 Копирование файлов

**Backend:**
- Скопируйте `univ\build\libs\univ-0.0.1-SNAPSHOT.jar` → `C:\LIMS\backend\`

**Frontend:**
- Скопируйте всё содержимое папки `dist\` → `C:\LIMS\frontend\`

### 4.3 Внешний конфигурационный файл (опционально)

Если хотите хранить конфигурацию отдельно от JAR:

1. Создайте файл `C:\LIMS\config\application.properties`:
   ```properties
   spring.r2dbc.url=r2dbc:postgresql://localhost:5432/lis_dashboard
   spring.r2dbc.username=postgres
   spring.r2dbc.password=ВАШ_ПАРОЛЬ
   spring.flyway.url=jdbc:postgresql://localhost:5432/lis_dashboard
   spring.flyway.user=postgres
   spring.flyway.password=ВАШ_ПАРОЛЬ
   server.port=8080
   ```

---

## Шаг 5: Настройка PostgreSQL на сервере

### 5.1 Создание базы данных

1. Откройте pgAdmin на сервере
2. Подключитесь к серверу PostgreSQL
3. Создайте базу данных `lis_dashboard`

Или через командную строку psql:
```cmd
"C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -c "CREATE DATABASE lis_dashboard;"
```

### 5.2 Настройка доступа

Отредактируйте файл `C:\Program Files\PostgreSQL\15\data\pg_hba.conf`:

Найдите строки:
```
# IPv4 local connections:
host    all             all             127.0.0.1/32            scram-sha-256
```

И замените метод аутентификации для локальных подключений (только для development):
```
# IPv4 local connections:
host    all             all             127.0.0.1/32            trust
```

**Перезапустите PostgreSQL:**
- Откройте Services (services.msc)
- Найдите `postgresql-x64-15`
- ПКМ → Restart

---

## Шаг 6: Запуск Backend

### 6.1 Ручной запуск (для тестирования)

```cmd
cd C:\LIMS\backend
java -jar univ-0.0.1-SNAPSHOT.jar
```

Или с внешним конфигом:
```cmd
cd C:\LIMS\backend
java -jar univ-0.0.1-SNAPSHOT.jar --spring.config.location=file:C:/LIMS/config/application.properties
```

Приложение запустится на порту 8080.
Проверка: откройте в браузере `http://localhost:8080/actuator/health`

### 6.2 Создание Windows Service для автозапуска

**Способ 1: Использование WinSW (рекомендуется)**

1. Скачайте WinSW: https://github.com/winsw/winsw/releases
   - Файл: `WinSW-x64.exe`
2. Переименуйте в `LIMS-Backend.exe` и положите в `C:\LIMS\backend\`
3. Создайте файл `LIMS-Backend.xml`:
   ```xml
   <service>
     <id>lims-backend</id>
     <name>LIMS Dashboard Backend</name>
     <description>LIMS Dashboard Spring Boot Application</description>
     <executable>java</executable>
     <arguments>-jar "C:\LIMS\backend\univ-0.0.1-SNAPSHOT.jar" --spring.config.location=file:C:/LIMS/config/application.properties</arguments>
     <logpath>C:\LIMS\logs</logpath>
     <logmode>rotate</logmode>
   </service>
   ```
4. Установите сервис:
   ```cmd
   cd C:\LIMS\backend
   LIMS-Backend.exe install
   ```
5. Запустите сервис:
   ```cmd
   LIMS-Backend.exe start
   ```
   Или через Services (services.msc) → найдите "LIMS Dashboard Backend" → Start

**Способ 2: Использование NSSM**

1. Скачайте NSSM: https://nssm.cc/download
2. Распакуйте nssm.exe в `C:\Windows\System32\`
3. Создайте сервис:
   ```cmd
   nssm install LIMS-Backend
   ```
4. В открывшемся окне настройте:
   - Path: `C:\Program Files\Eclipse Adoptium\jdk-17\bin\java.exe`
   - Startup directory: `C:\LIMS\backend`
   - Arguments: `-jar "C:\LIMS\backend\univ-0.0.1-SNAPSHOT.jar"`
5. Установите сервис и запустите:
   ```cmd
   nssm start LIMS-Backend
   ```

---

## Шаг 7: Настройка Frontend (варианты)

### Вариант A: Через nginx (рекомендуется для production)

1. Скачайте nginx для Windows: https://nginx.org/en/download.html
2. Распакуйте в `C:\nginx\`
3. Отредактируйте `C:\nginx\conf\nginx.conf`:
   ```nginx
   server {
       listen       80;
       server_name  localhost;
       
       location / {
           root   C:/LIMS/frontend;
           index  index.html index.htm;
           try_files $uri $uri/ /index.html;
       }
       
       location /api/ {
           proxy_pass http://localhost:8080/api/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```
4. Запустите nginx:
   ```cmd
   cd C:\nginx
   start nginx
   ```
5. Откройте `http://localhost` в браузере

### Вариант B: Через IIS (если уже установлен)

1. Откройте IIS Manager
2. Создайте новый сайт:
   - Site name: LIMS-Frontend
   - Physical path: `C:\LIMS\frontend`
   - Port: 80 (или другой)
3. Настройте MIME types для .js и .css если необходимо
4. Добавьте URL Rewrite rule для SPA (в web.config):
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <configuration>
     <system.webServer>
       <rewrite>
         <rules>
           <rule name="SPA" stopProcessing="true">
             <match url=".*" />
             <conditions logicalGrouping="MatchAll">
               <add input="{REQUEST_FILENAME}" matchType="IsFile" negate="true" />
               <add input="{REQUEST_FILENAME}" matchType="IsDirectory" negate="true" />
             </conditions>
             <action type="Rewrite" url="/index.html" />
           </rule>
         </rules>
       </rewrite>
     </system.webServer>
   </configuration>
   ```

### Вариант C: Простой Python сервер (только для тестирования)

Если на сервере установлен Python:
```cmd
cd C:\LIMS\frontend
python -m http.server 80
```

---

## Шаг 8: Проверка работоспособности

1. **Backend health check**:
   ```
   http://localhost:8080/actuator/health
   ```
   Должно вернуть: `{"status":"UP"}`

2. **API endpoints**:
   ```
   http://localhost:8080/api/analyzers
   http://localhost:8080/api/reagents/inventory/reagents
   ```

3. **Frontend**:
   ```
   http://localhost (или http://localhost:5173 для dev)
   ```

---

## Шаг 9: Firewall и безопасность

### 9.1 Настройка Windows Firewall

Откройте PowerShell от администратора:

```powershell
# Разрешить порт 8080 (Backend)
New-NetFirewallRule -DisplayName "LIMS Backend" -Direction Inbound -LocalPort 8080 -Protocol TCP -Action Allow

# Разрешить порт 80 (Frontend/HTTP)
New-NetFirewallRule -DisplayName "LIMS Frontend HTTP" -Direction Inbound -LocalPort 80 -Protocol TCP -Action Allow

# Разрешить порт 443 (HTTPS, если настроите)
New-NetFirewallRule -DisplayName "LIMS Frontend HTTPS" -Direction Inbound -LocalPort 443 -Protocol TCP -Action Allow
```

### 9.2 Доступ извне

Если нужен доступ с других компьютеров:
1. Убедитесь, что firewall настроен
2. Убедитесь, что в `application.properties` нет ограничений на `server.address`
3. Для production настройте HTTPS (SSL сертификат)

---

## Шаг 10: Автоматизация запуска (опционально)

### Создание batch-файла для запуска всего

Создайте `C:\LIMS\start-lims.bat`:
```batch
@echo off
echo Starting LIMS Dashboard...

:: Start Backend
cd C:\LIMS\backend
start "LIMS Backend" java -jar univ-0.0.1-SNAPSHOT.jar

:: Wait for backend to start
timeout /t 10 /nobreak >nul

:: Start nginx (если используете)
cd C:\nginx
start nginx

echo LIMS Dashboard started!
echo Backend: http://localhost:8080
echo Frontend: http://localhost
pause
```

### Создание batch-файла для остановки

Создайте `C:\LIMS\stop-lims.bat`:
```batch
@echo off
echo Stopping LIMS Dashboard...

:: Stop nginx (если используете)
cd C:\nginx
nginx -s quit

:: Kill Java process (backend)
taskkill /F /IM java.exe

echo LIMS Dashboard stopped!
pause
```

---

## Устранение неполадок

### Backend не запускается

1. Проверьте логи в консоли
2. Проверьте подключение к PostgreSQL:
   ```cmd
   "C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -d lis_dashboard -c "SELECT 1;"
   ```
3. Проверьте, что порт 8080 не занят:
   ```cmd
   netstat -ano | findstr :8080
   ```

### Frontend не отображается

1. Проверьте, что все файлы из `dist\` скопированы
2. Проверьте консоль браузера (F12 → Console)
3. Убедитесь, что API_URL настроен правильно в `.env`

### Ошибка CORS

Если frontend и backend на разных портах/доменах, добавьте в `application.properties`:
```properties
cors.allowed-origins=http://localhost,http://localhost:5173,http://ВАШ_СЕРВЕР
```

---

## Сводка команд для быстрого развёртывания

```cmd
:: === ШАГ 1: Подготовка сервера ===
:: Установите: Java 17, PostgreSQL 15, Node.js (опционально)

:: === ШАГ 2: Создание структуры ===
mkdir C:\LIMS\backend
mkdir C:\LIMS\frontend
mkdir C:\LIMS\logs
mkdir C:\LIMS\config

:: === ШАГ 3: База данных ===
"C:\Program Files\PostgreSQL\15\bin\psql.exe" -U postgres -c "CREATE DATABASE lis_dashboard;"

:: === ШАГ 4: Копирование файлов (вручную) ===
:: - JAR файл → C:\LIMS\backend\
:: - distolder → C:\LIMS\frontend\
:: - application.properties → C:\LIMS\config\

:: === ШАГ 5: Запуск backend ===
cd C:\LIMS\backend
java -jar univ-0.0.1-SNAPSHOT.jar --spring.config.location=file:C:/LIMS/config/application.properties

:: === ШАГ 6: Запуск frontend (через nginx или Python) ===
cd C:\LIMS\frontend
python -m http.server 80
:: или запустите nginx
```

---

## Контакты и поддержка

При возникновении проблем:
1. Проверьте логи backend в консоли
2. Проверьте логи PostgreSQL: `C:\Program Files\PostgreSQL\15\data\log\`
3. Проверьте browser console (F12)
