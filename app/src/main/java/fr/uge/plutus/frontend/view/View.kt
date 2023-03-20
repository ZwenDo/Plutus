package fr.uge.plutus.frontend.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.book.BookCreationView
import fr.uge.plutus.frontend.view.book.BookOverview
import fr.uge.plutus.frontend.view.book.BookSelectionView
import fr.uge.plutus.frontend.view.transaction.DisplayHeader
import fr.uge.plutus.frontend.view.transaction.DisplayTransactionDetails
import fr.uge.plutus.frontend.view.transaction.DisplayTransactions
import fr.uge.plutus.frontend.view.transaction.TransactionCreationView

enum class View(
    val headerComponent: @Composable () -> Unit,
    val contentComponent: @Composable (PaddingValues) -> Unit,
    val fabComponent: @Composable (() -> Unit) = {}
) {
    @RequiresApi(Build.VERSION_CODES.O)
    BOOK_SELECTION(
        headerComponent = {
            TopAppBar(title = { Text("Books") })
        },
        contentComponent = { BookSelectionView() },
        fabComponent = {
            val globalState = globalState()
            FloatingActionButton(onClick = {
                globalState.currentView = BOOK_CREATION
            }) {
                Icon(
                    Icons.Default.Add,
                    "New book"
                )
            }
        }
    ),

    @RequiresApi(Build.VERSION_CODES.O)
    BOOK_CREATION(
        headerComponent = {
            TopAppBar(title = { Text("New book") })
        },
        contentComponent = { BookCreationView() }
    ),

    BOOK_OVERVIEW(
        headerComponent = {
            val globalState = globalState()
            TopAppBar(title = { Text("Overview: ${globalState.currentBook!!.name}") })
        },
        contentComponent = { BookOverview() }
    ),

    @RequiresApi(Build.VERSION_CODES.O)
    TRANSACTION_CREATION(
        headerComponent = {
            TopAppBar(title = { Text("New transaction") })
        },
        contentComponent = { TransactionCreationView() }
    ),

    @RequiresApi(Build.VERSION_CODES.O)
    TRANSACTION_LIST(
        headerComponent = {
            val globalState = globalState()
            TopAppBar(title = { Text("Transactions: ${globalState.currentBook!!.name}") })
        },
        contentComponent = { DisplayTransactions() },
        fabComponent = {
            val globalState = globalState()
            FloatingActionButton(onClick = {
                globalState.currentView = TRANSACTION_CREATION
            }) {
                Icon(
                    Icons.Default.Add,
                    "New transaction"
                )
            }
        }
    ),

    @RequiresApi(Build.VERSION_CODES.O)
    TRANSACTION_DETAILS(
        headerComponent = {
            val globalState = globalState()
            Column {
                TopAppBar(title = { Text("Details") })
                DisplayHeader(globalState.currentTransaction!!) {
                    globalState.currentView = TRANSACTION_LIST
                    globalState.currentTransaction = null
                }
            }
        },
        contentComponent = {
            val globalState = globalState()
            DisplayTransactionDetails(globalState.currentTransaction!!)
        }
    )
}
