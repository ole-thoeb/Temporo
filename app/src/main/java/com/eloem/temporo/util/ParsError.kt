package com.eloem.temporo.util

sealed class ParsError

data class IllegalTokenError(val token: String): ParsError()
data class UnknownOperationError(val operation: String): ParsError()
data class UnexpectedOperatorError<T>(val operator: TokenOperation<T>): ParsError()
data class UnexpectedValueError<T>(val value: Value<T>): ParsError()
data class ConstantOverflowError(val stringValue: String): ParsError()
data class UnexpectedTypeError(val found: Type, val expected: Type): ParsError()
class IllegalBracketError: ParsError() {
    override fun equals(other: Any?): Boolean = other is IllegalBracketError
    override fun hashCode(): Int = 1
}