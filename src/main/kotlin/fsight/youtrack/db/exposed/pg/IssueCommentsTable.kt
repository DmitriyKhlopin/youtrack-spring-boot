package fsight.youtrack.db.exposed.pg

import org.jetbrains.exposed.sql.Table

object IssueCommentsTable : Table(name = "issue_comments") {
    val id = varchar(name = "id ", length = 256)
    val issueId = varchar(name = "issue_id ", length = 256)
    val parentId = varchar(name = "parent_id ", length = 256)
    val deleted = bool("deleted ")
    val shownForIssueAuthor = bool(name = "shown_for_issue_author ")
    val author = varchar(name = "author ", length = 256)
    val authorFullName = varchar(name = "author_full_name ", length = 256)
    val commentText = text(name = "comment_text ")
    val created = datetime(name = "created")
    val updated = datetime(name = "updated ")
    val permittedGroup = varchar(name = "permitted_group ", length = 256)
    val replies = varchar(name = "replies ", length = 256)
}