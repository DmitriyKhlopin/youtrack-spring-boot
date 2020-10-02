package fsight.youtrack.common

import org.springframework.stereotype.Service

@Service
class Resolver : IResolver {
    final val i: HashMap<String, String> = HashMap()

    init {
        i["\\AP\\Products\\Development\\Core Functionality"] = "Core"
        i["\\AP\\Products\\Development\\Internal"] = "Core"
        i["\\AP\\Products\\Development\\Data Engine"] = "Data Engine"
        i["\\AP\\Products\\Development\\InMemory"] = "Data Engine"
        i["\\AP\\Products\\Development\\Visualization"] = "Visualization"
        i["\\AP\\Products\\Development\\DevTools"] = "DevTools"
        i["\\AP\\Products\\Development\\Self Service BI"] = "SSBI"
        i["\\AP\\Products\\Development\\Products\\BPM and BAC\\West"] = "West BPM"
        i["\\AP\\Products\\Development\\Products\\BPM and BAC\\East"] = "East BPM"
        i["\\AP\\Products\\Development\\Reporting\\Phobos"] = "Phobos"
        i["\\AP\\Products\\Development\\Reporting\\Deimos"] = "Deimos"
        i["\\AP\\Products\\Development\\Statistics and Modeling Tools"] = "Modeling"
        i["\\AP\\Products\\Development\\React-сomponents\\Prime"] = "Prime"
        i["\\AP\\Products\\Development\\React-сomponents\\Nexus"] = "Nexus"
        i["\\AP\\Products\\Development\\Products\\Budgeting"] = "Corporate Products"
        i["\\AP\\Products\\Development\\Products\\Corporate Products Tools"] = "Corporate Products"
        i["\\AP\\Products\\Development\\Web administration and navigation tools"] = "Data Storage Constructors"
        i["\\AP\\Products\\Development\\Mobile Technologies"] = "Mobile Platform"
        i["\\AP\\Products\\Automated Testing"] = "Automation"
        i["\\AP\\Products\\Release Engineering"] = "DevOps"
        i["\\AP\\Products\\Technical Support"] = "Support"
        i["\\AP\\Products\\Analytics"] = "Design"
        i["\\AP\\Products\\Design"] = "Design"
        i["\\AP\\Products\\Documentation"] = "Documentation"
        i["\\AP\\Products\\Localization"] = "Documentation"
        i["\\AP\\Products\\raining"] = "Education"



        val j = "\\AP\\Products\\Development\\Core Functionality"
        i.filter { j.contains(it.key) }.forEach { println(it.value) }
    }

    override fun valueTypeToYouTrackProjectFieldType(string: String): String? {
        return when (string) {
            "enum" -> "EnumProjectCustomField"
            else -> null
        }
    }
}
