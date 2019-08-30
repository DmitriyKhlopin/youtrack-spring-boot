package fsight.youtrack.api.common

import fsight.youtrack.generated.jooq.tables.Issues.ISSUES
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class Common(private val dslContext: DSLContext) : ICommon {
    override fun findIssueInDB(id: String?): Boolean {
        return dslContext.selectCount().from(ISSUES).where(ISSUES.ID.eq(id)).fetchOne().component1() == 1
    }
}
