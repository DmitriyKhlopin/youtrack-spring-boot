package fsight.youtrack.models.v2

data class Issue(
    //var attachments: ArrayList<Any>? = null,
    var comments: ArrayList<IssueComment>? = null,
    var created: Long? = null,
    var description: String? = null,
    //var draftComment: String? = null,
    //var draftOwner: String? = null,
    //var externalIssue: String? = null,
    var fields: ArrayList<IssueCustomField>? = null,
    var idReadable: String? = null,
    //var isDraft: String? = null,
    //var links: ArrayList<Any>? = null,
    var numberInProject: String? = null,
    var parent: String? = null,
    var project: Project? = null,
    var reporter: User? = null,
    var resolved: Long? = null,
    //var subtasks: String? = null,
    var summary: String? = null,
    //var tags: ArrayList<Any>? = null,
    var updated: Long? = null,
    var updater: User? = null,
    var usesMarkdown: Boolean? = null,
    //var visibility: String? = null,
    //var voters: ArrayList<Any>? = null,
    //var votes: String? = null,
    //var watchers: ArrayList<Any>? = null,
    //var wikifiedDescription: String? = null,
    var id: String? = null,
    var `$type`: String? = null
)

data class User(
    var isLocke: Boolean? = null,
    var name: String? = null,
    var fullName: String? = null,
    var login: String? = null,
    var email: String? = null,
    var avatarUrl: String? = null,
    var online: Boolean? = null,
    var issueRelatedGroup: Any? = null,
    var id: String? = null,
    var `$type`: String? = null
)

data class IssueComment(
    var author: User,
    /*"checkboxes" : "List",
    "deleted" : "Boolean",
    "issue" : "Issue",
    "attachments" : "MutableCollection",
    "created" : "Long",
    "id" : "String",*/
    var text: String? = null
    /*"textPreview" : "String!",
    "updated" : "Long",
    "usesMarkdown" : "Boolean",
    "visibility" : "Visibility"*/
)

data class IssueCustomField(
    var projectCustomField: ProjectCustomField? = null,
    var value: Any? = null,
    val `$type`: String
)

data class ProjectCustomField(
/*        "canBeEmpty" : "Boolean",
"condition" : "CustomFieldCondition",
"emptyFieldText" : "String",*/
    var field: CustomField? = null
/*"hasRunningJob" : "Boolean",
"isPublic" : "Boolean",
"ordinal" : "Int",
"project" : "Project",
"id" : "String"*/
)

data class CustomField(
    var id: String? = null,
    var name: String? = null
)

data class CustomFieldValue(
    var id: String? = null,
    var name: String? = null
)

data class Project(
    var shortName: String? = null,
    var id: String? = null,
    var name: String? = null
)
