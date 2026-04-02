package project.gigienist_reports.core.config.enums

enum class Deadline(val value: String) {
    DAY("1 рабочее время"),
    DAYS("1-2 рабочее время"),
    SEVERAL_DAYS("2-3 рабочее время"),
    PAST_WEEK("3-5 рабочее время"),
    WEEK("5-7 рабочее время")
}