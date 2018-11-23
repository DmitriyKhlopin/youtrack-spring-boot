package fsight.youtrack.db.exposed.ms.schedule.fact

import fsight.youtrack.toWorkHoursModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class WorkHoursRepo(@Qualifier("msDataSource") private val db: Database) {
    fun getWorkHours(): List<WorkHoursModel> {
        return transaction(db) { WorkHoursTable.selectAll().limit(100).map(ResultRow::toWorkHoursModel) }
    }
}