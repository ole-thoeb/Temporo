package com.eloem.temporo

import com.eloem.temporo.util.addBrackets
import com.eloem.temporo.util.binarytree.inOrderIterator
import com.eloem.temporo.util.buildOperationTree
import com.eloem.temporo.util.mapOperations
import com.eloem.temporo.util.splitToTokens
import org.junit.Test

class ParsTest {
    @Test
    fun splitMapBracket() {
        //val input = "(a + (a + b) * c) / b"
        val input = "!a && false"
        val parsedSequence = addBrackets(mapOperations(input.splitToTokens()))
        println(parsedSequence)
        val tree = buildOperationTree(parsedSequence)
        println(tree)
    }
}