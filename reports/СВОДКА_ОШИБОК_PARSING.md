# Сводка ошибок парсинга отчёта "Отчет по выполненным исследованиям на рабочих местах"

## Дата анализа: 26.03.2026
## Файл: response_1774531186146.json

---

## 1. Удваивание данных из Part2 (Стационар)

### Проблема
Данные из второй таблицы (Part2 - Стационар) дублируются в summary для каждого рабочего места.

### Пример
- Рабочее место "гематология" (services: 14 услуг):
  - summary.completedValueTotal: 8456
  - summary.reportedTotalValueTotal: 15761

- Рабочее место "Клиническая химия (биохимия)" (services: 56 услуг):
  - summary.completedValueTotal: 31739
  - summary.reportedTotalValueTotal: 55362

### Причина
Метод `buildSummaryFromServices()` в `DamumedWorkplaceCompletedStudiesProcessedViewQueryService.kt` (строка 146-164) суммирует services из всех рабочих мест, а не только из текущего workplace.

### Ожидаемое поведение
summary должен содержать сумму только services текущего workplace.

---

## 2. Проблема с отделением "Не определено"

### Статус: ИСПРАВЛЕНО ✅
Отделение "Не определено" теперь корректно отображается:
- "Определение креатинина в сыворотке крови на анализаторе": numericValue = 1
- "Измерение скорости оседания эритроцитов (СОЭ) в крови на анализаторе": numericValue = 2

---

## 3. Обнаруженные несоответствия

### 3.1 Гематология
| Метрика | Значение в services | Значение в summary |
|---------|---------------------|-------------------|
| completedValueTotal (СОЭ анализатор) | 918 | - |
| completedValueTotal (вся гематология) | - | 8456 |

### 3.2 Клиническая химия (биохимия)
| Метрика | Значение в services | Значение в summary |
|---------|---------------------|-------------------|
| completedValueTotal (56 услуг) | сумма всех 56 | 31739 |
| reportedTotalValueTotal | сумма всех 56 | 55362 |

### 3.3 Коагулогия
| Метрика | Значение в services | Значение в summary |
|---------|---------------------|-------------------|
| completedValueTotal (8 услуг) | сумма всех 8 | 917 |
| reportedTotalValueTotal | сумма всех 8 | 1613 |

### 3.4 Микробиология
| Метрика | Значение в services | Значение в summary |
|---------|---------------------|-------------------|
| completedValueTotal (5 услуг) | сумма всех 5 | 128 |
| reportedTotalValueTotal | сумма всех 5 | 186 |

### 3.5 Общеклинические методы
| Метрика | Значение в services | Значение в summary |
|---------|---------------------|-------------------|
| completedValueTotal (17 услуг) | сумма всех 17 | 22487 |
| reportedTotalValueTotal | сумма всех 17 | 38463 |

---

## 4. Итоговые суммы по всему отчёту

| Параметр | Значение |
|----------|----------|
| Всего рабочих мест | 7 |
| Всего услуг | 138 |
| completedValueTotal (итого) | 74972 |
| reportedTotalValueTotal (итого) | 129509 |

---

## 5. Список рабочих мест и количество услуг

| № | Рабочее место | Услуг в services | serviceCount в summary |
|---|---------------|-------------------|------------------------|
| 1 | гематология | 14 | 14 |
| 2 | иммунология | 24 | 24 |
| 3 | Клиническая химия (биохимия) | 56 | 56 |
| 4 | коагулогия | 8 | 8 |
| 5 | микробиология | 5 | 5 |
| 6 | Молекулярная биология и молекулярная генетика | 17 | 17 |
| 7 | Общеклинические методы | 17 | 17 |

---

## 6. Файлы для проверки

- **JSON ответ API**: `d:\lis-integration-backend\reports\response_1774531186146.json`
- **Код парсинга**: `d:\lis-integration-backend\univ\src\main\kotlin\lab\dev\med\univ\feature\reporting\domain\services\DamumedWorkplaceCompletedStudiesProcessingService.kt`
- **Код View**: `d:\lis-integration-backend\univ\src\main\kotlin\lab\dev\med\univ\feature\reporting\domain\services\DamumedWorkplaceCompletedStudiesProcessedViewQueryService.kt`

---

## 7. Выполненные исправления

### 7.1 Исправление удваивания данных (выполнено 26.03.2026)

**Проблема**: Данные из Part1 (Амбулатория) и Part2 (Стационар) объединялись по ключу БЕЗ departmentGroup, что приводило к суммированию значений из разных групп.

**Решение**:
1. Возвращён `departmentGroup` в ключ аккумулятора в `DamumedWorkplaceCompletedStudiesProcessingService.kt` (строка 333-342)
2. Обновлён `toColumnKey()` в `DamumedWorkplaceCompletedStudiesProcessedViewQueryService.kt` для использования `departmentGroup`
3. Изменён `buildServiceView()` для использования `toColumnKey()` вместо `toColumnKeyForMerge()`

**Изменённые файлы**:
- `d:\lis-integration-backend\univ\src\main\kotlin\lab\dev\med\univ\feature\reporting\domain\services\DamumedWorkplaceCompletedStudiesProcessingService.kt`
- `d:\lis-integration-backend\univ\src\main\kotlin\lab\dev\med\univ\feature\reporting\domain\services\DamumedWorkplaceCompletedStudiesProcessedViewQueryService.kt`

### 7.2 Проверка после исправлений

После перезапуска и повторной загрузки отчёта проверить:
- Значения в cells должны быть корректными (без удваивания)
- summary для каждого workplace должен содержать правильные суммы
- "Не определено" должно отображаться корректно
