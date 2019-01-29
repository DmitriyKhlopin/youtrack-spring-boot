package fsight.youtrack.etl.bundles

import fsight.youtrack.AUTH
import fsight.youtrack.Converter
import fsight.youtrack.api.YouTrackAPIv2
import fsight.youtrack.generated.jooq.tables.BundleValues.BUNDLE_VALUES
import fsight.youtrack.generated.jooq.tables.records.BundleValuesRecord
import fsight.youtrack.toDatabaseRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Service

@Service
class BundleV2(private val dslContext: DSLContext) : IBundle {
    override fun getBundles() {
        val arr = arrayListOf<BundleValuesRecord>()
        val res = YouTrackAPIv2.create(Converter.GSON).getListOfCustomFields(AUTH).execute()
        val ids = res.body()?.mapNotNull { field -> field.id }
        println("Loaded ${ids?.size} bundles")
        ids?.forEachIndexed { index, id ->
            val response = YouTrackAPIv2.create(Converter.GSON).getCustomFieldInstances(AUTH, id).execute()
            val result = response.body()
            result?.instances?.forEach { instance: FieldInstance ->
                arr.addAll(instance.bundle?.values?.map { value ->
                    value.apply {
                        projectId = instance.project?.id
                        projectName = instance.project?.shortName
                        fieldId = instance.id
                        fieldName = result.name
                        `$type` = instance.bundle.`$type`
                    }
                    value.toDatabaseRecord()
                }.orEmpty())
            }
            print("Transforming ${index + 1} of ${ids.size}\r")
        }
        println("Saving to database")
        dslContext.deleteFrom(BUNDLE_VALUES).execute()
        val stored = dslContext.loadInto(BUNDLE_VALUES).loadRecords(arr).fields(
            BUNDLE_VALUES.ID,
            BUNDLE_VALUES.NAME,
            BUNDLE_VALUES.PROJECT_ID,
            BUNDLE_VALUES.PROJECT_NAME,
            BUNDLE_VALUES.FIELD_ID,
            BUNDLE_VALUES.FIELD_NAME,
            BUNDLE_VALUES.TYPE
        ).execute().stored()
        println("Saved ${if (arr.size == stored) "all($stored)" else "$stored of ${arr.size}"} bundle values")
    }
}
