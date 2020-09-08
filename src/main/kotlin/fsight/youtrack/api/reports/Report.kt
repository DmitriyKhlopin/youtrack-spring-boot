package fsight.youtrack.api.reports

import fsight.youtrack.generated.jooq.tables.DynamicsProcessedByDay.DYNAMICS_PROCESSED_BY_DAY
import fsight.youtrack.generated.jooq.tables.PartnerIssueStates.PARTNER_ISSUE_STATES
import fsight.youtrack.models.PartnerFilter
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class Report(private val dsl: DSLContext) : IReport {
    override fun getStatesByPartner(filters: List<PartnerFilter>): Any {
        return dsl.select(
            PARTNER_ISSUE_STATES.PROJECT_SHORT_NAME.`as`("partner"),
            PARTNER_ISSUE_STATES.ID.`as`("issueId"),
            PARTNER_ISSUE_STATES.CREATED_DATE.`as`("createdDate"),
            PARTNER_ISSUE_STATES.RESOLVED_DATE.`as`("resolvedDate"),
            PARTNER_ISSUE_STATES.STATE.`as`("state"),
            PARTNER_ISSUE_STATES.CUSTOMER.`as`("customer"),
            PARTNER_ISSUE_STATES.PROJ_ETS.`as`("ets"),
            PARTNER_ISSUE_STATES.IS_PUBLIC.`as`("public"),
            PARTNER_ISSUE_STATES.TYPE.`as`("type"),
            PARTNER_ISSUE_STATES.PRIORITY.`as`("priority"),
            PARTNER_ISSUE_STATES.PRODUCT.`as`("product")
        )
            .from(PARTNER_ISSUE_STATES)
            .where(filters.toCondition())
            .fetchInto(PartnerIndicator::class.java)

    }

    override fun getTypesByPartner(filters: List<PartnerFilter>): Any {
        return dsl.select(
            PARTNER_ISSUE_STATES.PROJECT_SHORT_NAME.`as`("partner"),
            PARTNER_ISSUE_STATES.ID.`as`("issueId"),
            PARTNER_ISSUE_STATES.CREATED_DATE.`as`("createdDate"),
            PARTNER_ISSUE_STATES.RESOLVED_DATE.`as`("resolvedDate"),
            PARTNER_ISSUE_STATES.STATE.`as`("state"),
            PARTNER_ISSUE_STATES.CUSTOMER.`as`("customer"),
            PARTNER_ISSUE_STATES.PROJ_ETS.`as`("ets"),
            PARTNER_ISSUE_STATES.IS_PUBLIC.`as`("public"),
            PARTNER_ISSUE_STATES.TYPE.`as`("type"),
            PARTNER_ISSUE_STATES.PRIORITY.`as`("priority"),
            PARTNER_ISSUE_STATES.PRODUCT.`as`("product")
        )
            .from(PARTNER_ISSUE_STATES)
            .where(filters.toCondition())
            .fetchInto(PartnerIndicator::class.java)
    }

    override fun getPrioritiesByPartner(filters: List<PartnerFilter>): Any {
        return dsl.select(
            PARTNER_ISSUE_STATES.PROJECT_SHORT_NAME.`as`("partner"),
            PARTNER_ISSUE_STATES.ID.`as`("issueId"),
            PARTNER_ISSUE_STATES.CREATED_DATE.`as`("createdDate"),
            PARTNER_ISSUE_STATES.RESOLVED_DATE.`as`("resolvedDate"),
            PARTNER_ISSUE_STATES.STATE.`as`("state"),
            PARTNER_ISSUE_STATES.CUSTOMER.`as`("customer"),
            PARTNER_ISSUE_STATES.PROJ_ETS.`as`("ets"),
            PARTNER_ISSUE_STATES.IS_PUBLIC.`as`("public"),
            PARTNER_ISSUE_STATES.TYPE.`as`("type"),
            PARTNER_ISSUE_STATES.PRIORITY.`as`("priority"),
            PARTNER_ISSUE_STATES.PRODUCT.`as`("product")
        )
            .from(PARTNER_ISSUE_STATES)
            .where(filters.toCondition())
            .fetchInto(PartnerIndicator::class.java)
    }

    override fun getCustomersByPartner(filters: List<PartnerFilter>): Any {
        return dsl.select(
            PARTNER_ISSUE_STATES.PROJECT_SHORT_NAME.`as`("partner"),
            PARTNER_ISSUE_STATES.ID.`as`("issueId"),
            PARTNER_ISSUE_STATES.CREATED_DATE.`as`("createdDate"),
            PARTNER_ISSUE_STATES.RESOLVED_DATE.`as`("resolvedDate"),
            PARTNER_ISSUE_STATES.STATE.`as`("state"),
            PARTNER_ISSUE_STATES.CUSTOMER.`as`("customer"),
            PARTNER_ISSUE_STATES.PROJ_ETS.`as`("ets"),
            PARTNER_ISSUE_STATES.IS_PUBLIC.`as`("public"),
            PARTNER_ISSUE_STATES.TYPE.`as`("type"),
            PARTNER_ISSUE_STATES.PRIORITY.`as`("priority"),
            PARTNER_ISSUE_STATES.PRODUCT.`as`("product")
        )
            .from(PARTNER_ISSUE_STATES)
            .where(filters.toCondition())
            .fetchInto(PartnerIndicator::class.java)
    }

    override fun getProductsByPartner(filters: List<PartnerFilter>): Any {
        return dsl.select(
            PARTNER_ISSUE_STATES.PROJECT_SHORT_NAME.`as`("partner"),
            PARTNER_ISSUE_STATES.ID.`as`("issueId"),
            PARTNER_ISSUE_STATES.CREATED_DATE.`as`("createdDate"),
            PARTNER_ISSUE_STATES.RESOLVED_DATE.`as`("resolvedDate"),
            PARTNER_ISSUE_STATES.STATE.`as`("state"),
            PARTNER_ISSUE_STATES.CUSTOMER.`as`("customer"),
            PARTNER_ISSUE_STATES.PROJ_ETS.`as`("ets"),
            PARTNER_ISSUE_STATES.IS_PUBLIC.`as`("public"),
            PARTNER_ISSUE_STATES.TYPE.`as`("type"),
            PARTNER_ISSUE_STATES.PRIORITY.`as`("priority"),
            PARTNER_ISSUE_STATES.PRODUCT.`as`("product")
        )
            .from(PARTNER_ISSUE_STATES)
            .where(filters.toCondition())
            .fetchInto(PartnerIndicator::class.java)
    }
}


fun List<PartnerFilter>.toCondition() = this.joinToString(separator = " or ") { transform.invoke(it) }

data class PartnerIndicator(
    var partner: String?,
    var issueId: String?,
    var createdDate: Timestamp?,
    var resolvedDate: Timestamp?,
    var state: String?,
    var customer: String?,
    var ets: String?,
    var public: Boolean?,
    var type: String?,
    var priority: String?,
    var product: String?
)

val transform: (PartnerFilter) -> String = { x: PartnerFilter -> "(project_short_name = '${x.project}' and proj_ets = '${x.ets}' and customer = '${x.customer}')" }
/*val transform: (PartnerFilter) -> String = PartnerFilterTransformer()*/

class PartnerFilterTransformer : (PartnerFilter) -> String {
    override operator fun invoke(x: PartnerFilter): String = "${x.project} ${x.ets} ${x.customer}"
}
