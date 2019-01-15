package fsight.youtrack.etl


import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import fsight.youtrack.AUTH
import fsight.youtrack.Converter
import fsight.youtrack.api.YouTrackAPIv2
import fsight.youtrack.models.Comment
import fsight.youtrack.models.sql.IssueHistoryItem
import org.springframework.web.bind.annotation.*
import java.sql.Timestamp

@CrossOrigin
@RestController
class ETLController(private val service: IETL) {
    @GetMapping("/api/etl")
    fun loadData(
        @RequestParam("dateFrom", required = false) dateFrom: String? = "",
        @RequestParam("dateTo", required = false) dateTo: String? = "",
        @RequestParam("parameters", required = false) parameters: String = ""
    ) =
        if (dateFrom != null && dateTo != null)
            service.loadDataFromYT(
                manual = true,
                customFilter = "updated: $dateFrom .. $dateTo",
                parameters = parameters
            ) else
            service.loadDataFromYT(manual = true, parameters = parameters)

    @GetMapping("/api/etl/state")
    fun getCurrentState() = ETL.etlState

    @GetMapping("/api/etl/bundle")
    fun getBundles() = service.getBundles()

    @GetMapping("/api/etl/users")
    fun getUsers() = service.getUsers()

    @GetMapping("/api/etl/issues/{id}")
    fun getIssueById(@PathVariable("id") id: String) = service.getIssueById(id)

    @GetMapping("/api/etl/history/{id}")
    fun getHistory(@PathVariable("id") id: String) {
        val accepted = listOf("jetbrains.youtrack.event.gaprest.impl.ActivityItemImpl")
        println(id)
        val categories = listOf(
            "categories=CommentsCategory",
            "categories=WorkItemCategory",
            "categories=AttachmentsCategory",
            "categories=AttachmentRenameCategory",
            "categories=CustomFieldCategory",
            "categories=DescriptionCategory",
            "categories=IssueCreatedCategory",
            "categories=IssueResolvedCategory",
            "categories=LinksCategory",
            "categories=ProjectCategory",
            "categories=PermittedGroupCategory",
            "categories=SprintCategory",
            "categories=SummaryCategory",
            "categories=TagsCategory"
        ).joinToString("&")
        val fields = listOf(
            "activities(\$type,added(\$type,\$type,\$type,agile(id),attachments(\$type,author(fullName,id,ringId),comment(id),created,id,imageDimension(height,width),issue(id,project(id,ringId)),mimeType,name,removed,size,thumbnailURL,url,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),branch,color(id),commands(end,errorText,hasError,start),comment(id),created,created,creator(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),date,date,deleted,duration(id,minutes,presentation),fetched,files,id,id,id,id,id,id,id,idReadable,isDraft,localizedName,mimeType,minutes,name,name,noHubUserReason(id),noUserReason(id),numberInProject,presentation,processor(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),processors(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),removed,resolved,shortName,size,state,summary,text,text,text,text,textPreview,textPreview,thumbnailURL,type(id,name),url,urls,user(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),userName,usesMarkdown,usesMarkdown,version,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),authorGroup(icon,name),category(id),field(\$type,customField(id,fieldType(isMultiValue,valueType)),id,linkId,presentation),id,markup,removed(\$type,\$type,agile(id),color(id),id,id,idReadable,isDraft,localizedName,name,numberInProject,project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),resolved,summary,text),target(created,id,usesMarkdown),targetMember,targetSubMember,timestamp)",
            ",cursor"
        ).joinToString(",")
        val issueActivities = YouTrackAPIv2
            .create(Converter.GSON)
            .getHistory(auth = AUTH, issueId = id/* categories = "$categories&",*/ /*top = -1, fields = fields*/)
            .execute()
        val activities = issueActivities.body()?.get("activities") as JsonArray
        val canBeUnwrapped =
            activities.filter { obj: JsonElement -> obj.asJsonObject?.get("\$type")?.asString in accepted }
        val comments =
            activities.filter { obj: JsonElement -> obj.asJsonObject?.get("\$type")?.asString == "jetbrains.youtrack.event.gaprest.impl.CommentActivityItem" }


        val r = canBeUnwrapped.map { obj: JsonElement ->
            println()
            println(obj)
            val type: String? = obj.asJsonObject?.get("\$type")?.asString
            val target: JsonObject? = obj.asJsonObject?.get("target")?.asJsonObject
            val idReadable = target?.get("idReadable").toString()
            val updateDateTime = obj.asJsonObject?.get("timestamp")?.asLong
            val author: JsonObject? = obj.asJsonObject?.get("author")?.asJsonObject
            val login = author?.get("login").toString()
            val field: JsonObject? = obj.asJsonObject?.get("field")?.asJsonObject
            val fieldName = field?.get("presentation")?.asString
            val customField: JsonObject? = field?.get("customField")?.asJsonObject
            val fieldType: JsonObject? = customField?.get("fieldType")?.asJsonObject
            val valueType: String? = fieldType?.get("valueType")?.asString
            val added: String? = when (valueType) {
                "string", "date and time" ->
                    obj.asJsonObject?.get("added")?.asString
                "enum", "version", "state" ->
                    obj.asJsonObject?.get("added")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("name")?.asString
                else ->
                    "unwrapped ${obj.asJsonObject?.get("added")?.toString()}"
            }
            val removed: String? = when (valueType) {
                "string", "date and time" -> {
                    val t = obj.asJsonObject?.get("removed")
                    if (t is JsonNull) null else obj.asJsonObject?.get("removed")?.asString
                }
                "enum", "version", "state" ->
                    obj.asJsonObject?.get("removed")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("name")?.asString
                else ->
                    "unwrapped ${obj.asJsonObject?.get("removed")?.toString()}"
            }
            println(valueType)
            println("$type - $added - $removed")

            val result = IssueHistoryItem(
                issueId = idReadable,
                author = login,
                updateDateTime = Timestamp(updateDateTime ?: 0),
                fieldName = fieldName,
                value = null,
                oldValue = removed,
                newValue = added,
                fieldType = valueType
            )
            println(result)
            result
        }

        /*comments.forEach { obj: JsonElement ->
            println()
            println("CANT UNWRAP")
            println(obj)


            val commentId: String = obj.asJsonObject?.get("id")?.asString ?: ""
            val target: JsonObject? = obj.asJsonObject?.get("target")?.asJsonObject
            val idReadable = target?.get("idReadable").toString()
            val updateDateTime = obj.asJsonObject?.get("timestamp")?.asLong ?: 0
            val author: JsonObject? = obj.asJsonObject?.get("author")?.asJsonObject
            val login = author?.get("login").toString()
            val fullName = author?.get("fullName").toString()
            val field: JsonObject? = obj.asJsonObject?.get("field")?.asJsonObject
            val fieldName = field?.get("presentation")?.asString
            val customField: JsonObject? = field?.get("customField")?.asJsonObject
            val fieldType: JsonObject? = customField?.get("fieldType")?.asJsonObject
            val valueType: String? = fieldType?.get("valueType")?.asString
            val added: String =
                obj.asJsonObject?.get("added")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("text")?.asString ?: ""

            val deleted: Boolean =
                obj.asJsonObject?.get("added")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("deleted")?.asBoolean
                    ?: false
            val comment = Comment(
                id = commentId,
                issueId = id,
                parentId = null,
                deleted = deleted,
                shownForIssueAuthor = true,
                author = login,
                authorFullName = fullName,
                text = added,
                created = updateDateTime,
                updated = updateDateTime,
                permittedGroup = "",
                replies = listOf()
            )
            println(comment)
        }*/
    }
}
