package fsight.youtrack.config

import org.springframework.stereotype.Service

@Service
class AppConfig : IAppConfig {
    private lateinit var conf: Any

    init {
        this.readConfig()
    }

    override val config: Any
        get() = conf

    override fun readConfig() {
        conf = "This is config"
    }
}
