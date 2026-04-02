package lab.dev.med.univ.feature.reporting.domain.models

enum class DamumedReportSemanticRole {
    REGISTRY,
    FACT,
    QUALITY_CONTROL,
    MATERIAL_USAGE,
    COST_ESTIMATE,
    POSITIVE_RESULT_MONITORING,
}

enum class DamumedReportAxisType {
    PERIOD,
    WORKPLACE,
    SERVICE,
    DEPARTMENT_GROUP,
    DEPARTMENT,
    MATERIAL,
    EMPLOYEE,
    IDENTIFIER,
    PATIENT,
    ORGANIZATION,
    FUNDING_SOURCE,
    DIAGNOSIS,
    STATUS,
    DATE_TIME,
    COST,
    RESULT_FLAG,
    TOTAL,
}

data class DamumedReportAxisProfile(
    val key: String,
    val type: DamumedReportAxisType,
    val aliases: Set<String>,
    val repeatedAcrossBlocks: Boolean = false,
    val canBeMergedHeader: Boolean = false,
)

data class DamumedReportMetricProfile(
    val key: String,
    val aliases: Set<String>,
    val numeric: Boolean = true,
    val mayBeFormulaBacked: Boolean = true,
    val representsTotal: Boolean = false,
)

data class DamumedReportNormalizationProfile(
    val reportKind: DamumedLabReportKind,
    val semanticRole: DamumedReportSemanticRole,
    val titleAliases: Set<String>,
    val dimensions: List<DamumedReportAxisProfile>,
    val metrics: List<DamumedReportMetricProfile>,
    val relationHints: Set<String>,
)

object DamumedReportSchemaCatalog {
    val profiles: Map<DamumedLabReportKind, DamumedReportNormalizationProfile> = listOf(
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES,
            semanticRole = DamumedReportSemanticRole.FACT,
            titleAliases = setOf("Отчет по выполненным исследованиям на рабочих местах"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("workplace", DamumedReportAxisType.WORKPLACE, setOf("рабочее место"), repeatedAcrossBlocks = true, canBeMergedHeader = true),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("услуга", "исследование"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("department_group", DamumedReportAxisType.DEPARTMENT_GROUP, setOf("амбулатория", "стационар"), repeatedAcrossBlocks = true, canBeMergedHeader = true),
                DamumedReportAxisProfile("department", DamumedReportAxisType.DEPARTMENT, setOf("отделение"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("total", DamumedReportAxisType.TOTAL, setOf("всего"), repeatedAcrossBlocks = true),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("completed_count", setOf("количество", "выполнено", "выполненных услуг")),
                DamumedReportMetricProfile("total_count", setOf("всего"), representsTotal = true),
            ),
            relationHints = setOf(
                "service-to-department-many-to-many",
                "workplace-splits-report-into-repeating-blocks",
                "department-group-is-higher-level-than-department",
            ),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL,
            semanticRole = DamumedReportSemanticRole.REGISTRY,
            titleAliases = setOf("ЖУРНАЛ регистрации лабораторных направлений"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("referral_number", DamumedReportAxisType.IDENTIFIER, setOf("№ направления", "направления"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("patient_iin", DamumedReportAxisType.IDENTIFIER, setOf("иин")),
                DamumedReportAxisProfile("patient_name", DamumedReportAxisType.PATIENT, setOf("фио пациента", "пациент")),
                DamumedReportAxisProfile("birth_date", DamumedReportAxisType.DATE_TIME, setOf("дата рождения")),
                DamumedReportAxisProfile("referring_organization", DamumedReportAxisType.ORGANIZATION, setOf("организация направитель", "медицинская организация")),
                DamumedReportAxisProfile("patient_department", DamumedReportAxisType.DEPARTMENT, setOf("отделение пациента", "отделение")),
                DamumedReportAxisProfile("funding_source", DamumedReportAxisType.FUNDING_SOURCE, setOf("источник финансирования")),
                DamumedReportAxisProfile("medical_record_number", DamumedReportAxisType.IDENTIFIER, setOf("№ истории болезни", "амбулаторной карты пациента")),
                DamumedReportAxisProfile("diagnosis", DamumedReportAxisType.DIAGNOSIS, setOf("диагноз")),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("наименование услуги", "услуга", "исследование")),
                DamumedReportAxisProfile("material", DamumedReportAxisType.MATERIAL, setOf("вид материала", "материал")),
                DamumedReportAxisProfile("emergency_flag", DamumedReportAxisType.RESULT_FLAG, setOf("экстренное")),
                DamumedReportAxisProfile("referral_status", DamumedReportAxisType.STATUS, setOf("статус направления", "статус")),
                DamumedReportAxisProfile("service_cost", DamumedReportAxisType.COST, setOf("стоимость услуги", "стоимость")),
                DamumedReportAxisProfile("received_at", DamumedReportAxisType.DATE_TIME, setOf("дата получения")),
                DamumedReportAxisProfile("completed_at", DamumedReportAxisType.DATE_TIME, setOf("дата выполнения")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("referral_count", setOf("направление", "строка журнала", "количество направлений")),
            ),
            relationHints = setOf(
                "source-registry-row-per-service",
                "referral-number-groups-multi-service-rows",
            ),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.COMPLETED_LAB_STUDIES_JOURNAL,
            semanticRole = DamumedReportSemanticRole.REGISTRY,
            titleAliases = setOf("ЖУРНАЛ регистрации выполненных лабораторных исследований"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("referral_number", DamumedReportAxisType.IDENTIFIER, setOf("№ направления", "направления", "номер направления"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("registry_number", DamumedReportAxisType.IDENTIFIER, setOf("№")),
                DamumedReportAxisProfile("referral_status", DamumedReportAxisType.STATUS, setOf("статус направления", "статус направ- ления", "статус")),
                DamumedReportAxisProfile("patient_iin", DamumedReportAxisType.IDENTIFIER, setOf("иин")),
                DamumedReportAxisProfile("patient_name", DamumedReportAxisType.PATIENT, setOf("фио пациента", "пациент")),
                DamumedReportAxisProfile("patient_rpn_id", DamumedReportAxisType.IDENTIFIER, setOf("rpnid пациента", "rpnid")),
                DamumedReportAxisProfile("birth_date", DamumedReportAxisType.DATE_TIME, setOf("дата рождения")),
                DamumedReportAxisProfile("sample_collected_at", DamumedReportAxisType.DATE_TIME, setOf("дата взятия биоматериала", "дата взятия")),
                DamumedReportAxisProfile("emergency_flag", DamumedReportAxisType.RESULT_FLAG, setOf("экстренно", "срочность")),
                DamumedReportAxisProfile("organization", DamumedReportAxisType.ORGANIZATION, setOf("медицинская организация", "организация", "организация выполнившая заказ")),
                DamumedReportAxisProfile("referring_employee", DamumedReportAxisType.EMPLOYEE, setOf("направивший врач", "врач")),
                DamumedReportAxisProfile("department", DamumedReportAxisType.DEPARTMENT, setOf("отделение")),
                DamumedReportAxisProfile("medical_record_number", DamumedReportAxisType.IDENTIFIER, setOf("№ истории болезни", "история болезни", "амбулаторной карты пациента")),
                DamumedReportAxisProfile("diagnosis", DamumedReportAxisType.DIAGNOSIS, setOf("диагноз")),
                DamumedReportAxisProfile("service_category", DamumedReportAxisType.SERVICE, setOf("категория услуги", "профиль исследования", "вид исследования")),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("наименование услуги", "услуга", "исследование")),
                DamumedReportAxisProfile("completed_at", DamumedReportAxisType.DATE_TIME, setOf("дата выполнения", "дата результата", "дата завершения")),
                DamumedReportAxisProfile("result_text", DamumedReportAxisType.RESULT_FLAG, setOf("результат", "результат исследования")),
                DamumedReportAxisProfile("performer", DamumedReportAxisType.EMPLOYEE, setOf("исполнитель", "выполнил", "лаборант")),
                DamumedReportAxisProfile("funding_source", DamumedReportAxisType.FUNDING_SOURCE, setOf("источник финансирования")),
                DamumedReportAxisProfile("service_price", DamumedReportAxisType.COST, setOf("стоимость", "цена")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("completed_count", setOf("выполнено", "количество", "число исследований")),
                DamumedReportMetricProfile("service_total_cost", setOf("сумма", "стоимость итого", "стоимость")),
            ),
            relationHints = setOf(
                "registry-row-per-completed-service",
                "referral-number-groups-multi-service-rows",
                "registry-can-be-reconciled-with-workplace-report",
            ),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.POSITIVE_RESULTS_JOURNAL,
            semanticRole = DamumedReportSemanticRole.POSITIVE_RESULT_MONITORING,
            titleAliases = setOf("Журнал исследований с положительным результатом"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("registry_number", DamumedReportAxisType.IDENTIFIER, setOf("№"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("service_code", DamumedReportAxisType.IDENTIFIER, setOf("код услуги")),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("наименование услуги", "услуга", "исследование")),
                DamumedReportAxisProfile("result_parameter", DamumedReportAxisType.RESULT_FLAG, setOf("параметр исследования", "параметр", "результат")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("completed_service_count", setOf("количество выполненных услуг", "выполненных услуг", "количество")),
                DamumedReportMetricProfile("positive_result_count", setOf("количество положительных результатов", "положительных результатов", "положительных")),
                DamumedReportMetricProfile("patient_count", setOf("количество пациентов", "пациентов")),
                DamumedReportMetricProfile("service_cost", setOf("стоимость услуги", "стоимость")),
                DamumedReportMetricProfile("service_total_cost", setOf("сумма по выполненным услугам", "сумма")),
            ),
            relationHints = setOf(
                "source-row-per-positive-result-aggregate",
                "positive-result-parameter-is-row-grain",
                "joins-to-completed-studies-by-service-period",
            ),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.REJECT_LOG,
            semanticRole = DamumedReportSemanticRole.QUALITY_CONTROL,
            titleAliases = setOf("Бракеражный журнал"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("registry_number", DamumedReportAxisType.IDENTIFIER, setOf("№")),
                DamumedReportAxisProfile("order_number", DamumedReportAxisType.IDENTIFIER, setOf("номер заказа", "заказ"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("patient", DamumedReportAxisType.PATIENT, setOf("иин - фио", "пациент")),
                DamumedReportAxisProfile("planned_completion_at", DamumedReportAxisType.DATE_TIME, setOf("планируемая дата выполнения")),
                DamumedReportAxisProfile("sender_organization", DamumedReportAxisType.ORGANIZATION, setOf("организация отправителя", "отправитель")),
                DamumedReportAxisProfile("reject_reason", DamumedReportAxisType.STATUS, setOf("причина бракеража", "бракераж")),
                DamumedReportAxisProfile("action_taken", DamumedReportAxisType.STATUS, setOf("принятые меры", "меры")),
                DamumedReportAxisProfile("referring_doctor", DamumedReportAxisType.EMPLOYEE, setOf("направивший врач", "врач")),
                DamumedReportAxisProfile("registered_by", DamumedReportAxisType.EMPLOYEE, setOf("фио пользователя зарегистрировавшего брак", "зарегистрировавшего брак")),
                DamumedReportAxisProfile("photo_flag", DamumedReportAxisType.RESULT_FLAG, setOf("наличие фото брака", "фото брака")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("rejected_count", setOf("брак", "строка журнала", "количество брака", "отклонено")),
            ),
            relationHints = setOf(
                "quality-signal-for-referrals-and-completed-studies",
                "source-registry-row-per-reject-entry",
                "order-number-groups-merged-row-blocks",
            ),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.CONSUMABLE_COST_QUANTITY,
            semanticRole = DamumedReportSemanticRole.MATERIAL_USAGE,
            titleAliases = setOf("Количественный отчет по затратам расходных материалов лаборатории"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("material", DamumedReportAxisType.MATERIAL, setOf("материал", "расходный материал")),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("услуга", "исследование")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("consumption_quantity", setOf("количество", "расход")),
            ),
            relationHints = setOf("joins-to-service-activity-for-cost-per-service"),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.CONSUMABLE_COST_LIST,
            semanticRole = DamumedReportSemanticRole.MATERIAL_USAGE,
            titleAliases = setOf("Списочный отчет по затратам расходных материалов лаборатории"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("material", DamumedReportAxisType.MATERIAL, setOf("материал", "расходный материал")),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("услуга", "исследование")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("consumption_quantity", setOf("количество", "расход")),
            ),
            relationHints = setOf("detail-version-of-consumable-quantity-report"),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.LAB_EXPENSE_ESTIMATE_QUANTITY,
            semanticRole = DamumedReportSemanticRole.COST_ESTIMATE,
            titleAliases = setOf("Количественный отчет по оценке расходов лаборатории"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("услуга", "исследование")),
                DamumedReportAxisProfile("material", DamumedReportAxisType.MATERIAL, setOf("материал", "расходный материал")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("estimated_cost_quantity", setOf("сумма", "стоимость", "расход")),
            ),
            relationHints = setOf("estimated-cost-can-be-compared-to-actual-consumption"),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.GOBMP_COMPLETED_SERVICES,
            semanticRole = DamumedReportSemanticRole.FACT,
            titleAliases = setOf("Отчет Выполненные услуги по ГОБМП, ГОБМП-2"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("registry_number", DamumedReportAxisType.IDENTIFIER, setOf("№"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("referring_branch", DamumedReportAxisType.ORGANIZATION, setOf("филиал направитель", "направитель")),
                DamumedReportAxisProfile("executor_organization", DamumedReportAxisType.ORGANIZATION, setOf("мо исполнитель", "исполнитель")),
                DamumedReportAxisProfile("laboratory", DamumedReportAxisType.WORKPLACE, setOf("лаборатория")),
                DamumedReportAxisProfile("department", DamumedReportAxisType.DEPARTMENT, setOf("отдел лаборатории", "отделение")),
                DamumedReportAxisProfile("service_code", DamumedReportAxisType.IDENTIFIER, setOf("код услуги")),
                DamumedReportAxisProfile("service", DamumedReportAxisType.SERVICE, setOf("наименование услуги", "услуга", "исследование")),
                DamumedReportAxisProfile("service_registry_id", DamumedReportAxisType.IDENTIFIER, setOf("id услуги", "ид услуги", "номер записи")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("completed_count", setOf("количество услуг", "количество", "выполнено")),
                DamumedReportMetricProfile("unit_price", setOf("цена", "стоимость")),
                DamumedReportMetricProfile("total_price", setOf("общая цена", "сумма")),
            ),
            relationHints = setOf(
                "source-row-per-service-aggregate",
                "page-header-repeats-between-data-blocks",
                "joins-to-completed-studies-by-service-and-period",
            ),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.REFERRAL_COUNT_BY_MATERIAL,
            semanticRole = DamumedReportSemanticRole.FACT,
            titleAliases = setOf("Отчет по количеству направлений на лабораторные исследования"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("material", DamumedReportAxisType.MATERIAL, setOf("материал")),
                DamumedReportAxisProfile("period_bucket", DamumedReportAxisType.PERIOD, setOf("месяц", "всего за год", "итого")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("referral_count", setOf("количество", "направлений")),
            ),
            relationHints = setOf("material-to-period-bucket-fact-table"),
        ),
        DamumedReportNormalizationProfile(
            reportKind = DamumedLabReportKind.EMPLOYEE_COMPLETED_STUDIES_SUMMARY,
            semanticRole = DamumedReportSemanticRole.FACT,
            titleAliases = setOf("Отчёт по выполненным лабораторным исследованиям"),
            dimensions = listOf(
                DamumedReportAxisProfile("period", DamumedReportAxisType.PERIOD, setOf("период"), canBeMergedHeader = true),
                DamumedReportAxisProfile("employee", DamumedReportAxisType.EMPLOYEE, setOf("фио сотрудника", "сотрудник", "исполнитель"), repeatedAcrossBlocks = true),
                DamumedReportAxisProfile("period_bucket", DamumedReportAxisType.PERIOD, setOf("месяц", "всего за год", "итого")),
            ),
            metrics = listOf(
                DamumedReportMetricProfile("completed_service_count", setOf("количество услуг")),
                DamumedReportMetricProfile("completed_patient_count", setOf("количество пациентов")),
            ),
            relationHints = setOf("employee-to-metric-and-period-bucket-fact-table"),
        ),
    ).associateBy { it.reportKind }

    fun profileFor(reportKind: DamumedLabReportKind): DamumedReportNormalizationProfile {
        return profiles.getValue(reportKind)
    }
}
