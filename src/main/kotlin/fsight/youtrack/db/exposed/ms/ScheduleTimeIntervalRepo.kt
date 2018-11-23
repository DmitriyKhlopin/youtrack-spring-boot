package fsight.youtrack.db.exposed.ms

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class ScheduleTimeIntervalRepo(@Qualifier("msDataSource") private val ds: Database) {
    fun getTopTen(): List<ScheduleTimeIntervalModel> {
        var res = listOf<ScheduleTimeIntervalModel>()
        transaction(ds) {
            res = ScheduleTimeIntervalTable.selectAll().limit(10).map { item ->
                ScheduleTimeIntervalModel(
                    id = item[ScheduleTimeIntervalTable.id],
                    userSchedule = item[ScheduleTimeIntervalTable.userSchedule],
                    week = item[ScheduleTimeIntervalTable.week],
                    day = item[ScheduleTimeIntervalTable.day],
                    dateBegin = item[ScheduleTimeIntervalTable.dateBegin],
                    dateEnd = item[ScheduleTimeIntervalTable.dateEnd]
                )
            }
        }
        return res
    }
}