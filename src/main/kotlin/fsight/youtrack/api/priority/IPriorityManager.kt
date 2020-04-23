package fsight.youtrack.api.priority

import org.springframework.http.ResponseEntity

interface IPriorityManager {
    fun getPriority(props: List<Any>): ResponseEntity<Any>
}