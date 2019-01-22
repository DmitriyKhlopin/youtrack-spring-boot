package fsight.youtrack.api.tfs

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class TFSDataController(private val service: ITFSData) {
    @GetMapping("/api/tfs/count")
    fun getItemsCount() = service.getItemsCount()

    @GetMapping("/api/tfs")
    fun getItems(
        @RequestParam("offset", required = false) offset: Int? = null,
        @RequestParam("limit", required = false) limit: Int? = null,
        @RequestParam("action", required = false) action: String? = null,
        @RequestParam("iteration", required = false) iteration: String? = null,
        @RequestParam("build", required = false) build: String? = null,
        @RequestParam("changeRequestId", required = false) changeRequestId: Int? = null
    ): ResponseEntity<Any> = when {
        action == "postChangeRequest" && changeRequestId != null -> service.postChangeRequestById(changeRequestId)
        action == "postToYT" && iteration != null -> service.postItemsToYouTrack(iteration)
        action == "postToYT" && iteration == null && offset != null && limit != null -> service.postItemsToYouTrack(
            offset,
            limit
        )
        action == "fixed" && iteration != null && build != null -> service.getDefectsByFixedBuildId(iteration, build)
        else -> /*service.getItems(offset, limit)*/ ResponseEntity.status(HttpStatus.OK).body("Undefined set of parameters.")
    }

    @GetMapping("/api/tfs/{id}")
    fun postItemToYouTrack(
        @PathVariable("id") id: Int,
        @RequestParam("action", required = false) action: String? = null
    ): ResponseEntity<Any> = when (action) {
        "postToYT" -> service.postItemToYouTrack(id)
        "toJSON" -> service.toJson(id)

        else -> ResponseEntity.status(HttpStatus.OK).body(service.getItemById(id))
    }

    @GetMapping("/api/tfs/iterations")
    fun getAllIterations(): ResponseEntity<Any> = service.getIterations()

    @GetMapping("/api/tfs/builds")
    fun getBuildsByIteration(@RequestParam("iteration") iteration: String? = null): ResponseEntity<Any> =
        when (iteration) {
            null -> service.getBuildsByIteration("\\P7\\PP9\\9.0\\1.0\\Update 1")
            else -> service.getBuildsByIteration(iteration)
        }

}


