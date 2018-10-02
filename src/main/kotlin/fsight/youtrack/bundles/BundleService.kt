package fsight.youtrack.bundles

import fsight.youtrack.AUTH
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import youtrack.jooq.tables.Bundles.BUNDLES

@Service
@Transactional
class BundleService(private val dslContext: DSLContext) {
    fun getBundles() {
        dslContext.deleteFrom(BUNDLES).execute()
        val bundles = arrayListOf("Приоритеты", "PPVersions", "Типы", "Prognoz Platform Лицензирование: Тип сервера",
                "Prognoz Platform Лицензирование: Prognoz Platform Server")
        bundles.forEach { getBundleValues(it) }
    }

    private fun getBundleValues(bundle: String) {
        val res = BundleRetrofitService.create().getBundleValues(AUTH, bundle).execute().body()
        val bundleName = res?.name ?: ""
        res?.value?.forEach {
            try {
                dslContext.insertInto(BUNDLES)
                        .set(BUNDLES.BUNDLE, bundleName)
                        .set(BUNDLES.VAL, it.value)
                        .set(BUNDLES.DESCRIPTION, it.description)
                        .set(BUNDLES.COLOR_INDEX, it.colorIndex)
                        .set(BUNDLES.LOCALIZED_NAME, it.localizedName)
                        .execute()
            } catch (e: DataAccessException) {
                println(e.message)
            }
        }
    }

}



