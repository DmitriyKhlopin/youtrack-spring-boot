package fsight.youtrack

import fsight.youtrack.etl.IETL
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.web.bind.annotation.RestController

@RestController
class ETLWebSocket(private val service: IETL) {
    /*@Scheduled(fixedRate = 10000)*/
    @MessageMapping("/etl/wsstate")
    @SendTo("/topic/all")
    fun greeting(): String {
        Thread.sleep(1000)
        println(System.currentTimeMillis())
        return System.currentTimeMillis().toString()
    }
}