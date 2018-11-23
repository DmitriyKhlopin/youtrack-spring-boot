package fsight.youtrack.db.exposed.pg

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class ProjectDictionaryRepo(@Qualifier("pgDataSource") private val db: Database) {
    fun get(): List<ProjectDictionaryModel> {
        return transaction(db) {
            ProjectDictionaryTable.selectAll().map {
                ProjectDictionaryModel(
                    it[ProjectDictionaryTable.projectShortName],
                    it[ProjectDictionaryTable.customer],
                    it[ProjectDictionaryTable.projectEts],
                    it[ProjectDictionaryTable.iterationPath],
                    it[ProjectDictionaryTable.dateFrom].millis,
                    it[ProjectDictionaryTable.dateTo].millis
                )
            }
        }
    }
}

//https://github.com/WifekRaissi/spring-boot-kotlin-exposed-simple/tree/master/src/main/kotlin/com/city/city/services