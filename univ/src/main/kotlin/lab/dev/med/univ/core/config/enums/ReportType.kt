package project.gigienist_reports.core.config.enums

enum class ReportType {
    BY_PATIENT,
    MATERIALS,
    COUNT_PATIENT,
    STAT_ONE_ONE,
    RESEARCH_STATUS_I_S
}

//Full command: [C:\Program Files\Eclipse Adoptium\jdk-8.0.442.6-hotspot\bin\java, -jar, D:\gigienist-reports\jasper\lib\jasperstarter.jar,
//process, D:\gigienist-reports\jasper\main\customer_specimen.jasper, -f, xlsx, -o,
//D:\gigienist-reports\files\reports/MATERIALS/ad9f76d7-ec70-4eb5-9725-b6ddae1f329620250815025748,
//-P, ROOT_REPORT_PATH=D:\gigienist-reports\jasper\main,
//REPORT_CODE=customer_specimen,
//REPORT_NAME=Зарегистрированные материалы по заказчикам,
//DATE_FROM=2025-08-01 00:00:00, DATE_TO=2025-08-14 00:00:00,
//REPORT_PARAMETERS_LIST=Отчет сформирован автоматический: дата с 01.08.2025; по 14.08.2025;,
//@D:\gigienist-reports\jasper\jdbc\dbPostgreSQL.conf, -u, postgres, -p, postgres]
//
//
//app-1  | Full command: [/usr/lib/jvm/java-8-openjdk-amd64/bin/java, -jar, /opt/jasper/lib/jasperstarter.jar,
//process, /opt/jasper/main/customer_specimen.jasper, -f, xlsx, -o,
///opt/gigienist/files/reports/MATERIALS/feac490c-cf3a-4710-9f75-6878394c00b720250815095424,
//-P, ROOT_REPORT_PATH=/opt/jasper/main,
//MAN_ID=1,
//REPORT_CODE=customer_specimen,
//REPORT_NAME=?????????????????? ????????? ?? ??????????,
//DATE_FROM=2025-08-01 00:00:00, DATE_TO=2025-08-14 00:00:00,
//REPORT_PARAMETERS_LIST=????? ??????????? ??????????????: ???? ? 01.08.2025; ?? 14.08.2025;,
//@/opt/jasper/jdbc/dbPostgreSQL.conf, -u, postgres, -p, postgres]
