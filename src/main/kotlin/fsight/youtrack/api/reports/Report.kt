package fsight.youtrack.api.reports

import fsight.youtrack.api.issues.IssueFilter
import fsight.youtrack.db.IPGProvider
import fsight.youtrack.generated.jooq.tables.PartnerIssueStates.PARTNER_ISSUE_STATES
import fsight.youtrack.models.PartnerFilter
import fsight.youtrack.models.PartnerIndicator
import fsight.youtrack.models.VelocityAggregated
import fsight.youtrack.models.toCondition
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Report(private val dsl: DSLContext) : IReport {
    @Autowired
    private lateinit var pg: IPGProvider

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

    override fun getVelocity(issueFilter: IssueFilter): Any {
        return pg.getVelocity(issueFilter).groupingBy { it.week }.aggregate { _, accumulator: VelocityAggregated?, element, first ->
            if (first) {
                VelocityAggregated(
                    element.week,
                    if (element.type == "Все типы") element.result else 0,
                    if (element.type == "Bug") element.result else 0,
                    if (element.type == "Feature") element.result else 0,
                    if (element.type == "Консультация") element.result else 0
                )
            } else {
                when (element.type) {
                    "Bug" -> accumulator?.bugs = element.result ?: 0
                    "Feature" -> accumulator?.features = element.result ?: 0
                    "Консультация" -> accumulator?.consultations = element.result ?: 0
                    "Все типы" -> accumulator?.all = element.result ?: 0
                }
                accumulator
            }
        }.map { it.value }.takeLast(issueFilter.limit)
    }
}




