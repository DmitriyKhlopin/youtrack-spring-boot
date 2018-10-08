package fsight.youtrack.etl.bundles

import fsight.youtrack.AUTH
import fsight.youtrack.generated.jooq.tables.Bundles.BUNDLES
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.SocketTimeoutException

@Service
@Transactional
class BundleImplementation(private val dslContext: DSLContext) : BundleService {
    override fun getBundles() {
        dslContext.deleteFrom(BUNDLES).execute()
        val bundles = arrayListOf("Приоритеты", "PPVersions", "Типы", "Prognoz Platform Лицензирование: Тип сервера",
                "Prognoz Platform Лицензирование: Prognoz Platform Server")
        bundles.forEach { getBundleValues(it) }
    }

    private fun getBundleValues(bundle: String) {
        try {
            val res = BundleRetrofitService.create().getBundleValues(AUTH, bundle).execute().body()
            val bundleName = res?.name ?: ""
            res?.value?.forEach {
                dslContext.insertInto(BUNDLES)
                        .set(BUNDLES.BUNDLE, bundleName)
                        .set(BUNDLES.VAL, it.value)
                        .set(BUNDLES.DESCRIPTION, it.description)
                        .set(BUNDLES.COLOR_INDEX, it.colorIndex)
                        .set(BUNDLES.LOCALIZED_NAME, it.localizedName)
                        .executeAsync()
            }
        } catch (e: SocketTimeoutException) {
            println(e)
        } catch (e: DataAccessException) {
            println(e)
        }
    }
}



