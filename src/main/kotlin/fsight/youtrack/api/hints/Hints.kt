package fsight.youtrack.api.hints

import fsight.youtrack.generated.jooq.tables.CustomerRepositories.CUSTOMER_REPOSITORIES
import org.jooq.DSLContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Hints(private val dsl: DSLContext) : IHints {

    data class CustomerRepository(
        var projectId: String? = null,
        var customer: String? = null,
        var url: String? = null,
        var schema: String? = null,
        var dbType: String? = null,
        var user: String? = null,
        var password: String? = null
    )

    override fun getNewIssueHints(project: String?, customer: String?): List<Hints.CustomerRepository> {
        return dsl.select(
            CUSTOMER_REPOSITORIES.PROJECT_ID.`as`("projectId"),
            CUSTOMER_REPOSITORIES.CUSTOMER,
            CUSTOMER_REPOSITORIES.URL,
            CUSTOMER_REPOSITORIES.SCHEMA,
            CUSTOMER_REPOSITORIES.DB_TYPE.`as`("dbType"),
            CUSTOMER_REPOSITORIES.USER,
            CUSTOMER_REPOSITORIES.PASSWORD
        )
            .from(CUSTOMER_REPOSITORIES)
            .where(CUSTOMER_REPOSITORIES.CUSTOMER.eq(customer))
            .fetchInto(CustomerRepository::class.java)
    }
}