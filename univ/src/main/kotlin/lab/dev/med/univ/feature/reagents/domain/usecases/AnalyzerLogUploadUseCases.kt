package lab.dev.med.univ.feature.reagents.domain.usecases

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogUpload
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerLogUploadIngestionService
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerLogUploadQueryService
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service

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
