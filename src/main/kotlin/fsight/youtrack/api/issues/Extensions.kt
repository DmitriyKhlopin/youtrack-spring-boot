package fsight.youtrack.api.issues

fun HighPriorityIssue.getBugs(): List<Int> = issue?.split(",")?.filterNot { it == "null" }?.map { it.trim().toInt() }?.filter { it <= 1000000 }.orEmpty()
fun HighPriorityIssue.getRequirements(): List<Int> = requirement?.split(",")?.filterNot { it == "null" }?.map { it.trim().toInt() }?.filter { it <= 1000000 }.orEmpty()

fun List<HighPriorityIssue>.getBugs(): List<Int> = this.map { it.getBugs() }.flatten().distinct()
fun List<HighPriorityIssue>.getRequirements(): List<Int> = this.map { it.getRequirements() }.flatten().distinct()