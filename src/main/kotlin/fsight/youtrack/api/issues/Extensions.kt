package fsight.youtrack.api.issues

fun IssueWiThDetails.getBugs(): List<Int> =
    issue?.splitToNumbers().orEmpty()

fun IssueWiThDetails.getRequirements(): List<Int> =
    requirement?.splitToNumbers().orEmpty()

fun String?.splitToNumbers() = this
    ?.split(",", " ")
    ?.filterNot { it == "null" || it.contains("HH-") || it.isEmpty() || it.isBlank() }?.map { it.trim().toInt() }
    ?.filter { it <= 1000000 }

fun List<IssueWiThDetails>.getBugs(): List<Int> = this.map { it.getBugs() }.flatten().distinct()
fun List<IssueWiThDetails>.getRequirements(): List<Int> = this.map { it.getRequirements() }.flatten().distinct()
fun List<IssueWiThDetails>.getBugsAndFeatures(): List<Int> {
    val i = this.map { it.getBugs() }.flatten() as ArrayList
    val j = this.map { it.getRequirements() }.flatten()
    i.addAll(j)
    return i.distinct()
}
