package lab.dev.med.univ.feature.reagents.domain.models

import java.time.LocalDate
import java.time.LocalDateTime

enum class AnalyzerType {
    HEMATOLOGY,
    BIOCHEMISTRY,
    COAGULATION,
    URINALYSIS,
    ESR,
    IMMUNOASSAY,
    BLOOD_GAS,
    IMMUNOHEMATOLOGY,
    POCT_COAGULATION,
    POCT_IMMUNOASSAY,
    INFRASTRUCTURE,
}

enum class ReagentOperationType {
    PATIENT_TEST,
    STARTUP,
    SHUTDOWN,
    QC,
    CALIBRATION,
    STANDBY_EXIT_SHORT,
    STANDBY_EXIT_MEDIUM,
    STANDBY_EXIT_LONG,
    BACKGROUND,
    ENHANCED_CLEAN,
}

enum class ReagentUnitType {
    // Объемные единицы
    ML,           // Миллилитры
    LITER,        // Литры
    UL,           // Микролитры (μL)

    // Массовые единицы
    MG,           // Миллиграммы
    G,            // Граммы
    KG,           // Килограммы

    // Международные единицы (для биологических препаратов)
    IU,           // МЕ - международные единицы
    MILLI_IU,     // мМЕ - милли-международные единицы

    // Дискретные единицы
    PIECE,        // Штуки
    TEST,         // Тесты (анализы)
    KIT,          // Наборы

    // Упаковочные единицы
    BOX,          // Коробки
    PACK,         // Упаковки/пачки
    CASE,         // Ящики/кейсы

    // Контейнерные единицы
    BOTTLE,       // Флаконы
    VIAL,         // Виалы (мини-флаконы)
    AMPOULE,      // Ампулы
    TUBE,         // Тюбики
    CANISTER,     // Канистры
    CARTRIDGE,    // Картриджи
    CASSETTE,     // Кассеты

    // Специфические для анализаторов
    TEST_POSITION, // Кассетные позиции (iFlash)
    STRIP,        // Тест-полоски
    SLIDE,        // Слайды
    CHIP,         // Чипы/карты
    DISK,         // Диски
    PLATE,        // Планшеты/пластины
    WELL,         // Лунки (well plates)
}

enum class ReagentInventoryStatus {
    IN_STOCK,
    OPENED,
    DEPLETED,
    EXPIRED,
    DISPOSED,
}

enum class ConsumableCategory {
    // Пробирки и контейнеры для образцов
    TUBE,              // Пробирки (вакуумные, обычные)
    TUBE_MICRO,        // Микропробирки (Eppendorf, PCR)
    CONTAINER,         // Контейнеры для сбора биоматериала
    URINE_CONTAINER,   // Контейнеры для мочи
    STOOL_CONTAINER,   // Контейнеры для кала

    // Дозирование и перенос жидкостей
    TIP,               // Наконечники для дозаторов/пипеток
    PIPETTE,           // Пипетки (одноразовые, пастеровские)
    PIPETTE_TIP_BOX,   // Штативы/коробки для наконечников
    SYRINGE,           // Шприцы (медицинские, инъекционные)
    NEEDLE,            // Иглы (инъекционные, для забора крови)

    // Кюветы и реакционные сосуды
    CUVETTE,           // Кюветы/кюветные ячейки
    REACTION_VESSEL,   // Реакционные сосуды
    WELL_PLATE,        // Планшеты с лунками (96-well, 384-well)

    // Карты и тест-системы
    CARD,              // Гель-карты (иммуногематология)
    CARTRIDGE,         // Картриджи (POCT анализаторы)
    CASSETTE,          // Кассеты (iFlash и аналоги)
    STRIP,             // Тест-полоски
    SLIDE,             // Предметные стёкла
    COVER_SLIP,        // Покровные стёкла
    CHIP,              // Чипы/карты микрофлюидные

    // Инструменты для забора крови
    HOLDER,            // Держатели для пробирок
    BUTTERFLY,         // Катетер-бабочка (игла-бабочка)
    LANCET,            // Ланцеты/скарификаторы
    TOURNIQUET,        // Жгуты кровоостанавливающие

    // Средства индивидуальной защиты (СИЗ)
    GLOVE,             // Перчатки (нитриловые, латексные, виниловые)
    MASK,              // Маски медицинские
    SHIELD,            // Защитные щитки/экраны
    GOWN,              // Халаты/одноразовые костюмы
    CAP,               // Шапочки/бахилы

    // Расходные материалы для очистки и дезинфекции
    WIPE,              // Салфетки/тампоны
    COTTON,            // Ватные шарики/палочки
    SPONGE,            // Губки медицинские
    GAUZE,             // Марля/бинты

    // Упаковочные материалы
    BAG,               // Пакеты (стерильные, для проб)
    ZIP_BAG,           // Зип-пакеты
    LABEL,             // Этикетки/стикеры
    TAPE,              // Лента клейкая/скотч
    FOIL,              // Фольга/парафильм

    // Жидкости и растворы
    DISTILLED_WATER,   // Дистиллированная вода
    DEIONIZED_WATER,   // Деионизированная вода
    BUFFER,            // Буферные растворы
    SALINE,            // Физраствор (NaCl 0.9%)
    DISINFECTANT,      // Дезинфицирующие средства
    DETERGENT,         // Моющие средства
    GEL,               // Гель для УЗИ
    PARAFFIN,          // Парафин жидкий/твердый

    // Фильтры и мембраны
    FILTER,            // Фильтры (бумажные, мембранные)
    SYRINGE_FILTER,    // Шприцевые фильтры
    FUNNEL,            // Воронки

    // Прочие расходники
    CAPILLARY,         // Капилляры (гематокритные)
    STOPPER,           // Пробки/заглушки
    RACK,              // Штативы/стойки
    FREEZER_BOX,       // Криоконтейнеры/коробки для заморозки
    PARAFILM,          // Парафильм

    // Оборудование и принадлежности
    PRINTER_PAPER,     // Термобумага для принтеров
    INK_CARTRIDGE,     // Картриджи для принтеров
    CALIBRATOR,        // Калибраторы/контрольные материалы
    QC_MATERIAL,       // Контрольные материалы качества

    OTHER,             // Прочее
}

enum class TubeColor {
    PURPLE,
    BLUE,
    RED,
    YELLOW,
    GREEN,
    GRAY,
}

enum class AnalyzerLogSourceType {
    APPLOGS,
    ERRORS_XML,
    USB_EXPORT,
}

enum class AnalyzerLogParseStatus {
    PENDING,
    PROCESSING,
    PARSED,
    FAILED,
}

enum class SampleClassification {
    LEGITIMATE,
    SUSPICIOUS,
    PROBABLE_RERUN,
    WASH_TEST,
    QC,
    CALIBRATION,
    ERROR,
}

data class Analyzer(
    val id: String,
    val name: String,
    val type: AnalyzerType,
    val workplaceName: String,
    val lisDeviceSystemName: String? = null,
    val lisAnalyzerId: Int? = null,
    val lisDeviceName: String? = null,
    val serialNumber: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
)

data class AnalyzerReagentRate(
    val id: String,
    val analyzerId: String,
    val reagentName: String,
    val operationType: ReagentOperationType,
    val testMode: String? = null,
    val volumePerOperationMl: Double? = null,
    val unitsPerOperation: Int? = null,
    val unitType: ReagentUnitType,
    val sourceDocument: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
)

data class ReagentInventory(
    val id: String,
    val analyzerId: String? = null,
    val reagentName: String,
    val lotNumber: String? = null,
    val manufacturer: String? = null,
    val expiryDateSealed: LocalDate? = null,
    val stabilityDaysAfterOpening: Int? = null,
    val openedDate: LocalDate? = null,
    val totalVolumeMl: Double? = null,
    val totalUnits: Int? = null,
    val unitType: ReagentUnitType,
    val unitPriceTenge: Double? = null,
    val status: ReagentInventoryStatus = ReagentInventoryStatus.IN_STOCK,
    val receivedAt: LocalDate,
    val receivedBy: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
)

data class ConsumableInventory(
    val id: String,
    val name: String,
    val category: ConsumableCategory,
    val tubeColor: TubeColor? = null,
    val linkedAnalyzerTypes: String? = null,
    val linkedServiceKeywords: String? = null,
    val quantityTotal: Int,
    val quantityRemaining: Int,
    val unitPriceTenge: Double? = null,
    val lotNumber: String? = null,
    val expiryDate: LocalDate? = null,
    val receivedAt: LocalDate,
    val receivedBy: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
)

data class AnalyzerLogUpload(
    val id: String,
    val analyzerId: String? = null,
    val sourceType: AnalyzerLogSourceType,
    val originalFileName: String,
    val storedFileName: String,
    val storagePath: String,
    val fileSizeBytes: Long,
    val checksumSha256: String,
    val parseStatus: AnalyzerLogParseStatus = AnalyzerLogParseStatus.PENDING,
    val parseStartedAt: LocalDateTime? = null,
    val parseCompletedAt: LocalDateTime? = null,
    val parseErrorMessage: String? = null,
    val totalLinesParsed: Int = 0,
    val totalSamplesFound: Int = 0,
    val legitimateSamples: Int = 0,
    val unauthorizedSamples: Int = 0,
    val washTestSamples: Int = 0,
    val rerunSamples: Int = 0,
    val logPeriodStart: LocalDateTime? = null,
    val logPeriodEnd: LocalDateTime? = null,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    val uploadedBy: String? = null,
    val version: Long? = null,
)

data class ParsedAnalyzerSample(
    val id: String,
    val logUploadId: String,
    val analyzerId: String? = null,
    val sampleTimestamp: LocalDateTime,
    val barcode: String,
    val deviceSystemName: String? = null,
    val deviceName: String? = null,
    val lisAnalyzerId: Int? = null,
    val testMode: String? = null,
    val bloodMode: String? = null,
    val takeMode: String? = null,
    val orderResearchId: Long? = null,
    val orderId: Long? = null,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val hasLisOrder: Boolean,
    val sampleRequestCount: Int = 0,
    val wbcValue: Double? = null,
    val rbcValue: Double? = null,
    val hgbValue: Double? = null,
    val pltValue: Double? = null,
    val classification: SampleClassification,
    val classificationReason: String? = null,
    val correlatedLegitimateSampleId: String? = null,
    val estimatedDiluentMl: Double? = null,
    val estimatedDiffLyseMl: Double? = null,
    val estimatedLhLyseMl: Double? = null,
    val estimatedCostTenge: Double? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
)

data class ReagentConsumptionReport(
    val id: String,
    val analyzerId: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val legitimateTestCount: Int = 0,
    val legitimateReagentConsumptionJson: String? = null,
    val legitimateCostTenge: Double = 0.0,
    val serviceOperationsJson: String? = null,
    val serviceReagentConsumptionJson: String? = null,
    val serviceCostTenge: Double = 0.0,
    val suspiciousTestCount: Int = 0,
    val rerunTestCount: Int = 0,
    val washTestCount: Int = 0,
    val unauthorizedReagentConsumptionJson: String? = null,
    val unauthorizedCostTenge: Double = 0.0,
    val inventoryStartJson: String? = null,
    val inventoryReceivedJson: String? = null,
    val inventoryEndExpectedJson: String? = null,
    val inventoryEndActualJson: String? = null,
    val discrepancyJson: String? = null,
    val discrepancyTotalTenge: Double = 0.0,
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    val generatedBy: String? = null,
    val version: Long? = null,
)
