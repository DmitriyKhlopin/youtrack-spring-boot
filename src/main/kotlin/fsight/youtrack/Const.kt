package fsight.youtrack

enum class ETLState(val state: Int) {
    IDLE(0),
    RUNNING(1),
    DONE(2)
}