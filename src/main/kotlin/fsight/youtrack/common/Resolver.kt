package fsight.youtrack.common

import fsight.youtrack.db.IPGProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class Resolver : IResolver {
    @Autowired
    private lateinit var pg: IPGProvider

    final val areas: HashMap<String, String> = HashMap()
    final val productOwners: HashMap<String, String> = HashMap()

    @PostConstruct
    fun loadAreasWithTeams() {
        pg.getAreasWithTeams().forEach { areas[it.area] = it.team }
        println("${areas.size} areas cached")
    }

    @PostConstruct
    fun loadProductOwners(){
        pg.getProductOwners().forEach { productOwners[it.team] = it.owner }
        println("${productOwners.size} product owners cached")
    }

    override fun valueTypeToYouTrackProjectFieldType(string: String): String? {
        return when (string) {
            "enum" -> "EnumProjectCustomField"
            else -> null
        }
    }

    override fun resolveAreaToTeam(area: String): String? {
        return areas[area]
    }

    override fun resolveTeamToPo(team: String): String? {
        return productOwners[team]
    }

    override fun resolveAreaToPo(area: String): String? {
        val i = resolveAreaToTeam(area) ?: return null
        return resolveTeamToPo(i)
    }

}
