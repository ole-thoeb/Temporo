package com.eloem.temporo

import com.eloem.temporo.util.*
import com.eloem.temporo.util.binarytree.inOrderIterator
import org.junit.Test

class ParsTest {
    @Test
    fun splitMapBracket() {
        //val input = "(a + (a + b) * c) / b"
        //val input = "!false && false"
        val sTime = System.currentTimeMillis()
        val input = "false"
        input.splitToTokens().chainSuccess {
            mapOperations(this)
        }.chainSuccess {
            addBrackets(this)
        }.chainSuccess {
            buildOperationTree(this)
        }.withSuccess {
            //println(this)
            println(buildLogicOperation(this).withSuccess { execute() })
            println("time: ${System.currentTimeMillis() - sTime}")
        }.throwError()
    }
}