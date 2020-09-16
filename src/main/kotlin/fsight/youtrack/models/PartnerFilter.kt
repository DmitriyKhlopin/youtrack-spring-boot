package fsight.youtrack.models

data class PartnerFilter(
        val project: String,
        val ets: String,
        val customer: String
)

val transform: (PartnerFilter) -> String = { x: PartnerFilter -> "(project_short_name = '${x.project}' and proj_ets = '${x.ets}' and customer = '${x.customer}')" }
/*val transform: (PartnerFilter) -> String = PartnerFilterTransformer()*/

class PartnerFilterTransformer : (PartnerFilter) -> String {
        override operator fun invoke(x: PartnerFilter): String = "${x.project} ${x.ets} ${x.customer}"
}

fun List<PartnerFilter>.toCondition() = this.joinToString(separator = " or ") { transform.invoke(it) }
