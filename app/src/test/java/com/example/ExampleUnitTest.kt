package com.example

import com.example.riverpod.DialectFilterNotifier
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDialectFilterNotifier_initialStateAndStateUpdates() {
    val notifier = DialectFilterNotifier()
    // Initial dialect should be "All"
    assertEquals("All", notifier.state)

    // Update dialect to Sanskrit
    notifier.setDialect("Sanskrit")
    assertEquals("Sanskrit", notifier.state)

    // Update dialect to Bhojpuri
    notifier.setDialect("Bhojpuri")
    assertEquals("Bhojpuri", notifier.state)
  }
}
