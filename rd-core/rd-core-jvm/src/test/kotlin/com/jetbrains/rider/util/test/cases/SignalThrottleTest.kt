package com.jetbrains.rider.util.test.cases

import com.jetbrains.rider.util.lifetime.Lifetime
import com.jetbrains.rider.util.reactive.*
import com.jetbrains.rider.util.threading.SynchronousScheduler
import com.jetbrains.rider.util.throttleLast
import org.junit.Test
import java.time.Duration

class SignalThrottleTest {
    @Test
    fun testThrottle() {
        val orig = Signal<Int>()
        val throttled = orig.throttleLast(Duration.ofMillis(200), SynchronousScheduler)

        var value = 0;
        Lifetime.using { lt ->
            throttled.advise(lt) {
                println(it)
            }

            repeat(40) {
                orig.fire(value++)
                Thread.sleep(20)
            }
        }
    }
}
