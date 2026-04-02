# LIMS Dashboard - Детальное описание проекта

## 1. Общая информация

**Название:** LIMS (Laboratory Information Management System) Dashboard  
**Назначение:** Управление лабораторными исследованиями для государственной больницы Казахстана  
**Основная функция:** Интеграция с системой Дамumed для загрузки, обработки и анализа отчётов лаборатории

---

## 2. Архитектура системы

### 2.1 Технологический стек

**Backend:**
- **Язык:** Kotlin
- **Фреймворк:** Spring Boot WebFlux (реактивный стек)
- **База данных:** PostgreSQL + R2DBC (реактивный драйвер)
- **Миграции:** Flyway
- **Аутентификация:** Firebase Authentication (production) / Local Security (dev)
- **Сборка:** Gradle

**Frontend:**
- **Язык:** TypeScript
- **Фреймворк:** React
- **Стилизация:** CSS (legacy shell), современные компоненты
- **Роуты:** /dashboard, /registry, /worklists, /equipment, /warehouse, /reports, /admin

### 2.2 Структура проекта

```
d:/lis-integration-backend/
├── univ/                          # Backend (Spring Boot)
│   ├── src/main/kotlin/lab/dev/med/univ/
│   │   ├── feature/               # Основные модули
│   │   │   ├── authority/         # Роли и разрешения
│   │   │   ├── authorization/     # Авторизация
│   │   │   ├── files/             # Управление файлами
│   │   │   ├── reagents/          # Учёт реагентов
│   │   │   ├── reporting/         # Отчёты Damumed
│   │   │   └── users/             # Пользователи
│   │   └── UnivApplication.kt     # Точка входа
│   └── src/main/resources/
│       └── db/migration/          # Flyway миграции (V1-V10)
│
d:/lis-integration-front/           # Frontend (React)
├── src/
│   ├── styles.css                # Legacy shell
│   └── routes/                   # Страницы приложения
└── ...

reports/                          # Документация и анализ
├── биохимия.md                   # Спецификации реагентов
├── РЕАГЕНТЫ_V2_ПОЛНЫЙ_АНАЛИЗ.md # Анализ всего оборудования
└── analyzator_docum.md           # Технические характеристики
```

---

## 3. Модули системы

### 3.1 Модуль отчётов Damumed (`feature.reporting`)

**Назначение:** Загрузка, парсинг и нормализация Excel-отчётов из системы Damumed

**Поддерживаемые отчёты:**
| Тип отчёта | Назначение |
|------------|-----------|
| `WORKPLACE_COMPLETED_STUDIES` | Выполненные исследования по рабочим местам |
| `COMPLETED_LAB_STUDIES_JOURNAL` | Журнал выполненных лабораторных исследований |
| `REFERRAL_REGISTRATION_JOURNAL` | Журнал регистрации направлений |
| `POSITIVE_RESULTS_JOURNAL` | Журнал положительных результатов |
| `REJECT_LOG` | Журнал брака |
| `EMPLOYEE_COMPLETED_STUDIES_SUMMARY` | Сводка по сотрудникам |
| `REFERRAL_COUNT_BY_MATERIAL` | Количество направлений по материалам |
| `GOBMP_COMPLETED_SERVICES` | Выполненные услуги по ГОБМП |

**API Endpoints:**
- `POST /api/damumed-reports/uploads` — загрузка файла
- `GET /api/damumed-reports/uploads/{id}` — статус обработки
- `GET /api/damumed-reports/uploads/{id}/facts` — нормализованные факты
- `GET /api/damumed-reports/uploads/{id}/processed-view` — обработанное представление

**Ключевые сервисы:**
- `DamumedReportIngestionFacade` — фасад для загрузки
- `DamumedWorkbookRawParsingService` — парсинг Excel
- `DamumedWorkbookNormalizationService` — нормализация данных
- `DamumedOperationalOverviewQueryService` — аналитические запросы

### 3.2 Модуль учёта реагентов (`feature.reagents`)

**Назначение:** Полный цикл управления лабораторными реагентами и анализаторами

**Поддерживаемое оборудование (15 единиц):**

| Анализатор | Тип | Принцип работы |
|------------|-----|----------------|
| BC-5000 | Гематология | 27.5ml разбавителя на тест |
| BS-240/Pro/430/600M | Биохимия | R1+R2 реагенты на тест |
| C3100 | Коагулология | Лиофилизированные реагенты (4-8ч жизни) |
| iFlash 1800 | Иммунохимия | 50-тестовые кассеты с чипом |
| ORTHO Workstation | Иммуногематология | Гель-карты |
| OCG-102 | POCT | Картриджи |
| Edan i15 | POCT | Одноразовые картриджи |
| Fluorecare | POCT | Картриджи |
| Mission U500 | Глюкометр | Тест-полоски |
| Vision Pro | Гематология | Без реагентов |

**Функционал:**
- Каталог анализаторов (CRUD)
- Нормы расхода реагентов (reagent rates)
- Инвентаризация реагентов и расходников
- Загрузка логов анализаторов (Applogs, errors.xml)
- **Two-stream tracking:**
  - Stream 1: Легитимные тесты из LIS
  - Stream 2: Нелегальные "левые" запуски из логов
- Генерация отчётов по потреблению

**API Endpoints:**
- `GET/PUT/DELETE /api/reagents/analyzers` — управление анализаторами
- `GET/PUT/DELETE /api/reagents/analyzers/{id}/rates` — нормы расхода
- `GET/PUT/DELETE /api/reagents/inventory` — инвентарь
- `POST /api/reagents/log-uploads` — загрузка логов
- `POST /api/reagents/reports/generate` — генерация отчётов

### 3.3 Модуль авторизации (`feature.authorization`)

**Роли системы:**
- **Администратор** — полный доступ, включая админ-панель
- **Руководитель** — доступ к отчётам и аналитике
- **Аналитик** — рабочие функции, загрузка отчётов

**Особенности:**
- Firebase Auth в production
- Local Security в development (application-local.properties)
- Редирект не-админов на главный дашборд после логина

### 3.4 Модуль файлов (`feature.files`)

- Загрузка Excel-файлов
- Хранение на диске с метаданными в БД
- Привязка к отчётам Damumed

### 3.5 Модуль пользователей (`feature.users`)

- Управление учётными записями
- Привязка к Firebase UID
- Ролевая модель через Authority

---

## 4. База данных

### 4.1 Основные таблицы

**Отчёты Damumed:**
```sql
damumed_report_uploads          # Загрузки отчётов
damumed_report_parsed_sheets  # Распарсенные листы Excel
damumed_report_parsed_rows    # Строки
damumed_report_parsed_cells   # Ячейки
damumed_report_normalized_sections    # Нормализованные секции
damumed_report_normalized_facts       # Факты
damumed_report_normalized_dimensions  # Измерения
```

**Реагенты:**
```sql
analyzers                      # Справочник анализаторов
analyzer_reagent_rates         # Нормы расхода реагентов
reagent_inventory              # Инвентарь реагентов
consumable_inventory           # Инвентарь расходников
reagent_consumption_reports    # Отчёты по потреблению
analyzer_log_uploads           # Загруженные логи
parsed_analyzer_samples        # Распарсенные образцы из логов
```

**Пользователи и права:**
```sql
users, authorities, user_authorities  # Ролевая модель
files                                 # Метаданные файлов
```

### 4.2 Миграции Flyway

| Миграция | Содержание |
|----------|-----------|
| V1-V7 | Базовая схема (users, authority, files) |
| V8__reagent_tracking.sql | Таблицы для учёта реагентов |
| V9__biochemistry_coagulation_reagent_rates.sql | Нормы расхода для BS-серии и C3100 |
| V10__hematology_analyzer_rates.sql | BC-5000 и другие анализаторы |

---

## 5. Ключевые особенности реализации

### 5.1 Оптимизации для больших данных

**Batch Processing:**
- `DamumedNormalizedBatchRepository` — batch INSERT по 1000 записей
- Оптимизация для отчётов с 22K+ строк (с часа до минут)

**Streaming Excel Parsing:**
- Apache POI с потоковой обработкой
- Избежание OutOfMemory на больших файлах

### 5.2 Парсинг отчётов

**Проблемы решённые:**
- Multi-row headers (сложные заголовки с объединёнными ячейками)
- Пропущенные услуги из-за false-positive в date detection
- Двойной подсчёт через семантические ключи
- Deduplication на уровне facts

### 5.3 Two-Stream Reagent Tracking

**Архитектура:**
```
┌─────────────────┐     ┌─────────────────┐
│   LIS (legal)   │     │  Applogs/Errors │
│  parsed samples │     │ (unauthorized)  │
└────────┬────────┘     └────────┬────────┘
         │                       │
         └──────────┬────────────┘
                    │
         ┌──────────▼──────────┐
         │  Rerun Detection    │  ← ±30 min correlation
         │  (temporal match)   │
         └──────────┬──────────┘
                    │
         ┌──────────▼──────────┐
         │ Consumption Report  │
         │  legitimate/service │
         │  /unauthorized      │
         └─────────────────────┘
```

---

## 6. Текущий статус разработки

### Реализовано ✅

**Репорты Damumed:**
- Загрузка и парсинг всех типов отчётов
- Нормализация с batch save
- Query API для аналитики

**Реагенты:**
- Полный CRUD анализаторов и норм
- Загрузка логов
- Базовая генерация отчётов потребления
- 15 анализаторов в каталоге с реагентами

**Инфраструктура:**
- Firebase/local аутентификация
- Ролевая модель
- Flyway миграции

### В работе / TODO 📋

**Реагенты:**
- Детальная реконсиляция склад/LIS
- Discrepancy reporting
- Интеграция с модулем warehouse

**Frontend:**
- Удаление mock-данных
- Интеграция с backend API
- Admin panel доступ

---

## 7. API Usage Examples

### Загрузка отчёта Damumed

```bash
curl -X POST http://localhost:8080/api/damumed-reports/uploads \
  -H "Content-Type: multipart/form-data" \
  -F "file=@Отчет_по_выполненным_исследованиям.xls" \
  -F "sourceMode=MANUAL_UPLOAD"
```

### Получение обработанных данных

```bash
curl http://localhost:8080/api/damumed-reports/uploads/{uploadId}/processed-view/workplace-summary
```

### Генерация отчёта по реагентам

```bash
curl -X POST http://localhost:8080/api/reagents/reports/generate \
  -H "Content-Type: application/json" \
  -d '{
    "analyzerId": "bc5000-001",
    "periodStart": "2024-01-01",
    "periodEnd": "2024-01-31"
  }'
```

---

## 8. Сборка и запуск

### Требования
- Java 17 (JDK at `C:\Program Files\Java\jdk-17`)
- PostgreSQL
- Gradle 9.4+

### Backend
```bash
cd d:/lis-integration-backend/univ
./gradlew.bat bootRun          # Development (local profile)
./gradlew.bat bootRun --args='--spring.profiles.active=prod'  # Production
```

### Frontend
```bash
cd d:/lis-integration-front
npm install
npm start
```

---

## 9. Документация

**Внешние источники:**
- `reports/биохимия.md` — каталог реагентов Mindray
- `reports/РЕАГЕНТЫ_V2_ПОЛНЫЙ_АНАЛИЗ.md` — полный анализ оборудования
- `ts.md` — техническая спецификация ролей

**Внутренняя документация:**
- KotlinDoc в сервисах
- Swagger/OpenAPI доступен по `/swagger-ui.html` (при включении)

---

## 10. Контакты и поддержка

**Кодовая база:**
- Backend: `d:/lis-integration-backend/univ`
- Frontend: `d:/lis-integration-front`
- Документация: `d:/lis-integration-backend/reports/`

**Последние изменения:**
- Оптимизация batch processing для 22K+ записей
- Исправление фильтра дат (false positive на "с дифференцировкой 5")
- Добавление 15 анализаторов в V9/V10 миграции
