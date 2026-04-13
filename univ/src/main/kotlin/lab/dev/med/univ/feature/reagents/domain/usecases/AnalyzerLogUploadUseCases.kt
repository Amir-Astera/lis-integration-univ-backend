package lab.dev.med.univ.feature.reagents.domain.usecases

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogUpload
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerLogUploadIngestionService
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerLogUploadQueryService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

data class BatchAnalyzerLogResult(
    val uploadId: String,
    val originalFileName: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val upload: AnalyzerLogUpload? = null,
)

interface UploadAnalyzerLogUseCase {
    suspend operator fun invoke(
        sourceType: AnalyzerLogSourceType,
        analyzerId: String?,
        part: FilePart,
        uploadedBy: String? = null,
        autoParse: Boolean = true,
    ): AnalyzerLogUpload
}

interface ParseAnalyzerLogUploadUseCase {
    suspend operator fun invoke(uploadId: String): AnalyzerLogUpload
}

interface GetAnalyzerLogUploadsUseCase {
    suspend operator fun invoke(analyzerId: String? = null): List<AnalyzerLogUpload>
}

interface GetParsedAnalyzerSamplesUseCase {
    suspend operator fun invoke(uploadId: String): List<ParsedAnalyzerSample>
}

@Service
internal class UploadAnalyzerLogUseCaseImpl(
    private val ingestionService: AnalyzerLogUploadIngestionService,
) : UploadAnalyzerLogUseCase {

    override suspend fun invoke(
        sourceType: AnalyzerLogSourceType,
        analyzerId: String?,
        part: FilePart,
        uploadedBy: String?,
        autoParse: Boolean,
    ): AnalyzerLogUpload {
        val upload = ingestionService.upload(sourceType, analyzerId, part, uploadedBy)
        return if (autoParse && sourceType == AnalyzerLogSourceType.APPLOGS) {
            ingestionService.parseAndPersist(upload.id)
        } else {
            upload
        }
    }
}

@Service
internal class ParseAnalyzerLogUploadUseCaseImpl(
    private val ingestionService: AnalyzerLogUploadIngestionService,
) : ParseAnalyzerLogUploadUseCase {
    override suspend fun invoke(uploadId: String): AnalyzerLogUpload = ingestionService.parseAndPersist(uploadId)
}

@Service
internal class GetAnalyzerLogUploadsUseCaseImpl(
    private val queryService: AnalyzerLogUploadQueryService,
) : GetAnalyzerLogUploadsUseCase {
    override suspend fun invoke(analyzerId: String?): List<AnalyzerLogUpload> = queryService.getUploads(analyzerId)
}

@Service
internal class GetParsedAnalyzerSamplesUseCaseImpl(
    private val queryService: AnalyzerLogUploadQueryService,
) : GetParsedAnalyzerSamplesUseCase {
    override suspend fun invoke(uploadId: String): List<ParsedAnalyzerSample> = queryService.getParsedSamples(uploadId)
}

interface BatchUploadAnalyzerLogsUseCase {
    suspend operator fun invoke(
        sourceType: AnalyzerLogSourceType,
        analyzerId: String?,
        parts: List<FilePart>,
        uploadedBy: String? = null,
        autoParse: Boolean = true,
    ): List<BatchAnalyzerLogResult>
}

@Service
internal class BatchUploadAnalyzerLogsUseCaseImpl(
    private val ingestionService: AnalyzerLogUploadIngestionService,
) : BatchUploadAnalyzerLogsUseCase {

    override suspend fun invoke(
        sourceType: AnalyzerLogSourceType,
        analyzerId: String?,
        parts: List<FilePart>,
        uploadedBy: String?,
        autoParse: Boolean,
    ): List<BatchAnalyzerLogResult> {
        val results = mutableListOf<BatchAnalyzerLogResult>()
        for (part in parts) {
            val fileName = part.filename()
            val result = runCatching {
                val upload = ingestionService.upload(sourceType, analyzerId, part, uploadedBy)
                val finalUpload = if (autoParse && sourceType == AnalyzerLogSourceType.APPLOGS) {
                    ingestionService.parseAndPersist(upload.id)
                } else {
                    upload
                }
                BatchAnalyzerLogResult(
                    uploadId = finalUpload.id,
                    originalFileName = fileName,
                    success = true,
                    upload = finalUpload,
                )
            }.getOrElse { ex ->
                BatchAnalyzerLogResult(
                    uploadId = "",
                    originalFileName = fileName,
                    success = false,
                    errorMessage = ex.message,
                )
            }
            results += result
        }
        return results
    }
}
