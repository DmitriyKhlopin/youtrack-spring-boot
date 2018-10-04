package fsight.youtrack.models

import fsight.youtrack.ETLState

data class ETLResult(val state: ETLState, val issues: Int, val timeUnit: Int)