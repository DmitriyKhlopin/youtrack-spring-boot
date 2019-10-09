package fsight.youtrack.api.partners

import fsight.youtrack.generated.jooq.tables.PartnerDetails.PARTNER_DETAILS
import fsight.youtrack.models.Partner
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class Partners(private val dsl: DSLContext) : IPartners {

    override fun getPartners(): Any {
        return dsl.select(
            PARTNER_DETAILS.PARTNER_NAME.`as`("partnerName"),
            PARTNER_DETAILS.PROJECT_ID.`as`("projectId"),
            PARTNER_DETAILS.CUSTOMER_NAME.`as`("customerName"),
            PARTNER_DETAILS.ISSUES_COUNT.`as`("issuesCount"),
            PARTNER_DETAILS.PRODUCT_VERSIONS.`as`("productVersions"),
            PARTNER_DETAILS.DATABASE_VERSIONS.`as`("databaseVersions"),
            PARTNER_DETAILS.PROJ_ETS.`as`("etsProject"),
            PARTNER_DETAILS.ITERATION_PATH.`as`("iteration"),
            PARTNER_DETAILS.DATE_TO.`as`("dateTo"),
            PARTNER_DETAILS.IS_COMMERCIAL.`as`("isCommercial"),
            PARTNER_DETAILS.IS_DESKTOP_APP.`as`("isDesktop"),
            PARTNER_DETAILS.IS_WEB_APP.`as`("isWeb"),
            PARTNER_DETAILS.IS_MOBILE_APP.`as`("isMobile"),
            PARTNER_DETAILS.IS_DEMO.`as`("isDemo"),
            PARTNER_DETAILS.IS_IMPORTANT.`as`("isImportant")
        ).from(PARTNER_DETAILS)
            .fetchInto(Partner::class.java)
    }
}
