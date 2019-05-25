package com.eloem.temporo.timercomponents

class DataSequence(val id: Long, val title: String, components: List<DataComponent>): List<DataComponent> by components