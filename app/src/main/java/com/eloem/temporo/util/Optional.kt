package com.eloem.temporo.util

import java.lang.RuntimeException
import java.util.ArrayList

sealed class Optional<T, E: Any> {
    abstract fun orElse(other: T): T
    abstract fun orElse(other: (E) -> T): T
    abstract fun <S>withSuccess(action: T.() -> S): Optional<S, E>
    abstract fun <S>chainSuccess(action: T.() -> Optional<S, out Any>): Optional<S, out Any>
    abstract fun throwError(): T
    abstract fun ifFailure(action: (E) -> Unit): Optional<T, E>

    data class Success<T, E: Any>(val value: T): Optional<T, E>() {
        override fun orElse(other: T) = value
        override fun orElse(other: (E) -> T) = value
        override fun <S> withSuccess(action: T.() -> S): Success<S, E> = Success(action(value))
        override fun <S> chainSuccess(action: T.() -> Optional<S, out Any>): Optional<S, out Any> = action(value)
        override fun throwError(): T = value
        override fun ifFailure(action: (E) -> Unit): Success<T, E> = this
    }

    data class Failure<T, E: Any>(val error: E): Optional<T, E>() {
        override fun orElse(other: T): T = other
        override fun orElse(other: (E) -> T): T = other(error)
        override fun <S> withSuccess(action: T.() -> S): Failure<S, E> = Failure(error)
        //override fun <S, O : Optional<S, E>> chainSuccess(action: T.() -> O): O = Failure(error)
        override fun throwError() = throw RuntimeException(error.toString())
        override fun <S> chainSuccess(action: T.() -> Optional<S, out Any>): Failure<S, out Any> = Failure(error)
        override fun ifFailure(action: (E) -> Unit): Failure<T, E> {
            action(error)
            return this
        }
    }
}

inline fun <T, E: Any>Optional<T, E>.onFailure(action: (E) -> T): T = when (this) {
    is Optional.Failure -> action(error)
    is Optional.Success -> value
}

inline fun <T> T?.ifNull(producer: () -> T): T {
    return this ?: producer()
}

inline fun <T, R, E: Any>List<T>.mapOptional(transform: (T) -> Optional<R, E>): Optional<List<R>, E> {
    val destination = ArrayList<R>(size)
    for (item in this) {
        when(val result = transform(item)) {
            is Optional.Failure -> return Optional.Failure(result.error)
            is Optional.Success -> destination.add(result.value)
        }.exhaustive()
    }
    return Optional.Success(destination)
}

fun <T> T.exhaustive() = this