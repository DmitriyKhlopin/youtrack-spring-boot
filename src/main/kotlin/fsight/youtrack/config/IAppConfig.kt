package fsight.youtrack.config

interface IAppConfig{
    val config: Any
    fun readConfig()
}
