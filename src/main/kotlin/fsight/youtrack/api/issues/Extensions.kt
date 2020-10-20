package fsight.youtrack.api.issues

fun HighPriorityIssue.getBugs(): List<Int> =
    issue?.splitToNumbers().orEmpty()

fun HighPriorityIssue.getRequirements(): List<Int> =
    requirement?.splitToNumbers().orEmpty()

fun String?.splitToNumbers() = this
    ?.split(",", " ")
    ?.filterNot { it == "null" || it.contains("HH-") || it.isEmpty() || it.isBlank() }?.map { it.trim().toInt() }
    ?.filter { it <= 1000000 }

fun List<HighPriorityIssue>.getBugs(): List<Int> = this.map { it.getBugs() }.flatten().distinct()
fun List<HighPriorityIssue>.getRequirements(): List<Int> = this.map { it.getRequirements() }.flatten().distinct()
