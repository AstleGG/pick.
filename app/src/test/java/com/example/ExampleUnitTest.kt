package com.example

import com.example.data.Pick
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
  fun testPickEntityDefaultValues() {
    val pick = Pick(
      id = 1L,
      title = "What should I eat?",
      options = listOf("Pizza", "Burger", "Sushi")
    )
    
    assertEquals(1L, pick.id)
    assertEquals("What should I eat?", pick.title)
    assertEquals(listOf("Pizza", "Burger", "Sushi"), pick.options)
    assertFalse("By default, a pick should not be a favourite", pick.isFavourite)
  }

  @Test
  fun testPickEntityFavouriteStatusModification() {
    val pick = Pick(
      id = 1L,
      title = "What should I eat?",
      options = listOf("Pizza", "Burger", "Sushi")
    )
    
    val favouritedPick = pick.copy(isFavourite = true)
    
    assertTrue("Updating favourite status should persist in copy", favouritedPick.isFavourite)
  }
}
