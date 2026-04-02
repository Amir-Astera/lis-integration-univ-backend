package project.gigienist_reports.core.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("jasper")
data class JasperProperties(
    val javaHome: String, //# путь к директории с java, на разных образах и установках путь
    val absolutePath: String, //# Абсолютный путь к приложению
    val pdfFilesPath: String, // # Путь относительно абсолютного пути приложения, тут будут храниться pdf
    val absoluteTemplatePath: String, //# Абсолютный путь к директории с шаблоном бланка
    val templateMainFilePath: String,// # Точка входа, главный файл с которого запускается формирование pdf
    val absoluteJasperStarterPath: String, // # Абсолютный путь к директории с jasperstarter
    val jasperStarterJarFilePath: String, // # Относительный путь к приложению jasperStarter внутри директории с jasperstarter
    val jasperStarterConfFilePath: String // # Относительный путь к конфигу jasperStarter внутри директории с jasperstarter
)