package project.gigienist_reports.core.config.enums

enum class PatientSex(private val sex: String) {
    MALE("0"),
    FEMALE("1");

    companion object {
        fun fromCode(code: String): PatientSex {
            return entries.first { it.sex == code }
        }

        fun fromValue(value: String): String {
            return entries.first { it.name == value }.ordinal.toString()
        }
    }
}