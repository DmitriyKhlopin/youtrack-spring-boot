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
            var dbName: String? = null,
            var schema: String? = null,
            var dbType: String? = null,
            var userName: String? = null,
            var password: String? = null
    )

    override fun getAllRepositories(): List<CustomerRepository> {
        return dsl.select(
                CUSTOMER_REPOSITORIES.PROJECT_ID.`as`("projectId"),
                CUSTOMER_REPOSITORIES.CUSTOMER,
                CUSTOMER_REPOSITORIES.URL,
                CUSTOMER_REPOSITORIES.SCHEMA,
                CUSTOMER_REPOSITORIES.DB_TYPE.`as`("dbType"),
                CUSTOMER_REPOSITORIES.DB_NAME.`as`("dbName"),
                CUSTOMER_REPOSITORIES.USER_NAME.`as`("userName"),
                CUSTOMER_REPOSITORIES.PASSWORD
        )
                .from(CUSTOMER_REPOSITORIES)
                .fetchInto(CustomerRepository::class.java)
    }

    override fun getRepositoriesByCustomer(project: String?, customer: String?): List<Hints.CustomerRepository> {
        return dsl.select(
                CUSTOMER_REPOSITORIES.PROJECT_ID.`as`("projectId"),
                CUSTOMER_REPOSITORIES.CUSTOMER,
                CUSTOMER_REPOSITORIES.URL,
                CUSTOMER_REPOSITORIES.SCHEMA,
                CUSTOMER_REPOSITORIES.DB_TYPE.`as`("dbType"),
                CUSTOMER_REPOSITORIES.DB_NAME.`as`("dbName"),
                CUSTOMER_REPOSITORIES.USER_NAME.`as`("userName"),
                CUSTOMER_REPOSITORIES.PASSWORD
        )
                .from(CUSTOMER_REPOSITORIES)
                .where(CUSTOMER_REPOSITORIES.CUSTOMER.eq(customer))
                .and(CUSTOMER_REPOSITORIES.PROJECT_ID.eq(project))
                .fetchInto(CustomerRepository::class.java)
    }

    override fun postRepository(repository: CustomerRepository): CustomerRepository {
        dsl.insertInto(CUSTOMER_REPOSITORIES)
                .set(CUSTOMER_REPOSITORIES.PROJECT_ID, repository.projectId)
                .set(CUSTOMER_REPOSITORIES.CUSTOMER, repository.customer)
                .set(CUSTOMER_REPOSITORIES.URL, repository.url)
                .set(CUSTOMER_REPOSITORIES.SCHEMA, repository.schema)
                .set(CUSTOMER_REPOSITORIES.DB_TYPE, repository.dbType)
                .set(CUSTOMER_REPOSITORIES.DB_NAME, repository.dbName)
                .set(CUSTOMER_REPOSITORIES.USER_NAME, repository.userName)
                .set(CUSTOMER_REPOSITORIES.PASSWORD, repository.password)
                .returning(CUSTOMER_REPOSITORIES.URL)
                .fetch()
        return repository
    }
}