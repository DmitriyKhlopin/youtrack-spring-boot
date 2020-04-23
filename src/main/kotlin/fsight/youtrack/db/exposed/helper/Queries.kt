package fsight.youtrack.db.exposed.helper

class Queries {
    fun getBugsByIdsQuery(bugIds: List<Int>) = """select System_Id, System_State, IterationPath from CurrentWorkItemView where System_Id in (${bugIds.joinToString(",")}) and TeamProjectCollectionSK = 37 order by System_Id asc"""
}