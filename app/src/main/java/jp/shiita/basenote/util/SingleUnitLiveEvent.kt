package jp.shiita.basenote.util

class SingleUnitLiveEvent : SingleLiveEvent<Unit>() {
    fun call() {
        postValue(Unit)
    }
}