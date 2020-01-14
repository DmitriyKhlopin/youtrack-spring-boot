package fsight.youtrack


import fsight.youtrack.api.dictionaries.IDictionary
import org.jetbrains.exposed.sql.Database
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


fun main(args: Array<String>) {
    runApplication<YouTrackApplication>(*args)
}

@SpringBootApplication
@EnableWebMvc
@EnableScheduling
class YouTrackApplication : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(YouTrackApplication::class.java)
    }

    @Bean
    fun msDataSource(): Database {
        return Database.connect(
                url = MS_URL,
                driver = MS_DRIVER,
                user = MS_USER,
                password = MS_PASSWORD
        )
    }

    @Bean
    fun pgDataSource(): Database {
        return Database.connect(
                url = PG_URL,
                driver = PG_DRIVER,
                user = PG_USER,
                password = PG_PASSWORD
        )
    }

    @Bean
    fun gkbDataSource(): Database {
        return Database.connect(
                url = GKB_URL,
                driver = PG_DRIVER,
                user = GKB_USER,
                password = GKB_PASSWORD
        )
    }

    @Bean
    fun tfsDataSource(): Database {
        return Database.connect(
                url = TFS_URL,
                driver = TFS_DRIVER,
                user = TFS_USER,
                password = TFS_PASSWORD
        )
    }

    @Bean
    fun corsConfigurer(): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                super.addCorsMappings(registry)
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://10.9.172.12:3000",
                                "http://10.9.172.76:3000",
                                "http://localhost:8080",
                                "http://10.30.207.22:8080"
                        )
            }
        }
    }

    @Bean
    fun initProjects(service: IDictionary) = CommandLineRunner {
        service.preloadCommercialProjects()
        service.preloadInnerProjects()
        service.preloadDevOpsStates()
    }

    /*@Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.messageConverters
            .add(0, StringHttpMessageConverter(Charset.forName("UTF-8")))
        return restTemplate
    }*/
}