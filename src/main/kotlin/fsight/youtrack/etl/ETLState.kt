package fsight.youtrack.etl

import fsight.youtrack.ETLState
import org.springframework.stereotype.Service


@Service
class ETLState : IETLState {
    private var localState = ETLState.IDLE
    override var state: ETLState
        get() = localState
        set(value) {
            localState = value
        }
}