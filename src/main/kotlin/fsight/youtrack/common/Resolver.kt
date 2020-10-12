package fsight.youtrack.common

import org.springframework.stereotype.Service

@Service
class Resolver : IResolver {
    final val areas: HashMap<String, String> = HashMap()

    init {
        areas["\\AP\\Products\\Development\\Core Functionality"] = "Core"
        areas["\\AP\\Products\\Development\\Internal"] = "Core"
        areas["\\AP\\Products\\Development\\Data Engine"] = "Data Engine"
        areas["\\AP\\Products\\Development\\ETL Toolkit"] = "Core"
        areas["\\AP\\Products\\Development\\InMemory"] = "Data Engine"
        areas["\\AP\\Products\\Development\\Visualization"] = "Visualization"
        areas["\\AP\\Products\\Development\\Components\\Visualization"] = "Visualization"
        areas["\\AP\\Products\\Development\\DevTools"] = "DevTools"
        areas["\\AP\\Products\\Development\\Components\\Development Tools"] = "DevTools"
        areas["\\AP\\Products\\Development\\Self Service BI"] = "SSBI"
        areas["\\AP\\Products\\Development\\Products\\BPM and BAC\\West"] = "West BPM"
        areas["\\AP\\Products\\Development\\Products\\BPM and BAC\\East"] = "East BPM"
        areas["\\AP\\Products\\Development\\Products\\BPM and BAC"] = "BPM"
        areas["\\AP\\Products\\Development\\Reporting\\Phobos"] = "Phobos"
        areas["\\AP\\Products\\Development\\Reporting\\Deimos"] = "Deimos"
        areas["\\AP\\Products\\Development\\Reporting"] = "Reporting"
        areas["\\AP\\Products\\Development\\Statistics and Modeling Tools"] = "Modeling"
        areas["\\AP\\Products\\Development\\React-сomponents\\Prime"] = "Prime"
        areas["\\AP\\Products\\Development\\React-сomponents\\Nexus"] = "Nexus"
        areas["\\AP\\Products\\Development\\Products\\Budgeting"] = "Corporate Products"
        areas["\\AP\\Products\\Development\\Products\\Modeling Interface"] = "Modeling"
        areas["\\AP\\Products\\Development\\Products\\Corporate Products Tools"] = "Corporate Products"
        areas["\\AP\\Products\\Development\\Products\\BPM and BAC"] = "Corporate Products"
        areas["\\AP\\Products\\Development\\Web administration and navigation tools"] = "Data Storage Constructors"
        areas["\\AP\\Products\\Development\\Mobile Technologies"] = "Mobile Platform"
        areas["\\AP\\Products\\Automated Testing"] = "Automation"
        areas["\\AP\\Products\\Release Engineering"] = "DevOps"
        areas["\\AP\\Products\\Technical Support"] = "Support"
        areas["\\AP\\Products\\Analytics"] = "Design"
        areas["\\AP\\Products\\Design"] = "Design"
        areas["\\AP\\Products\\Documentation"] = "Documentation"
        areas["\\AP\\Products\\Localization"] = "Documentation"
        areas["\\AP\\Products\\raining"] = "Education"
    }
    override fun valueTypeToYouTrackProjectFieldType(string: String): String? {
        return when (string) {
            "enum" -> "EnumProjectCustomField"
            else -> null
        }
    }

    override fun resolveAreaToTeam(area: String): String? {
        return areas.filter { area.contains(it.key) }.map { it.value }.firstOrNull()
    }
}
