package project.gigienist_reports.core.concurrency

import org.springframework.stereotype.Component
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@Component
class ExcelLimiter {
    private val sem = Semaphore(permits = 4) // настроить по ресурсам
    suspend fun <T> withPermit(block: suspend () -> T): T = sem.withPermit { block() }
}