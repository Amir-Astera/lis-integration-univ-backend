package project.gigienist_reports.core.util

import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.text.Normalizer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FileNameUtil {
    /** Формат метки времени в имени: yyyy.MM.dd - HH.mm (без двоеточий → кроссплатформенно) */
    private val TS_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd - HH.mm")

    /** Разрешённые для "кириллического" режима символы: любые буквы (включая кириллицу), цифры и базовая пунктуация */
    private val SAFE_CHARS_ANY_LANG = Regex("[^\\p{L}\\p{N} _\\-().]")

    /** Для ASCII-режима: только латиница/цифры и базовая пунктуация */
    private val SAFE_ASCII = Regex("[^A-Za-z0-9 _\\-().]")

    /** Простая транслитерация RU → LAT (для ASCII-режима) */
    private fun translitRuToLat(src: String): String {
        val map = mapOf(
            'А' to "A",'Б' to "B",'В' to "V",'Г' to "G",'Д' to "D",'Е' to "E",'Ё' to "E",
            'Ж' to "Zh",'З' to "Z",'И' to "I",'Й' to "I",'К' to "K",'Л' to "L",'М' to "M",
            'Н' to "N",'О' to "O",'П' to "P",'Р' to "R",'С' to "S",'Т' to "T",'У' to "U",
            'Ф' to "F",'Х' to "Kh",'Ц' to "Ts",'Ч' to "Ch",'Ш' to "Sh",'Щ' to "Shch",
            'Ъ' to "","Ы" to "Y","Ь" to "","Э" to "E","Ю" to "Yu","Я" to "Ya",
            'а' to "a",'б' to "b",'в' to "v",'г' to "g",'д' to "d",'е' to "e",'ё' to "e",
            'ж' to "zh",'з' to "z",'и' to "i",'й' to "i",'к' to "k",'л' to "l",'м' to "m",
            'н' to "n",'о' to "o",'п' to "p",'р' to "r",'с' to "s",'т' to "t",'у' to "u",
            'ф' to "f",'х' to "kh",'ц' to "ts",'ч' to "ch",'ш' to "sh",'щ' to "shch",
            'ъ' to "","ы" to "y","ь" to "","э" to "e","ю" to "yu","я" to "ya"
        )
        val sb = StringBuilder(src.length * 2)
        for (ch in src) sb.append(map[ch] ?: ch)
        return sb.toString()
    }

    /**
     * Санитизация имени файла с сохранением национальных букв (в т.ч. кириллицы).
     * Убираем опасные символы, нормализуем, схлопываем пробелы/подчёркивания, ограничиваем длину.
     */
    fun sanitizeForFilename(raw: String, maxLen: Int = 150): String {
        var s = raw.trim()
        if (s.isEmpty()) return "report"

        // Нормализуем Unicode (NFC)
        s = Normalizer.normalize(s, Normalizer.Form.NFC)

        // Служебные символы файловых систем → "_"
        s = s.replace("""[\\/:*?"<>|]""".toRegex(), "_")

        // Убираем всё, кроме разрешённого множества (любой язык)
        s = s.replace(SAFE_CHARS_ANY_LANG, "_")

        // Схлопываем пробелы/подряд идущие "_"
        s = s.replace("\\s+".toRegex(), " ")
            .replace("_{2,}".toRegex(), "_")
            .replace(" _".toRegex(), "_")
            .replace("_ ".toRegex(), "_")
            .trim()

        // Ограничиваем длину
        if (s.length > maxLen) s = s.substring(0, maxLen).trim()

        return if (s.isBlank()) "report" else s
    }

    /**
     * Безопасное ASCII-имя: NFD → убираем диакритику, RU→LAT, выкидываем не ASCII,
     * убираем служебные символы, схлопываем, режем длину.
     */
    fun asciiSafeFilename(raw: String, maxLen: Int = 150): String {
        var s = raw.trim()
        if (s.isEmpty()) return "report"

        // NFD + убрать диакритику
        s = Normalizer.normalize(s, Normalizer.Form.NFD)
        s = s.replace("\\p{Mn}+".toRegex(), "")

        // Транслит RU→LAT
        s = translitRuToLat(s)

        // Служебные символы файловых систем → "_"
        s = s.replace("""[\\/:*?"<>|]""".toRegex(), "_")

        // Только ASCII-набор
        s = s.replace(SAFE_ASCII, "_")

        // Схлопываем
        s = s.replace("\\s+".toRegex(), " ")
            .replace("_{2,}".toRegex(), "_")
            .replace(" _".toRegex(), "_")
            .replace("_ ".toRegex(), "_")
            .trim()

        if (s.length > maxLen) s = s.substring(0, maxLen).trim()
        return if (s.isBlank()) "report" else s
    }

    /** Метка времени для имени файла (yyyy.MM.dd - HH.mm) */
    fun timestamp(now: LocalDateTime = LocalDateTime.now()): String = TS_FMT.format(now)

    /** JVM по умолчанию в UTF-8? */
    fun isUtf8DefaultCharset(): Boolean =
        Charset.defaultCharset().name().equals("UTF-8", ignoreCase = true)

    /** Если файл уже существует — добавляет суффикс " (1)", " (2)" ... */
    fun uniquePath(baseDir: Path, fileNameWithExt: String): Path {
        var p = baseDir.resolve(fileNameWithExt)
        if (!Files.exists(p)) return p

        val dot = fileNameWithExt.lastIndexOf('.')
        val name = if (dot > 0) fileNameWithExt.substring(0, dot) else fileNameWithExt
        val ext  = if (dot > 0) fileNameWithExt.substring(dot) else ""

        var i = 1
        while (true) {
            val candidate = baseDir.resolve("$name ($i)$ext")
            if (!Files.exists(candidate)) return candidate
            i++
        }
    }
}
