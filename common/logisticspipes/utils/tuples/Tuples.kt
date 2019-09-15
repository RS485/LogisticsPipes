package logisticspipes.utils.tuples

import java.util.function.Consumer

data class Tuple2<T1, T2>(
        var value1: T1,
        var value2: T2
) {
    fun <R> map1(op: (T1) -> R) = Tuple2(op(value1), value2)
    fun <R> map2(op: (T2) -> R) = Tuple2(value1, op(value2))

    fun with1(op: Consumer<T1>): Tuple2<T1, T2> {
        op.accept(value1)
        return this
    }

    fun with2(op: Consumer<T2>): Tuple2<T1, T2> {
        op.accept(value2)
        return this
    }

}

data class Tuple3<T1, T2, T3>(
        var value1: T1,
        var value2: T2,
        var value3: T3
) {
    fun <R> map1(op: (T1) -> R) = Tuple3(op(value1), value2, value3)
    fun <R> map2(op: (T2) -> R) = Tuple3(value1, op(value2), value3)
    fun <R> map3(op: (T3) -> R) = Tuple3(value1, value2, op(value3))

    fun with1(op: Consumer<T1>): Tuple3<T1, T2, T3> {
        op.accept(value1)
        return this
    }

    fun with2(op: Consumer<T2>): Tuple3<T1, T2, T3> {
        op.accept(value2)
        return this
    }

    fun with3(op: Consumer<T3>): Tuple3<T1, T2, T3> {
        op.accept(value3)
        return this
    }

}

data class Tuple4<T1, T2, T3, T4>(
        var value1: T1,
        var value2: T2,
        var value3: T3,
        var value4: T4
) {
    fun <R> map1(op: (T1) -> R) = Tuple4(op(value1), value2, value3, value4)
    fun <R> map2(op: (T2) -> R) = Tuple4(value1, op(value2), value3, value4)
    fun <R> map3(op: (T3) -> R) = Tuple4(value1, value2, op(value3), value4)
    fun <R> map4(op: (T4) -> R) = Tuple4(value1, value2, value3, op(value4))

    fun with1(op: Consumer<T1>): Tuple4<T1, T2, T3, T4> {
        op.accept(value1)
        return this
    }

    fun with2(op: Consumer<T2>): Tuple4<T1, T2, T3, T4> {
        op.accept(value2)
        return this
    }

    fun with3(op: Consumer<T3>): Tuple4<T1, T2, T3, T4> {
        op.accept(value3)
        return this
    }

    fun with4(op: Consumer<T4>): Tuple4<T1, T2, T3, T4> {
        op.accept(value4)
        return this
    }

}