package fsight.youtrack.api.priority

import org.jooq.DSLContext
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class PriorityManager(private val dsl: DSLContext) : IPriorityManager {
    override fun getPriority(props: List<Any>): ResponseEntity<Any> {
        return ResponseEntity.ok("Test")
    }
}