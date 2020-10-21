package fsight.youtrack.api.tfs

import com.google.gson.Gson
import fsight.youtrack.integrations.devops.hooks.ITFSHooks
import fsight.youtrack.integrations.devops.revisions.IDevOpsRevisions
import fsight.youtrack.models.hooks.Hook
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class TFSDataController(private val dataService: ITFSData, private val hooksService: ITFSHooks, private val devOpsRevisions: IDevOpsRevisions) {
    private val logger = KotlinLogging.logger {}

    @GetMapping("/api/tfs/count")
    fun getItemsCount() = dataService.getItemsCount()

    @GetMapping("/api/tfs")
    fun getItems(
        @RequestParam("offset", required = false) offset: Int? = null,
        @RequestParam("limit", required = false) limit: Int? = null,
        @RequestParam("action", required = false) action: String? = null,
        @RequestParam("iteration", required = false) iteration: String? = null,
        @RequestParam("build", required = false) build: String? = null,
        @RequestParam("changeRequestId", required = false) changeRequestId: Int? = null
    ): ResponseEntity<Any> {
        return when {
            action == "postToYT" && iteration != null -> dataService.postItemsToYouTrack(iteration)
            action == "postToYT" && iteration == null && offset != null && limit != null -> dataService.postItemsToYouTrack(
                offset,
                limit
            )
            action == "fixed" && iteration != null && build != null -> dataService.getDefectsByFixedBuildId(
                iteration,
                build
            )
            else -> ResponseEntity.status(HttpStatus.OK).body("Undefined set of parameters.")
        }
    }

    @PostMapping("/api/tfs")
    fun postChangeRequest(
        @RequestParam("action", required = false) action: String? = null,
        @RequestParam("changeRequestId", required = false) changeRequestId: Int? = null,
        @RequestBody(required = false) body: String? = null
    ): ResponseEntity<Any> {
        println(body)
        return when {
            action == "postChangeRequest" && changeRequestId != null -> dataService.postChangeRequestById(
                changeRequestId,
                body
            )
            else -> /*service.getItems(offset, limit)*/ ResponseEntity.status(HttpStatus.OK).body("Undefined set of parameters.")
        }
    }

    @GetMapping("/api/tfs/{id}")
    fun postItemToYouTrack(
        @PathVariable("id") id: Int,
        @RequestParam("action", required = false) action: String? = null
    ): ResponseEntity<Any> = when (action) {
        "postToYT" -> dataService.postItemToYouTrack(id)
        "toJSON" -> dataService.toJson(id)

        else -> ResponseEntity.status(HttpStatus.OK).body(dataService.getItemById(id))
    }

    @GetMapping("/api/tfs/iterations")
    fun getAllIterations(): ResponseEntity<Any> = dataService.getIterations()

    @GetMapping("/api/tfs/builds")
    fun getBuildsByIteration(@RequestParam("iteration") iteration: String? = null): ResponseEntity<Any> =
        when (iteration) {
            null -> dataService.getBuildsByIteration("\\P7\\PP9\\9.0\\1.0\\Update 1")
            else -> dataService.getBuildsByIteration(iteration)
        }

    /*@PostMapping(value = ["/api/tfs/serviceHooks", "/api/tfs/serviceHooks/wiUpdated"])
    fun postHookOnWIUpdated(
        @RequestParam("bugs", required = false) bugs: List<Int>? = null,
        @RequestBody body: String?
    ): ResponseEntity<Any> {
        return try {
            val jsonBody = Gson().fromJson(body, Hook::class.java)
            hooksService.handleWiUpdated(jsonBody)
        } catch (e: Exception) {
            println("WorkItem Updated: ${e.message}")
            logger.info { "WorkItem Updated: ${e.message}" }
            logger.info { body }
            ResponseEntity.status(HttpStatus.OK).body(e.message)
        }
    }*/

    @PostMapping("/api/tfs/serviceHooks/wiUpdated")
    fun postHookOnWIUpdated(
        @RequestParam("bugs", required = false) bugs: List<Int>? = null,
        @RequestBody body: String?
    ): ResponseEntity<Any> {
        return try {
            val jsonBody = Gson().fromJson(body, Hook::class.java)
            hooksService.handleWiUpdated(jsonBody)
        } catch (e: Exception) {
            println("WorkItem Updated: ${e.message}")
            logger.info { "WorkItem Updated: ${e.message}" }
            logger.info { body }
            ResponseEntity.status(HttpStatus.OK).body(e.message)
        }
    }

    @PostMapping("/api/tfs/serviceHooks/wiCommented")
    fun postHookOnWICommented(
        @RequestParam("bugs", required = false) bugs: List<Int>? = null,
        @RequestBody body: String?
    ): ResponseEntity<Any> {
        return try {
            val jsonBody = Gson().fromJson(body, Hook::class.java)
            hooksService.handleWiCommented(jsonBody)
        } catch (e: Exception) {
            println("WorkItem Commented: ${e.message}")
            logger.info { "WorkItem Commented: ${e.message}" }
            logger.info { body }
            ResponseEntity.status(HttpStatus.OK).body(e.message)
        }
    }

    @GetMapping("/api/tfs/serviceHooks/post/{id}")
    fun postCommand(@PathVariable("id") id: String? = null): ResponseEntity<Any> {
        return hooksService.postCommand(id, "Состояние Открыта")
    }


    @GetMapping("/api/tfs/revision")
    fun startRevision(
        @RequestParam("stage", required = false) stage: Int? = null,
        @RequestParam("limit", required = false) limit: Int? = null,
        @RequestParam("offset", required = false) offset: Int? = null
    ): Any = devOpsRevisions.getActiveBugsAndFeatures(stage, limit, offset)
}


