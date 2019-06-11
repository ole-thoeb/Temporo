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

enum class Type { BOOLEAN, INT, ANY, NULL }

val AtomicToken?.type: Type
        get() = this?.type ?: Type.NULL

sealed class AtomicToken {
    open fun attachRuntime(v: Map<String, Int>?) {
        variables = v
    }

    var variables: Map<String, Int>? = null
        private set(value) {
            field = value
        }

    abstract val type: Type
}

sealed class Bracket: AtomicToken() {

    override val type: Type = Type.BOOLEAN

    object OPEN: Bracket() {
        override fun toString(): String = "("
    }
    object CLOSE: Bracket() {
        override fun toString(): String = ")"
    }
}

@Suppress("ClassName")
object PLACEHOLDER_TOKEN: AtomicToken() {
    override val type: Type = Type.ANY
}

sealed class Value<out T>: AtomicToken() {
    abstract val value: T
}

sealed class BooleanValue: Value<Boolean>() {
    override val type: Type = Type.BOOLEAN
}

sealed class IntValue: Value<Int>() {
    override val type: Type = Type.INT
}

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
        get() = requireVariables().getValue(vName)

    override fun execute(): Int = value

    override fun toString(): String = vName
}

sealed class TokenOperation<out R>: AtomicToken(), Operation<R>

sealed class UnaryOperation<T, out R>: TokenOperation<R>() {
    open var operand1: Operation<T>? = null
    abstract val expectedOperand1: Type

    override fun attachRuntime(v: Map<String, Int>?) {
        super.attachRuntime(v)
        (operand1 as? AtomicToken)?.attachRuntime(v)
    }

    @Suppress("UNCHECKED_CAST")
    open fun forceOperand1(operand: Any?) {
        operand1 = operand as Operation<T>
    }
}

class Not: UnaryOperation<Boolean, Boolean>(), LogicOperation {
    override fun execute(): Boolean = !requireOperand1().execute()

    override fun toString(): String = "!"

    override val type: Type = Type.BOOLEAN
    override val expectedOperand1: Type = Type.BOOLEAN
}

sealed class BinaryOperation<T1, T2, out R>: UnaryOperation<T1, R>() {
    open var operand2: Operation<T2>? = null
    abstract val expectedOperand2: Type

    override fun attachRuntime(v: Map<String, Int>?) {
        super.attachRuntime(v)
        (operand2 as? AtomicToken)?.attachRuntime(v)
    }

    @Suppress("UNCHECKED_CAST")
    open fun forceOperand2(operand: Any?) {
        operand2 = operand as Operation<T2>
    }
}

sealed class IntCompare: BinaryOperation<Int, Int, Boolean>(), LogicOperation {
    override val type: Type = Type.BOOLEAN
    override val expectedOperand1: Type = Type.INT
    override val expectedOperand2: Type = Type.INT
}

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

sealed class AnyCompare: BinaryOperation<Any, Any, Boolean>(), LogicOperation {
    override val type: Type = Type.BOOLEAN
    override val expectedOperand1: Type = Type.BOOLEAN
    override val expectedOperand2: Type = Type.BOOLEAN
}

class Equal: AnyCompare() {
    override fun execute(): Boolean = requireOperand1().execute() == requireOperand2().execute()

    override fun toString(): String = "=="
}

class NotEqual: AnyCompare() {
    override fun execute(): Boolean = requireOperand1().execute() != requireOperand2().execute()


    override fun toString(): String = "!="
}

sealed class BooleanOperation: BinaryOperation<Boolean, Boolean, Boolean>() {
    override val type: Type = Type.BOOLEAN
    override val expectedOperand1: Type = Type.BOOLEAN
    override val expectedOperand2: Type = Type.BOOLEAN
}

class And: BooleanOperation(), LogicOperation {
    override fun execute(): Boolean = requireOperand1().execute() && requireOperand2().execute()

    override fun toString(): String = "&&"
}

class Or: BooleanOperation(), LogicOperation {
    override fun execute(): Boolean = requireOperand1().execute() || requireOperand2().execute()

    override fun toString(): String = "||"
}

sealed class IntOperation: BinaryOperation<Int, Int, Int>(), ArithmeticOperation {
    override val type: Type = Type.INT
    override val expectedOperand1: Type = Type.INT
    override val expectedOperand2: Type = Type.INT
}

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
