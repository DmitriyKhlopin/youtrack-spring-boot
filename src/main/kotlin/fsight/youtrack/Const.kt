package fsight.youtrack

enum class ETLState(val state: Int) {
    IDLE(0),
    RUNNING(1),
    DONE(2)
}

enum class Converter {
    GSON, SCALAR
}

const val REACT_STATIC_DIR = "static"
const val REACT_DIR = "/static/"