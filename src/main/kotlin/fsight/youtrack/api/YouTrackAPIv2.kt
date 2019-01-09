package fsight.youtrack.api

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import fsight.youtrack.Converter
import fsight.youtrack.NEW_ROOT_REF
import fsight.youtrack.models.UserDetails
import fsight.youtrack.models.YouTrackIssue
import fsight.youtrack.models.YouTrackIssueWorkItem
import fsight.youtrack.models.YouTrackUser
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

interface YouTrackAPIv2 {
    @Headers("Accept: application/json")
    @GET("api/issues")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("fields") fields: String,
        @Query("\$top") top: Int,
        @Query("\$skip") skip: Int
    ): Call<List<YouTrackIssue>>


    @Headers("Accept: application/json")
    @GET("api/issues")
    fun getIssueList(
        @Header("Authorization") auth: String,
        @Query("fields") fields: String,
        @Query("\$top") top: Int,
        @Query("\$skip") skip: Int,
        @Query("query") query: String
    ): Call<List<YouTrackIssue>>


    @Headers("Accept: application/json")
    @GET("api/issues/{issueId}/timeTracking/workItems?\$top=100&fields=created,date,duration(minutes),updated,author(login,email),creator,id,type(id,name,autoAttached),text")
    fun getWorkItems(
        @Header("Authorization") auth: String,
        @Path("issueId") issueId: String
    ): Call<List<YouTrackIssueWorkItem>>

    @Headers("Accept: application/json")
    @GET(
        "api/issues/{id}/activitiesPage?\$top=100&categories=CommentsCategory&categories=WorkItemCategory&categories=AttachmentsCategory&categories=AttachmentRenameCategory&categories=CustomFieldCategory&categories=DescriptionCategory&categories=IssueCreatedCategory&categories=IssueResolvedCategory&categories=LinksCategory&categories=ProjectCategory&categories=PermittedGroupCategory&categories=SprintCategory&categories=SummaryCategory&categories=TagsCategory&fields=activities(\$type,added(\$type,\$type,\$type,agile(id),attachments(\$type,author(fullName,id,ringId),comment(id),created,id,imageDimension(height,width),issue(id,project(id,ringId)),mimeType,name,removed,size,thumbnailURL,url,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),branch,color(id),commands(end,errorText,hasError,start),comment(id),created,created,creator(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),date,date,deleted,duration(id,minutes,presentation),fetched,files,id,id,id,id,id,id,id,idReadable,isDraft,localizedName,mimeType,minutes,name,name,noHubUserReason(id),noUserReason(id),numberInProject,presentation,processor(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),processors(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),removed,resolved,shortName,size,state,summary,text,text,text,text,textPreview,textPreview,thumbnailURL,type(id,name),url,urls,user(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),userName,usesMarkdown,usesMarkdown,version,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),authorGroup(icon,name),category(id),field(\$type,customField(id,fieldType(isMultiValue,valueType)),id,linkId,presentation),id,markup,removed(\$type,\$type,agile(id),color(id),id,id,idReadable,isDraft,localizedName,name,numberInProject,project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),resolved,summary,text),target(created,id,usesMarkdown),targetMember,targetSubMember,timestamp),cursor&reverse=true"
        /*"issues/{id}/activitiesPage?categories=CommentsCategory&categories=WorkItemCategory&categories=AttachmentsCategory&categories=AttachmentRenameCategory&categories=CustomFieldCategory&categories=DescriptionCategory&categories=IssueCreatedCategory&categories=IssueResolvedCategory&categories=LinksCategory&categories=ProjectCategory&categories=PermittedGroupCategory&categories=SprintCategory&categories=SummaryCategory&categories=TagsCategory&fields=activities(\$type,added(\$type,\$type,\$type,agile(id),attachments(\$type,author(fullName,id,ringId),comment(id),created,id,imageDimension(height,width),issue(id,project(id,ringId)),mimeType,name,removed,size,thumbnailURL,url,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),branch,color(id),commands(end,errorText,hasError,start),comment(id),created,created,creator(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),date,date,deleted,duration(id,minutes,presentation),fetched,files,id,id,id,id,id,id,id,idReadable,isDraft,localizedName,mimeType,minutes,name,name,noHubUserReason(id),noUserReason(id),numberInProject,presentation,processor(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),processors(\$type,committers,enabled,handle,handle,id,login,params,progress(message),project(id),repoName,repoOwnerName,repository,repositoryOwner,server(id,url,enabled),stateMessage,tcId,upsourceHubResourceKey,upsourceProjectName,version),project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),removed,resolved,shortName,size,state,summary,text,text,text,text,textPreview,textPreview,thumbnailURL,type(id,name),url,urls,user(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),userName,usesMarkdown,usesMarkdown,version,visibility(\$type,implicitPermittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),permittedGroups(\$type,allUsersGroup,icon,id,name,ringId),permittedUsers(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId))),author(\$type,avatarUrl,email,fullName,id,isLocked,issueRelatedGroup(icon),login,name,online,ringId),authorGroup(icon,name),category(id),field(\$type,customField(id,fieldType(isMultiValue,valueType)),id,linkId,presentation),id,markup,removed(\$type,\$type,agile(id),color(id),id,id,idReadable,isDraft,localizedName,name,numberInProject,project(\$type,id,name,plugins(timeTrackingSettings(enabled,estimate(field(id,name),id),timeSpent(field(id,name),id)),vcsIntegrationSettings(processors(enabled,url,upsourceHubResourceKey,server(enabled,url)))),ringId,shortName),resolved,summary,text),target(created,id,usesMarkdown),targetMember,targetSubMember,timestamp),cursor&reverse=true"*/
    )
    fun getHistory(
        @Header("Authorization") auth: String,
        @Path("id") issueId: String/*,*/
        /*@Path("categories") categories: String,*/
        /*@Query("\$top") top: Int,
        @Query("fields") fields: String*/
    ): Call<JsonObject>

    @Headers("Accept: application/json")
    @GET("hub/api/rest/users?&\$top=-1&fields=id,login,name,profile(email),groups(name)")
    fun getUserDetails(
        @Header("Authorization") auth: String
    ): Call<HubResponse>

    data class HubResponse(
        var skip: Int? = null,
        var total: Int? = null,
        var users: List<YouTrackUser>? = null
    )

    companion object Factory {
        fun create(converter: Converter = Converter.SCALAR): YouTrackAPIv2 {
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
                .create(YouTrackAPIv2::class.java)
        }
    }
}
