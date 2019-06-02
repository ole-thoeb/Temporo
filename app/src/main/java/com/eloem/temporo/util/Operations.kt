package com.eloem.temporo.util

interface Operation<out T> {
    fun execute(): T
}

interface ArithmeticOperation : Operation<Int> {
    override fun execute(): Int
}

interface LogicOperation: Operation<Boolean> {
    override fun execute(): Boolean
}

sealed class AtomicToken {
    open fun attachRuntime(v: Map<String, Int>?) {
        variables = v
    }

    var variables: Map<String, Int>? = null
        private set(value) {
            field = value
        }
}

sealed class Bracket: AtomicToken() {

    object OPEN: Bracket() {
        override fun toString(): String = "("
    }
    object CLOSE: Bracket() {
        override fun toString(): String = ")"
    }
}

@Suppress("ClassName")
object PLACEHOLDER_TOKEN: AtomicToken()

sealed class Value<T>: AtomicToken() {
    abstract val value: T
}

sealed class BooleanValue: Value<Boolean>()

sealed class IntValue: Value<Int>()

object ConstFalse: BooleanValue(), LogicOperation {
    override val value: Boolean = false

    override fun execute(): Boolean = value

    override fun toString(): String = "false"
}

object ConstTrue: BooleanValue(), LogicOperation {
    override val value: Boolean = false

    override fun execute(): Boolean = value

    override fun toString(): String = "true"
}

class ConstantInt(override val value: Int): IntValue(), ArithmeticOperation {
    override fun execute(): Int = value

    override fun toString(): String = value.toString()
}

class Variable(private val vName: String): IntValue(), ArithmeticOperation {

    override val value: Int
        get() = requireVariables()[vName]!!

    override fun execute(): Int = value

    override fun toString(): String = vName
}

sealed class TokenOperation<out R>: AtomicToken(), Operation<R>

sealed class UnaryOperation<T, out R>: TokenOperation<R>() {
    open var operand1: Operation<T>? = null

    override fun attachRuntime(v: Map<String, Int>?) {
        super.attachRuntime(v)
        (operand1 as? AtomicToken)?.attachRuntime(v)
    }
}

class Not: UnaryOperation<Boolean, Boolean>(), LogicOperation {
    override fun execute(): Boolean = !requireOperand1().execute()

    override fun toString(): String = "!"
}

sealed class BinaryOperation<T1, T2, out R>: UnaryOperation<T1, R>() {
    open var operand2: Operation<T2>? = null

    override fun attachRuntime(v: Map<String, Int>?) {
        super.attachRuntime(v)
        (operand2 as? AtomicToken)?.attachRuntime(v)
    }
}

sealed class IntCompare: BinaryOperation<Int, Int, Boolean>(), LogicOperation

class Smaller: IntCompare() {
    override fun execute(): Boolean = requireOperand1().execute() < requireOperand2().execute()

    override fun toString(): String = "<"
}

class Greater: IntCompare() {
    override fun execute(): Boolean = requireOperand1().execute() > requireOperand2().execute()

    override fun toString(): String = ">"
}

class SmallerEqual: IntCompare() {
    override fun execute(): Boolean = requireOperand1().execute() <= requireOperand2().execute()

    override fun toString(): String = "<="
}

class GreaterEqual: IntCompare() {
    override fun execute(): Boolean = requireOperand1().execute() >= requireOperand2().execute()

    override fun toString(): String = ">="
}

class Equal<T1, T2>: BinaryOperation<T1, T2, Boolean>(), LogicOperation {
    override fun execute(): Boolean = requireOperand1().execute() == requireOperand2().execute()

    override fun toString(): String = "=="
}

class NotEqual<T1, T2>: BinaryOperation<T1, T2, Boolean>(), LogicOperation {
    override fun execute(): Boolean = requireOperand1().execute() != requireOperand2().execute()


    override fun toString(): String = "!="
}

class And: BinaryOperation<Boolean, Boolean, Boolean>(), LogicOperation {
    override fun execute(): Boolean = requireOperand1().execute() && requireOperand2().execute()

    override fun toString(): String = "&&"
}

class Or: BinaryOperation<Boolean, Boolean, Boolean>(), LogicOperation {
    override fun execute(): Boolean = requireOperand1().execute() || requireOperand2().execute()

    override fun toString(): String = "||"
}

sealed class IntOperation: BinaryOperation<Int, Int, Int>(), ArithmeticOperation

class Plus: IntOperation() {
    override fun execute(): Int = requireOperand1().execute() + requireOperand2().execute()

    override fun toString(): String = "+"
}

class Minus: IntOperation() {
    override fun execute(): Int = requireOperand1().execute() - requireOperand2().execute()

    override fun toString(): String = "-"
}

class Mult: IntOperation() {
    override fun execute(): Int = requireOperand1().execute() * requireOperand2().execute()

    override fun toString(): String = "*"
}

class Div: IntOperation() {
    override fun execute(): Int = requireOperand1().execute() / requireOperand2().execute()

    override fun toString(): String = "/"
}

fun AtomicToken.requireVariables(): Map<String, Int> = variables ?: throw IllegalStateException("No runtime attached")

fun <T, R>UnaryOperation<T, R>.requireOperand1(): Operation<T> = operand1 ?: throw IllegalStateException("No operand1 attached")

fun <T1, T2, R>BinaryOperation<T1, T2, R>.requireOperand2(): Operation<T2> = operand2 ?: throw IllegalStateException("No operand2 attached")
