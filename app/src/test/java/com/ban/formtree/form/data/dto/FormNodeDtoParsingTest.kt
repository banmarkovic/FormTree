package com.ban.formtree.form.data.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormNodeDtoParsingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parses pages with nested sections and questions`() {
        val result = json.decodeFromString<List<FormNodeDto>>(FORM_JSON)

        assertEquals(2, result.size)
        val page = result.first() as FormNodeDto.PageDto
        assertEquals(1L, page.id)
        assertEquals("Main Page", page.title)

        val section = page.items.first() as FormNodeDto.SectionDto
        assertEquals("Introduction", section.title)

        val text = section.items[0] as FormNodeDto.TextDto
        assertEquals("Welcome to the main page!", text.content)

        val image = section.items[1] as FormNodeDto.ImageDto
        assertEquals("https://example.com/android.png", image.src)
        assertEquals("Welcome Image", image.title)

        val nestedSection = (page.items[1] as FormNodeDto.SectionDto).items[1] as FormNodeDto.SectionDto
        assertEquals("Subsection 1.1", nestedSection.title)
        assertEquals(1, nestedSection.items.size)
    }

    @Test
    fun `parses choice questions with response sets`() {
        val result = json.decodeFromString<List<FormNodeDto>>(FORM_JSON)

        val chapter = (result[1] as FormNodeDto.PageDto).items.first() as FormNodeDto.SectionDto
        val singleChoice = chapter.items[0] as FormNodeDto.ChoiceDto
        assertEquals("What is the main topic?", singleChoice.content)
        assertEquals(false, singleChoice.responseSet.multipleSelection)
        assertEquals(listOf(1011L, 1012L, 1013L), singleChoice.responseSet.responses.map { it.id })
        assertEquals(1, singleChoice.responseSet.responses[0].score)
        assertNull(singleChoice.responseSet.responses[2].score)

        val multiChoice = chapter.items[1] as FormNodeDto.ChoiceDto
        assertTrue(multiChoice.responseSet.multipleSelection)
        assertTrue(multiChoice.responseSet.responses.all { it.score == null })
    }

    @Test
    fun `ignores unknown json fields`() {
        val result = json.decodeFromString<List<FormNodeDto>>(
            """[{"id": 1, "type": "page", "title": "Page", "items": [], "unknown_field": 42}]""",
        )

        assertEquals(1L, (result.single() as FormNodeDto.PageDto).id)
    }

    private companion object {
        val FORM_JSON = """
            [
              {
                "id": 1,
                "type": "page",
                "title": "Main Page",
                "items": [
                  {
                    "id": 2,
                    "type": "section",
                    "title": "Introduction",
                    "items": [
                      {"id": 3, "type": "text", "content": "Welcome to the main page!"},
                      {"id": 4, "type": "image", "src": "https://example.com/android.png", "title": "Welcome Image"}
                    ]
                  },
                  {
                    "id": 5,
                    "type": "section",
                    "title": "Chapter 1",
                    "items": [
                      {"id": 6, "type": "text", "content": "This is the first chapter."},
                      {
                        "id": 7,
                        "type": "section",
                        "title": "Subsection 1.1",
                        "items": [
                          {"id": 8, "type": "text", "content": "This is a subsection."}
                        ]
                      }
                    ]
                  }
                ]
              },
              {
                "id": 10,
                "type": "page",
                "title": "Second Page",
                "items": [
                  {
                    "id": 11,
                    "type": "section",
                    "title": "Chapter 2",
                    "items": [
                      {
                        "id": 13,
                        "type": "choice",
                        "content": "What is the main topic?",
                        "response_set": {
                          "id": 101,
                          "multiple_selection": false,
                          "responses": [
                            {"id": 1011, "label": "Safety procedures", "score": 1},
                            {"id": 1012, "label": "Equipment checks", "score": 2},
                            {"id": 1013, "label": "Not applicable", "score": null}
                          ]
                        }
                      },
                      {
                        "id": 14,
                        "type": "choice",
                        "content": "Which areas were inspected?",
                        "response_set": {
                          "id": 102,
                          "multiple_selection": true,
                          "responses": [
                            {"id": 1021, "label": "Entrance"},
                            {"id": 1022, "label": "Storage"},
                            {"id": 1023, "label": "Loading bay"}
                          ]
                        }
                      }
                    ]
                  }
                ]
              }
            ]
        """.trimIndent()
    }
}
