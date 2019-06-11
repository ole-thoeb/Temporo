package com.eloem.temporo.util

import com.eloem.temporo.util.binarytree.TreeNode
import com.eloem.temporo.util.binarytree.nodeInOrderIterator
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.StringBuilder
import kotlin.IllegalStateException

enum class TokenCategory { OPERATOR, VARIABLE, CONSTANT, BRACKET }

//fun String.splitToTokens(): List<Pair<String, TokenCategory>> {
//    return replace(" ", "").splitToTokensNoBlanks()
//}

fun String.splitToTokens(): Optional<List<Pair<String, TokenCategory>>, IllegalTokenError> {
    if (isEmpty()) return Optional.Success(emptyList())

    val tokenList = mutableListOf<Pair<String, TokenCategory>>()

    var i = 0
    while (this[i] == ' ') i++
    var currentToken = StringBuilder()
    var currentCategory = this[i].category().onFailure { return Optional.Failure(it) }

    currentToken.append(this[i])
    i++
    while (i < length) {
        if (this[i] != ' ') {
            val category = this[i].category().onFailure { return Optional.Failure(it) }
            //bracket is always one character
            if (currentCategory != category || category == TokenCategory.BRACKET) {
                tokenList.add(currentToken.toString() to currentCategory)
                currentToken = StringBuilder()
                currentCategory = category
            }
            currentToken.append(this[i])
        }
        i++
    }

    tokenList.add(currentToken.toString() to currentCategory)

    return Optional.Success(tokenList)
}

//private val variableRegex = """[A-Za-z]""".toRegex()
//private val operatorRegex = """[<>=+\-*/]""".toRegex()

fun Char.category(): Optional<TokenCategory, IllegalTokenError> {
    return when {
        this in 'A'..'Z' || this in 'a'..'z' -> Optional.Success(TokenCategory.VARIABLE)
        this == '<' || this == '>' || this == '=' || this == '+' || this == '-' ||
                this == '*' || this == '/' || this == '&' || this == '|' || this == '!' -> Optional.Success(
            TokenCategory.OPERATOR
        )
        this in '0'..'9' -> Optional.Success(TokenCategory.CONSTANT)
        this == '(' || this == ')' -> Optional.Success(TokenCategory.BRACKET)
        else -> Optional.Failure(IllegalTokenError(this.toString()))
    }
}

fun mapOperations(tokens: List<Pair<String, TokenCategory>>): Optional<List<AtomicToken>, ParsError> {
    return tokens.mapOptional<Pair<String, TokenCategory>, AtomicToken, ParsError> {
        when (it.second) {
            TokenCategory.BRACKET -> {
                if (it.first == "(") Optional.Success<AtomicToken, ParsError>(Bracket.OPEN)
                else Optional.Success<AtomicToken, ParsError>(Bracket.CLOSE)
            }
            TokenCategory.CONSTANT -> {
                try {
                    Optional.Success<AtomicToken, ParsError>(ConstantInt(it.first.toInt()))
                } catch (e: NumberFormatException) {
                    Optional.Failure<AtomicToken, ParsError>(ConstantOverflowError(it.first))
                }
            }
            TokenCategory.VARIABLE -> {
                when {
                    it.first.equals("false", false) -> Optional.Success<AtomicToken, ParsError>(ConstFalse)
                    it.first.equals("true", true) -> Optional.Success<AtomicToken, ParsError>(ConstTrue)
                    else -> Optional.Success<AtomicToken, ParsError>(Variable(it.first))
                }
            }
            TokenCategory.OPERATOR -> {
                when (it.first) {
                    "==" -> Optional.Success<AtomicToken, ParsError>(Equal())
                    "!=" -> Optional.Success<AtomicToken, ParsError>(NotEqual())
                    "<" -> Optional.Success<AtomicToken, ParsError>(Smaller())
                    ">" -> Optional.Success<AtomicToken, ParsError>(Greater())
                    "<=" -> Optional.Success<AtomicToken, ParsError>(SmallerEqual())
                    ">=" -> Optional.Success<AtomicToken, ParsError>(GreaterEqual())
                    "&&" -> Optional.Success<AtomicToken, ParsError>(And())
                    "||" -> Optional.Success<AtomicToken, ParsError>(Or())
                    "!" -> Optional.Success<AtomicToken, ParsError>(Not())
                    "+" -> Optional.Success<AtomicToken, ParsError>(Plus())
                    "-" -> Optional.Success<AtomicToken, ParsError>(Minus())
                    "*" -> Optional.Success<AtomicToken, ParsError>(Mult())
                    "/" -> Optional.Success<AtomicToken, ParsError>(Div())
                    else -> Optional.Failure<AtomicToken, ParsError>(UnknownOperationError(it.first))
                }
            }
        }
    }
}

fun List<AtomicToken>.expressionEndLeft(startIndex: Int): Optional<Int, IllegalBracketError> {
    var i = startIndex - 1
    var brackets = 0
    while (i >= 0) {
        if (this[i] == Bracket.CLOSE) brackets++
        else if (this[i] == Bracket.OPEN) brackets--
        if (brackets == 0) return Optional.Success(i)
        i--
    }
    return Optional.Failure(IllegalBracketError())
}

fun List<AtomicToken>.expressionEndRight(startIndex: Int): Optional<Int, IllegalBracketError> {
    var i = startIndex + 1
    var brackets = 0
    while (i < size) {
        if (this[i] == Bracket.CLOSE) brackets--
        else if (this[i] == Bracket.OPEN) brackets++
        if (brackets == 0) return Optional.Success(i)
        i++
    }
    return Optional.Failure(IllegalBracketError())
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

fun addBrackets(tokens: List<AtomicToken>): Optional<List<AtomicToken>, IllegalBracketError> {
    val mutable = tokens.toMutableList()

    var i = 0
    while (i < mutable.size) {
        val cur = mutable[i]
        if (cur is Not) {
            val endRight = mutable.expressionEndRight(i).onFailure { return Optional.Failure(it) }
            if (endRight != mutable.lastIndex && mutable[endRight + 1] != Bracket.CLOSE) {
                mutable.add(i, Bracket.OPEN)
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
            val endLeft = mutable.expressionEndLeft(i).onFailure { return Optional.Failure(it) }
            val endRight = mutable.expressionEndRight(i).onFailure { return Optional.Failure(it) }
            //one of the indexes is not at the edge and there isn't already the bracket
            if ((endLeft != 0 && mutable[endLeft - 1] != Bracket.OPEN) ||
                (endRight != mutable.lastIndex && mutable[endRight + 1] != Bracket.CLOSE)
            ) {
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
        if (cur is Plus || cur is Minus) {
            val endLeft = mutable.expressionEndLeft(i).onFailure { return Optional.Failure(it) }
            val endRight = mutable.expressionEndRight(i).onFailure { return Optional.Failure(it) }
            //one of the indexes is not at the edge and there isn't already the bracket
            if ((endLeft != 0 && mutable[endLeft - 1] != Bracket.OPEN) ||
                (endRight != mutable.lastIndex && mutable[endRight + 1] != Bracket.CLOSE)
            ) {
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
        if (cur is IntCompare || cur is BooleanOperation || cur is Equal || cur is NotEqual) {
            val endLeft = mutable.expressionEndLeft(i).onFailure { return Optional.Failure(it) }
            val endRight = mutable.expressionEndRight(i).onFailure { return Optional.Failure(it) }
            //one of the indexes is not at the edge and there isn't already the bracket
            if ((endLeft != 0 && mutable[endLeft - 1] != Bracket.OPEN) ||
                (endRight != mutable.lastIndex && mutable[endRight + 1] != Bracket.CLOSE)
            ) {
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
    return Optional.Success(mutable)
}

fun buildOperationTree(tokens: List<AtomicToken>): Optional<TreeNode<AtomicToken>, ParsError> {
    //only 1 token plus 2 Brackets
    if (tokens.size == 3) return Optional.Success(TreeNode.valueOf(tokens[1]))

    var current: TreeNode<AtomicToken> = TreeNode.valueOf(PLACEHOLDER_TOKEN)
    for (i in 1..tokens.lastIndex) {
        when (val cur = tokens[i]) {
            is Bracket.OPEN -> {
                val newNode: TreeNode<AtomicToken> = TreeNode.valueOf(PLACEHOLDER_TOKEN)
                when {
                    current.left == null -> current.insertLeft(newNode)
                    current.right == null -> current.insertRight(newNode)
                    else -> return Optional.Failure(IllegalBracketError())
                }
                current = newNode
            }
            is Bracket.CLOSE -> {
                if (i != tokens.lastIndex) current = current.parent.ifNull { return Optional.Failure(IllegalBracketError()) }
            }
            is TokenOperation<*> -> {
                if (current.value != PLACEHOLDER_TOKEN) {
                    return Optional.Failure(UnexpectedOperatorError(cur))
                }
                current.value = cur
            }
            is Value<*> -> when {
                current.left == null -> current.insertLeft(TreeNode.valueOf(cur))
                current.right == null -> current.insertRight(TreeNode.valueOf(cur))
                else -> return Optional.Failure(UnexpectedValueError(cur))
            }
        }
    }
    return Optional.Success(current)
}

fun linkOperations(
    operationTreeNode: TreeNode<AtomicToken>
): Optional<AtomicToken, UnexpectedTypeError> {
    for (node in operationTreeNode.nodeInOrderIterator()) {
        val value = node.value
        if (value is UnaryOperation<*, *>) {
            try {
                value.forceOperand1(node.left?.value)
            } catch (e: Exception) {
                return Optional.Failure(UnexpectedTypeError(node.left?.value.type, value.expectedOperand1))
            }
            if (value is BinaryOperation<*, *, *>) {
                try {
                    value.forceOperand2(node.right!!.value)
                } catch (e: Exception) {
                    return Optional.Failure(UnexpectedTypeError(node.left?.value.type, value.expectedOperand2))
                }
            }
        }
    }
    return Optional.Success(operationTreeNode.value)
    //operationTreeNode.value.attachRuntime(runtime)
    //@Suppress("UNCHECKED_CAST")
    //return try {
    //    Optional.Success(operationTreeNode.value as T)
    //} catch (e: ClassCastException) {
    //    Optional.Failure(UnexpectedTypeError(operationTreeNode.value.type, ))
    //}
}

fun buildArithmeticOperation(
    operationTreeNode: TreeNode<AtomicToken>,
    runtime: Map<String, Int>? = null
): Optional<ArithmeticOperation, UnexpectedTypeError> {
    return try {
        linkOperations((operationTreeNode)).withSuccess {
            attachRuntime(runtime)
            this as ArithmeticOperation
        }
    } catch (e: ClassCastException) {
        Optional.Failure(UnexpectedTypeError(operationTreeNode.value.type, Type.INT))
    }
}

fun buildLogicOperation(
    operationTreeNode: TreeNode<AtomicToken>,
    runtime: Map<String, Int>? = null
): Optional<LogicOperation, UnexpectedTypeError> {
    return try {
        linkOperations((operationTreeNode)).withSuccess {
            attachRuntime(runtime)
            this as LogicOperation
        }
    } catch (e: ClassCastException) {
        Optional.Failure(UnexpectedTypeError(operationTreeNode.value.type, Type.BOOLEAN))
    }
}

/*fun TreeNode<AtomicToken>.requireParent() = parent ?: throw IllegalStateException("could not return to parent")

fun TreeNode<AtomicToken>.requirePlaceholder(neededFor: AtomicToken) {
    if (value != PLACEHOLDER_TOKEN) {
        throw IllegalTokenException("PlaceHolder expected for $neededFor")
    }
}*/

open class IllegalTokenException(msg: String) : Exception(msg)

class IllegalBracketException(msg: String = "Bad Brackets") : IllegalTokenException(msg)