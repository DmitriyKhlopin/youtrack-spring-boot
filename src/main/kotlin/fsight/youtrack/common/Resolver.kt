package fsight.youtrack.common

import fsight.youtrack.api.dictionaries.IDictionary
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Resolver : IResolver {
    @Autowired
    private lateinit var dictionary: IDictionary

    override fun valueTypeToYouTrackProjectFieldType(string: String): String? {
        return when (string) {
            "enum" -> "EnumProjectCustomField"
            else -> null
        }
    }

    override fun resolveAreaToTeam(area: String): String? {
        return dictionary.areas[area]
    }

    override fun resolveTeamToPo(team: String): String? {
        return dictionary.productOwners[team]
    }

    override fun resolveAreaToPo(area: String): String? {
        val i = resolveAreaToTeam(area) ?: return null
        return resolveTeamToPo(i)
    }
}
