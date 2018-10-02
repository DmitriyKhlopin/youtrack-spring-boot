package fsight.youtrack


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
/*@Configuration*/
class YoutrackApplication : SpringBootServletInitializer() {
    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(YoutrackApplication::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<YoutrackApplication>(*args)
}