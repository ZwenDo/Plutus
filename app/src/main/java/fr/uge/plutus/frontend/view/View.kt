package fr.uge.plutus.frontend.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import fr.uge.plutus.R
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.book.BookCreationView
import fr.uge.plutus.frontend.view.book.BookOverviewLoader
import fr.uge.plutus.frontend.view.book.BookSelectionView
import fr.uge.plutus.frontend.view.search.*
import fr.uge.plutus.frontend.view.transaction.*
import kotlinx.coroutines.launch

enum class View(
    val headerComponent: @Composable () -> Unit,
    val contentComponent: @Composable (PaddingValues) -> Unit,
    val fabComponent: @Composable (() -> Unit) = {},
    val drawerComponent: @Composable (ColumnScope.() -> Unit)? = null
) {

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
        contentComponent = { BookOverviewLoader() }
    ),

    TRANSACTION_CREATION(
        headerComponent = {
            TopAppBar(title = { Text("New transaction") })
        },
        contentComponent = { TransactionCreationView() }
    ),

    TRANSACTION_EDIT(
        headerComponent = {
            TopAppBar(title = { Text("Edit transaction") })
        },
        contentComponent = { TransactionCreationView() }
    ),

    TRANSACTION_LIST(
        headerComponent = {
            val globalState = globalState()
            val coroutineScope = rememberCoroutineScope()
            TopAppBar(
                title = { Text("Transactions: ${globalState.currentBook!!.name}") },
                actions = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            globalState.scaffoldState.drawerState.open()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            "Search"
                        )
                    }
                }
            )
        },
        contentComponent = { TransactionSearchView() },
        drawerComponent = { SearchFiltersView() },
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

    TRANSACTION_DETAILS(
        headerComponent = {
            val globalState = globalState()
            val currentTransaction = globalState.currentTransaction
            Column {
                TopAppBar(
                    title = { Text("Transaction details") },
                    navigationIcon = {
                        IconButton(onClick = {
                            globalState.currentView = TRANSACTION_LIST
                            globalState.currentTransaction = null
                        }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    })
                if (currentTransaction != null) {
                    TransactionHeader(currentTransaction)
                }
            }
        },
        contentComponent = {
            val globalState = globalState()
            TransactionDetails(globalState.currentTransaction!!)
        },
        fabComponent = {
            val globalState = globalState()
            FloatingActionButton(onClick = {
                globalState.currentView = TRANSACTION_EDIT
            }) {
                Icon(
                    Icons.Default.Edit,
                    "Edit"
                )
            }
        }
    )
}
