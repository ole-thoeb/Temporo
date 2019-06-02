package com.eloem.temporo.util

import com.eloem.temporo.util.binarytree.TreeNode
import java.lang.Exception
import java.lang.StringBuilder
import kotlin.IllegalStateException

enum class TokenCategory { OPERATOR, VARIABLE, CONSTANT, BRACKET, ILLEGAL }

fun String.splitToTokens(): List<Pair<String, TokenCategory>> {
    return replace(" ", "").splitToTokensNoBlanks()
}

private fun String.splitToTokensNoBlanks(): List<Pair<String, TokenCategory>> {
    if (isEmpty()) return emptyList()

    val tokenList = mutableListOf<Pair<String, TokenCategory>>()

    var currentToken = StringBuilder()
    var currentCategory = first().category()
    currentToken.append(first())

    for (i in 1..lastIndex) {
        val category = this[i].category()
        //bracket is always one character
        if (currentCategory != category || category == TokenCategory.BRACKET) {
            tokenList.add(currentToken.toString() to currentCategory)
            currentToken = StringBuilder()
            currentCategory = category
        }
        currentToken.append(this[i])
    }

    tokenList.add(currentToken.toString() to currentCategory)

    return tokenList
}

//private val variableRegex = """[A-Za-z]""".toRegex()
//private val operatorRegex = """[<>=+\-*/]""".toRegex()

fun Char.category(): TokenCategory {
    return when {
        this in 'A'..'Z' || this in 'a'..'z' -> TokenCategory.VARIABLE
        this == '<' || this == '>' || this == '=' || this == '+' || this == '-' ||
                this == '*' || this == '/' || this == '&' || this == '|' || this == '!' -> TokenCategory.OPERATOR
        this in '0'..'9' -> TokenCategory.CONSTANT
        this == '(' || this == ')' -> TokenCategory.BRACKET
        else -> TokenCategory.ILLEGAL
    }
}

fun mapOperations(tokens: List<Pair<String, TokenCategory>>): List<AtomicToken> {
    val cleanedList = tokens.filterNot { it.second == TokenCategory.ILLEGAL }
    return cleanedList.map<Pair<String, TokenCategory>, AtomicToken> {
        when(it.second) {
            TokenCategory.BRACKET -> {
                if (it.first == "(") Bracket.OPEN
                else Bracket.CLOSE
            }
            TokenCategory.CONSTANT -> {
                ConstantInt(it.first.toInt())
            }
            TokenCategory.VARIABLE -> {
                when {
                    it.first.equals("false", true) -> ConstFalse
                    it.first.equals("true", true) -> ConstTrue
                    else -> Variable(it.first)
                }
            }
            TokenCategory.OPERATOR -> {
                when(it.first) {
                    "==" -> Equal<Any, Any>()
                    "!=" -> NotEqual<Any, Any>()
                    "<" -> Smaller()
                    ">" -> Greater()
                    "<=" -> SmallerEqual()
                    ">=" -> GreaterEqual()
                    "&&" -> And()
                    "||" -> Or()
                    "!" -> Not()
                    "+" -> Plus()
                    "-" -> Minus()
                    "*" -> Mult()
                    "/" -> Div()
                    else -> throw IllegalTokenException("Unknown token string ${it.first}")
                }
            }
            TokenCategory.ILLEGAL -> throw IllegalTokenException("Unknown token string ${it.first}")
        }
    }
}

fun List<AtomicToken>.expressionEndLeft(startIndex: Int): Int {
    var i = startIndex - 1
    var brackets = 0
    while (i >= 0) {
        if (this[i] == Bracket.CLOSE) brackets++
        else if (this[i] == Bracket.OPEN) brackets--
        if (brackets == 0) return i
        i--
    }
    throw IllegalBracketException()
}

fun List<AtomicToken>.expressionEndRight(startIndex: Int) : Int {
    var i = startIndex + 1
    var brackets = 0
    while (i < size) {
        if (this[i] == Bracket.CLOSE) brackets--
        else if (this[i] == Bracket.OPEN) brackets++
        if (brackets == 0) return i
        i++
    }
    throw IllegalBracketException()
}

fun List<AtomicToken>.hasBracketsAtEnds(): Boolean {
    var j = 0
    var brackets = 0
    while (j < size) {
        if (this[j] == Bracket.CLOSE) brackets--
        else if (this[j] == Bracket.OPEN) brackets++
        if (brackets == 0) return j == lastIndex
        j++
    }
    throw IllegalBracketException()
}

fun addBrackets(tokens: List<AtomicToken>): List<AtomicToken> {
    val mutable = tokens.toMutableList()

    var i = 0
    while (i < mutable.size) {
        val cur = mutable[i]
        if (cur is Not) {
            val endRight = mutable.expressionEndRight(i)
            if (endRight != mutable.lastIndex && mutable[endRight + 1] != Bracket.CLOSE) {
                mutable.add(i , Bracket.OPEN)
                i++
                mutable.add(endRight + 2, Bracket.CLOSE)
            }
        }
        i++
    }
    i = 0
    while (i < mutable.size) {
        val cur = mutable[i]
        if (cur is Div || cur is Mult) {
            val endLeft = mutable.expressionEndLeft(i)
            val endRight = mutable.expressionEndRight(i)
            //one of the indexes is not at the edge and there isn't already the bracket
            if ((endLeft != 0 && mutable[endLeft - 1] != Bracket.OPEN) ||
                (endRight != mutable.lastIndex && mutable[endRight + 1] != Bracket.CLOSE)) {
                mutable.add(endLeft, Bracket.OPEN)
                i++
                mutable.add(endRight + 2, Bracket.CLOSE)
            }
        }
        i++
    }
    i = 0
    while (i < mutable.size) {
        val cur = mutable[i]
        if (cur is TokenOperation<*> && cur !is Div && cur !is Mult && cur !is Not) {
            val endLeft = mutable.expressionEndLeft(i)
            val endRight = mutable.expressionEndRight(i)
            //one of the indexes is not at the edge and there isn't already the bracket
            if ((endLeft != 0 && mutable[endLeft - 1] != Bracket.OPEN) ||
                (endRight != mutable.lastIndex && mutable[endRight + 1] != Bracket.CLOSE)) {
                mutable.add(endLeft, Bracket.OPEN)
                i++
                mutable.add(endRight + 2, Bracket.CLOSE)
            }
        }
        i++
    }
    if (mutable.first() !is Bracket.OPEN || !mutable.hasBracketsAtEnds()) {
        mutable.add(0, Bracket.OPEN)
        mutable.add(Bracket.CLOSE)
    }
    return mutable
}

fun buildOperationTree(tokens: List<AtomicToken>): TreeNode<AtomicToken> {
    var current: TreeNode<AtomicToken> = TreeNode.valueOf(PLACEHOLDER_TOKEN)
    for (i in 1..tokens.lastIndex) {
        when(val cur = tokens[i]) {
            is Bracket.OPEN -> {
                val newNode: TreeNode<AtomicToken> = TreeNode.valueOf(PLACEHOLDER_TOKEN)
                when {
                    current.left == null -> current.insertLeft(newNode)
                    current.right == null -> current.insertRight(newNode)
                    else -> throw IllegalStateException("can't open another bracket.")
                }
                current = newNode
            }
            is Bracket.CLOSE -> {
                if (i != tokens.lastIndex) current = current.requireParent()
            }
            is TokenOperation<*> -> {
                current.requirePlaceholder(cur)
                current.value = cur
            }
            is Value<*> -> when {
                current.left == null -> current.insertLeft(TreeNode.valueOf(cur))
                current.right == null -> current.insertRight(TreeNode.valueOf(cur))
                else -> throw IllegalStateException("no space to put $cur")
            }
        }
    }
    return current
}

fun TreeNode<AtomicToken>.requireParent() = parent ?: throw IllegalStateException("could not return to parent")

fun TreeNode<AtomicToken>.requirePlaceholder(neededFor: AtomicToken) {
    if (value != PLACEHOLDER_TOKEN) {
        throw IllegalTokenException("PlaceHolder expected for $neededFor")
    }
}

open class IllegalTokenException(msg: String): Exception(msg)

class IllegalBracketException(msg: String = "Bad Brackets"): IllegalTokenException(msg)