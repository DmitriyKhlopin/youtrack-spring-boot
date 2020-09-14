package fsight.youtrack.common

interface IResolver {
    fun valueTypeToYouTrackProjectFieldType(string: String): String?
}
