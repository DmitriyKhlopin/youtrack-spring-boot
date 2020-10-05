package fsight.youtrack.api.db.devops

import fsight.youtrack.db.IDevOpsProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DevOpsController {
    @Autowired
    private lateinit var devops: IDevOpsProvider

    @GetMapping("/api/db/devops/features_by_planning_board_state")
    fun startRevision(
        /*@RequestParam("stage", required = false) states: Int? = null*/
    ): Any = devops.getFeaturesByPlanningBoardStates(listOf("Отклонено", "На уточнении"))
}
