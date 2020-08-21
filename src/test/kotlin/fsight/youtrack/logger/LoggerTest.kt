package fsight.youtrack.logger

import mu.KotlinLogging
import org.junit.jupiter.api.Test

private val logger = KotlinLogging.logger {}

class LoggerTest {

    @Test
    fun bar() {
        logger.info { "hi this is a test" }
        assert(true)
    }
}