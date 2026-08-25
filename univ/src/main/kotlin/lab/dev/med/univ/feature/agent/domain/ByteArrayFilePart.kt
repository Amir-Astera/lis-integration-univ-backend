package lab.dev.med.univ.feature.agent.domain

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.codec.multipart.FilePart
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Path

/**
 * Synthetic FilePart backed by an in-memory byte array.
 * Used by AgentLogController to pass pre-read bytes into AnalyzerLogUploadIngestionService
 * without touching the file system a second time.
 */
class ByteArrayFilePart(
    private val name: String,
    private val bytes: ByteArray,
) : FilePart {

    override fun filename(): String = name

    override fun transferTo(dest: Path): Mono<Void> =
        Mono.fromRunnable { dest.toFile().writeBytes(bytes) }

    override fun name(): String = name

    override fun headers(): HttpHeaders = HttpHeaders().also { h ->
        h.contentLength = bytes.size.toLong()
    }

    override fun content(): Flux<DataBuffer> {
        val buffer = DefaultDataBufferFactory.sharedInstance.wrap(bytes)
        return Flux.just(buffer)
    }
}
