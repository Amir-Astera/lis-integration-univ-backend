package project.gigienist_reports.core.config.properties

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "labs")
data class MaterialsMappingProperties(
    var materialToTube: Map<String, String> = defaultMaterialToTube(),
    var tubeDisplay: Map<String, String> = defaultTubeDisplay()
) {
    val allTubeCodes: Set<String> get() = tubeDisplay.keys + materialToTube.values
}

@Configuration
@EnableConfigurationProperties(MaterialsMappingProperties::class)
class MaterialsMappingConfig

private fun defaultTubeDisplay() = linkedMapOf(
    "YELLOW"         to "Жёлтая пробирка (SST/сыворотка)",
    "LAVENDER"       to "Сиреневая пробирка (EDTA)",
    "BLUE"           to "Голубая пробирка (цитрат)",
    "GREEN"          to "Зелёная пробирка (гепарин)",
    "GREY"           to "Серая пробирка (фторид натрия)",
    "URINE_JAR"      to "Банка для мочи",
    "STOOL_JAR"      to "Контейнер для кала",
    "SWAB"           to "Тампон/транспортная среда",
    "SWAB_VTM"       to "Тампон в VTM",
    "MICRO_LAVENDER" to "Микропробирка EDTA",
    "MICRO_HEPARIN"  to "Микропробирка гепарин",
    "BACTO_RED"      to "Красная бак-пробирка",
    "BACTO_BLUE"     to "Синяя бак-пробирка"
)

private fun defaultMaterialToTube() = linkedMapOf(
    // кровь/сыворотка
    "Кровь"              to "LAVENDER",
    "Кровь венозная"     to "LAVENDER",
    "Плазма"             to "GREEN",
    "Сыворотка"          to "YELLOW",
    // моча/кал
    "Моча"               to "URINE_JAR",
    "Моча суточная"      to "URINE_JAR",
    "Фекалии"            to "STOOL_JAR",
    // тампоны
    "Мазок"              to "SWAB",
    "Мазок носоглоточный" to "SWAB_VTM",
    // глюкоза / цитрат / фторид
    "Кровь цитрат"       to "BLUE",
    "Кровь фторид"       to "GREY",
    // микроколлекция
    "Кровь капиллярная EDTA" to "MICRO_LAVENDER",
    "Кровь капиллярная гепарин" to "MICRO_HEPARIN",
    // бактериология
    "Посев материал (красная)" to "BACTO_RED",
    "Посев материал (синяя)"   to "BACTO_BLUE"
)


//@ConfigurationProperties(prefix = "labs")
//data class MaterialsMappingProperties(
//    val materialToTubeList: List<Entry> = emptyList(),
//    val tubeDisplay: Map<String, String> = emptyMap()
//) {
//    data class Entry(val key: String, val value: String)
//    val materialToTube: Map<String, String>
//        get() = materialToTubeList.associate { it.key to it.value }
//}
//// Вариант 1: явное включение
//@Configuration
//@EnableConfigurationProperties(MaterialsMappingProperties::class)
//class MaterialsMappingConfig