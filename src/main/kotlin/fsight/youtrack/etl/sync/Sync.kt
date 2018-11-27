package fsight.youtrack.etl.sync

import org.jetbrains.exposed.sql.Database
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class Sync(@Qualifier("pgDataSource") private val db: Database) : ISync {
    override fun getActiveIssuesYT(): List<Any> {
        return listOf()
    }
}