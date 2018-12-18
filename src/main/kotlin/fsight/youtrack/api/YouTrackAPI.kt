package fsight.youtrack.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fsight.youtrack.Converter
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.ROOT_REF
import fsight.youtrack.etl.bundles.CustomField
import fsight.youtrack.models.*
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface YouTrackAPI {
    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("issues?fields=idReadable,id")
    fun createIssue(
        @Header("Authorization") auth: String,
        @Body model: String
    ): Call<String>

    @Headers("Accept: application/json", "Content-Type: application/json;charset=UTF-8")
    @POST("commands")
    fun postCommand(
        @Header("Authorization") auth: String,
        @Body model: String
    ): Call<String>

    @Headers("Accept: application/json")
    @GET("admin/customfield/bundle/{bundleName}")
    fun getBundleValues(
        @Header("Authorization") auth: String,
        @Path("bundleName") bundleName: String
    ): Call<EnumBundle>

    @Headers("Accept: application/json")
    @GET("admin/customFieldSettings/customFields?\$top=-1&fields=name,id,aliases,fieldType(id),instances(id,project(shortName,id),bundle(name,values(id,name),id))")
    fun getBundleValues(
        @Header("Authorization") auth: String
    ): Call<List<CustomField>>

    @Headers("Accept: application/json")
    @GET("issue")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("with") with: ArrayList<String>?,
        @Query("after") skip: Int,
        @Query("max") max: Int
    ): Call<Issues>

    @Headers("Accept: application/json")
    @GET("issue")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("filter") filter: String?,
        @Query("with") with: ArrayList<String>?,
        @Query("after") skip: Int,
        @Query("max") max: Int
    ): Call<Issues>

    @Headers("Accept: application/json")
    @GET("issue/{issueId}/changes")
    fun getIssueHistory(
        @Header("Authorization") auth: String,
        @Path("issueId") issueId: String
    ): Call<HistoricalChanges>


    @Headers("Accept: application/json")
    @GET(
        "issues/{id}/activitiesPage?\$top=100&categories=CommentsCategory&categories=WorkItemCategory&categories=AttachmentsCategory&categories=AttachmentRenameCategory&categories=CustomFieldCategory&categories=DescriptionCategory&categories=IssueCreatedCategory&categories=IssueResolvedCategory&categories=LinksCategory&categories=ProjectCategory&categories=PermittedGroupCategory&categories=SprintCategory&categories=SummaryCategory&categories=TagsCategory&fields=activities(\$type,added(\$type,\$type,\$type,agile(id),attachments(\$type,author(fullName,id,ringId),comment(id),created,id,imageDimension(height,width),issue(id,project(id,ringId)),mimeType,name,removed,size,thumbnailURL,url,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),branch,color(id),commands(end,errorText,hasError,start),comment(id),created,created,creator(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),date,date,deleted,duration(id,minutes,presentation),fetched,files,id,id,id,id,id,id,id,idReadable,isDraft,localizedName,mimeType,minutes,name,name,noHubUserReason(id),noUserReason(id),numberInProject,presentation,processor(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),processors(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),removed,resolved,shortName,size,state,summary,text,text,text,text,textPreview,textPreview,thumbnailURL,type(id,name),url,urls,user(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),userName,usesMarkdown,usesMarkdown,version,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),authorGroup(icon,name),category(id),field(\$type,customField(id,fieldType(isMultiValue,valueType)),id,linkId,presentation),id,markup,removed(\$type,\$type,agile(id),color(id),id,id,idReadable,isDraft,localizedName,name,numberInProject,project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),resolved,summary,text),target(created,id,usesMarkdown),targetMember,targetSubMember,timestamp),cursor&reverse=true"
    )
    fun getHistory(
        @Header("Authorization") auth: String,
        @Path("id") issueId: String
    ): Call<JsonObject>

    @Headers("Accept: application/json")
    @GET(
        "issues/{id}/activitiesPage?\$top=100&categories=CustomFieldCategory&fields=activities(\$type,added(\$type,\$type,\$type,agile(id),attachments(\$type,author(fullName,id,ringId),comment(id),created,id,imageDimension(height,width),issue(id,project(id,ringId)),mimeType,name,removed,size,thumbnailURL,url,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),branch,color(id),commands(end,errorText,hasError,start),comment(id),created,created,creator(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),date,date,deleted,duration(id,minutes,presentation),fetched,files,id,id,id,id,id,id,id,idReadable,isDraft,localizedName,mimeType,minutes,name,name,noHubUserReason(id),noUserReason(id),numberInProject,presentation,processor(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),processors(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),removed,resolved,shortName,size,state,summary,text,text,text,text,textPreview,textPreview,thumbnailURL,type(id,name),url,urls,user(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),userName,usesMarkdown,usesMarkdown,version,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),authorGroup(icon,name),category(id),field(\$type,customField(id,fieldType(isMultiValue,valueType)),id,linkId,presentation),id,markup,removed(\$type,\$type,agile(id),color(id),id,id,idReadable,isDraft,localizedName,name,numberInProject,project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),resolved,summary,text),target(created,id,idReadable,usesMarkdown),targetMember,targetSubMember,timestamp)"
    )
    fun getCustomFieldsHistory(
        @Header("Authorization") auth: String,
        @Path("id") issueId: String
    ): Call<JsonObject>

    @Serializable
    data class ActivityCursorPage(
        var activities: List<ActivityItem>? = null,
        var `$type`: String? = null
    )

    @Serializable
    data class ActivityItem(
        var field: CustomFilterField? = null,
        var id: String? = null,
        var target: fsight.youtrack.models.v2.Issue? = null,
        var timestamp: Long? = null,
        var category: CustomFieldCategory? = null,
        var targetMember: String? = null,
        @Serializable
        var added: Any? = null,
        var removed: Any? = null,
        var author: fsight.youtrack.models.v2.User? = null,
        var `$type`: String? = null,
        var unwrappedAdded: Any?,
        var unwrappedRemoved: Any?
    ) {
        fun unwrapAdded(): String? {
            println(this.added)
            val i = this.added
            return when (this.field?.customField?.fieldType?.valueType) {
                "enum" -> (this.added as List<*>).map { Gson().toJson(it) }.joinToString()
                "version" -> (this.added as List<*>).map { it }.joinToString()
                "state" -> (this.added as List<*>).map { it }.joinToString()
                /*return when (i) {*/
                /*is List<*> -> (i.firstOrNull() as AddedRemovedElement)?.name*/
                /*is List<*> -> Gson().fromJson(i.firstOrNull().toString(), AddedRemovedElement::class.java)?.name*/
                /*is String -> i*/
                else -> i.toString()
            }
        }

        /*fun unwrapRemoved(): String? {
            val i = this.removed
            println(i)
            return when (i) {
                is List<*> -> Gson().fromJson(
                    i.firstOrNull().toString(),
                    AddedRemovedElement::class.java
                )?.name
                is String -> i
                null -> null
                else -> "Unhandled case"
            }
        }*/
    }

    @Serializable
    data class AddedRemovedElement(
        var localizedName: String? = null,
        var name: String? = null,
        var color: String? = null,
        var id: String? = null,
        var `$type`: String? = null
    )

    @Serializable
    data class CustomFilterField(
        var customField: CustomFieldV2? = null,
        var id: String? = null,
        var presentation: String? = null,
        var `$type`: String? = null
    )

    @Serializable
    data class CustomFieldV2(
        var fieldType: FieldType? = null,
        var id: String? = null,
        var `$type`: String? = null
    )

    @Serializable
    data class FieldType(
        var valueType: String? = null,
        var isMultiValue: Boolean? = null,
        var `$type`: String? = null
    )

    @Serializable
    data class CustomFieldCategory(
        var id: String? = null,
        var `$type`: String? = null
    )

    @Headers("Accept: application/json")
    @GET("issue/{id}?with=projectShortName")
    fun check(
        @Header("Authorization") auth: String,
        @Path("id") issueId: String
    ): Call<Issue>

    @Headers("Accept: application/json")
    @GET("issue/{issueId}/timetracking/workitem")
    fun getWorkItems(
        @Header("Authorization") auth: String,
        @Path("issueId") issueId: String
    ): Call<List<WorkItem>>

    @Headers("Accept: application/json")
    @GET("project/all")
    fun getProjectsList(@Header("Authorization") auth: String): Call<List<ProjectModel>>

    @Headers("Accept: application/json")
    @GET("admin/project/{projectId}/customfield")
    fun getProjectCustomFields(
        @Header("Authorization") auth: String,
        @Path("projectId") projectId: String
    ): Call<List<ProjectCustomField>>

    @Headers("Accept: application/json")
    @GET("{path}")
    fun getProjectCustomFieldParameters(
        @Header("Authorization") auth: String,
        @Path("path", encoded = true) path: String
    ): Call<ProjectCustomFieldParameters>

    @Headers("Accept: application/json")
    @GET("admin/users?fields=id,name,email,ringId,login,fullName&\$top=-1")
    fun getUserDetails(
        @Header("Authorization") auth: String
    ): Call<List<UserDetails>>

    @Headers("Accept: application/json")
    @GET("admin/user")
    fun getUsers(
        @Header("Authorization") auth: String,
        @Query("start") start: Int
    ): Call<List<User>>

    companion object Factory {
        fun create(converter: Converter = Converter.SCALAR): YouTrackAPI {
            val converterFactory = when (converter) {
                Converter.SCALAR -> {
                    ScalarsConverterFactory.create()
                }
                else -> {
                    val gson = GsonBuilder().setLenient().create()
                    GsonConverterFactory.create(gson)
                }
            }
            return Retrofit
                .Builder()
                .baseUrl(NEW_ROOT_REF)
                .addConverterFactory(converterFactory)
                .build()
                .create(YouTrackAPI::class.java)
        }

        fun createOld(converter: Converter): YouTrackAPI {
            val converterFactory = when (converter) {
                Converter.SCALAR -> {
                    ScalarsConverterFactory.create()
                }
                else -> {
                    val gson = GsonBuilder().setLenient().create()
                    GsonConverterFactory.create(gson)
                }
            }
            return Retrofit.Builder()
                .baseUrl(ROOT_REF)
                .addConverterFactory(converterFactory)
                .build()
                .create(YouTrackAPI::class.java)

        }
    }
}
