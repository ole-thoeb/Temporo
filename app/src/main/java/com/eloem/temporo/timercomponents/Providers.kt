package com.eloem.temporo.timercomponents

interface Provider<T> {
    fun newValue(): T
}

class ColorProvider(colors: List<Int>) : Provider<Int> {

    private val colorMap: MutableMap<Int, Boolean> = HashMap()
    init {
        colors.forEach {
            colorMap[it] = false
        }
    }

    fun free(color: Int) {
        colorMap[color] = false
    }

    override fun newValue(): Int {
        val newColor = colorMap.filter { !it.value }.keys.first()
        colorMap[newColor] = true
        return newColor
    }
}