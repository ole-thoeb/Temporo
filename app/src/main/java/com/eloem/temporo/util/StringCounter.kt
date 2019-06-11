package com.eloem.temporo.util

class StringCounter (val fixedStart: String = "", val fixedEnd: String = "") {

    private val constSize = fixedStart.length + fixedEnd.length
    private val counter = mutableListOf(97 - 1)

    fun next(): String {
        val counterStr = nextCountString()
        return buildString(constSize + counterStr.length) {
            append(fixedStart)
            append(counterStr)
            append(fixedEnd)
        }
    }

    private fun nextCountString(): String {
        increaseCount()
        return buildString(counter.size) {
            counter.forEach { append(it.toChar()) }
        }
    }

    private fun increaseCount() {
        var i = counter.lastIndex
        while (i >= 0) {
            counter[i]++
            if (counter[i] > 122) {
                counter[i] = 97
                if (i == 0) {
                    counter.add(97)
                }
            } else {
                break
            }
            i--
        }
    }
}