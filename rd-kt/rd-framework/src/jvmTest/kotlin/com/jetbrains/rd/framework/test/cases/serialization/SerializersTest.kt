package com.jetbrains.rd.framework.test.cases.serialization

import com.jetbrains.rd.framework.UnsafeBuffer
import com.jetbrains.rd.framework.readArray
import com.jetbrains.rd.framework.writeArray
import org.junit.Test
import kotlin.test.assertTrue

class SerializersTest {



    @Test
    fun testReadArray() {
        val buffer = UnsafeBuffer(ByteArray(10))
        buffer.writeArray(Array(1) {"abc"}) {}
        buffer.rewind()

        assertTrue (arrayOf("abc") contentEquals buffer.readArray { "abc" })
    }

}