package fsight.youtrack.models.youtrack


data class Project(
        var startingNumber: Long?,
        var shortName: String?,
        var description: String?,
        var leader: User?,
        var createdBy: User?,
        var issues: Array<Issue>?,
        var customFields: Array<ProjectCustomField>?,
        var archived: Boolean?,
        var fromEmail: String?,
        var replyToEmail: String?,
        var template: Boolean?,
        var iconUrl: String?,
        var name: String?,
        var idReadable: String?

)