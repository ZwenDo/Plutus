package fr.uge.plutus.frontend.components.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.uge.plutus.backend.Book
import fr.uge.plutus.backend.Tag
import fr.uge.plutus.backend.TagType

@Composable
fun DisplayTag(
    caption: String,
    clickHandler: () -> Unit,
    color: Color = MaterialTheme.colors.background
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        Modifier
            .clip(shape = shape)
            .border(width = 1.dp, color = Color.Black, shape = shape)
            .clickable { clickHandler() }
            .background(color = color)
    ) {
        Text(
            text = caption,
            modifier = Modifier.padding(5.dp),
            fontSize = 20.sp
        )
    }
}

fun getTagFromString(name: String, book: Book): Tag {
    val (type, n) = TagType.tagFromString(name)
    return Tag(n, type, book.uuid)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    val book = Book("Android For Dummies")

    val t1 = getTagFromString("+Quotidiens", book)
    val t2 = getTagFromString("-sorties", book)
    val t3 = getTagFromString("anniversaries", book)
    val t4 = getTagFromString("repas", book)
    val t5 = getTagFromString("animaux", book)

    val tags = listOf(t1, t2, t3, t4, t5)
    DisplayTags(tags = tags)
}