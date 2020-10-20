package fsight.youtrack.common

interface IResolver {
    fun valueTypeToYouTrackProjectFieldType(string: String): String?
    fun resolveAreaToTeam(area: String): String?
    fun resolveAreaToPo(area: String): String?
    fun resolveTeamToPo(team: String): String?
}
