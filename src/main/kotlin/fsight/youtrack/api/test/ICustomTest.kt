package fsight.youtrack.api.test

import fsight.youtrack.ETLState

interface ICustomTest {
    fun increment(): Int
    fun getState(): ETLState
}