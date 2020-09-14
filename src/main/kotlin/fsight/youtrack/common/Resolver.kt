package fsight.youtrack.common

import org.springframework.stereotype.Service

@Service
class Resolver : IResolver {
    override fun valueTypeToYouTrackProjectFieldType(string: String): String? {
        return when (string) {
            "enum" -> "EnumProjectCustomField"
            else -> null
        }
    }
}
