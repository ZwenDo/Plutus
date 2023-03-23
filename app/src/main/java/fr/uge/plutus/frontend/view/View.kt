package fr.uge.plutus.frontend.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import fr.uge.plutus.R
import fr.uge.plutus.frontend.store.globalState
import fr.uge.plutus.frontend.view.book.*
import fr.uge.plutus.frontend.view.search.SearchFiltersView
import fr.uge.plutus.frontend.view.transaction.TransactionCreationView
import fr.uge.plutus.frontend.view.transaction.TransactionDetails
import fr.uge.plutus.frontend.view.transaction.TransactionHeader
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
            var showMenu by remember { mutableStateOf(false) }

            TopAppBar(
                title = {
                    Text(
                        "Transactions: ${globalState.currentBook!!.name}",
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                actions = {
                    IconButton(onClick = {
                        globalState.displaySorting = true
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.sort),
                            "Sort"
                        )
                    }
                    IconButton(onClick = {
                        coroutineScope.launch {
                            globalState.scaffoldState.drawerState.open()
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            "Filters"
                        )
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            globalState.importExportState = ImportExportState.IMPORT
                        }) {
                            Text("Import")
                        }
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            globalState.importExportState = ImportExportState.EXPORT
                        }) {
                            Text("Export")
                        }
                        DropdownMenuItem(onClick = {
                            showMenu = false
                            globalState.deletingBook = true
                        }) {
                            Text("Delete book")
                        }
                    }
                }
            )
        },
        contentComponent = { BookTransactionsListView() },
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

            var showMenu by androidx.compose.runtime.remember { mutableStateOf(false) }

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
                    },
                    actions = {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(onClick = {
                                showMenu = false
                                globalState.deletingTransaction = true
                            }) {
                                Text("Delete transaction")
                            }
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
