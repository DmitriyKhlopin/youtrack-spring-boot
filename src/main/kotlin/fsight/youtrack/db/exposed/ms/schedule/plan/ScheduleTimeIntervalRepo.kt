package fsight.youtrack.db.exposed.ms.schedule.plan

import fsight.youtrack.toScheduleTimeIntervalModel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class ScheduleTimeIntervalRepo(@Qualifier("msDataSource") private val ds: Database) {
    fun getTopTen(): List<ScheduleTimeIntervalModel> {
        return transaction(ds) {
            ScheduleTimeIntervalTable.selectAll().limit(10).map(ResultRow::toScheduleTimeIntervalModel)
        }
    }
}