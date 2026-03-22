package org.kristof.hbviewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

private val GrayYellowScheme = darkColorScheme(
    primary = Color(0xFFF2C94C),
    onPrimary = Color(0xFF1F1F1F),
    secondary = Color(0xFFE1B12C),
    background = Color(0xFF2D2F33),
    surface = Color(0xFF3A3D42),
    surfaceVariant = Color(0xFF4A4E55),
    onBackground = Color(0xFFF3F3F0),
    onSurface = Color(0xFFF3F3F0),
    onSurfaceVariant = Color(0xFFD3D5D8)
)

@Composable
fun ViewerApp(
    controller: ViewerController,
    onOpenFile: () -> Unit
) {
    val state by controller.state.collectAsState()

    MaterialTheme(colorScheme = GrayYellowScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val sidebarWidth = 280.dp
                val gutter = 16.dp
                val mainWidth = if (maxWidth > sidebarWidth + gutter) maxWidth - sidebarWidth - gutter else maxWidth

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(gutter)
                ) {
                Sidebar(
                    state = state,
                    onOpenFile = onOpenFile,
                    onSelectAllAccounts = controller::selectAllAccounts,
                    onSelectAccount = controller::selectAccount,
                    modifier = Modifier.width(sidebarWidth).fillMaxHeight()
                )
                    MainPane(
                        state = state,
                        onSearch = controller::search,
                        onSelectKind = controller::setTransactionKindFilter,
                        onMinimumAmountChange = controller::setMinimumAmount,
                        onMaximumAmountChange = controller::setMaximumAmount,
                        onSelectTransaction = controller::selectTransaction,
                        modifier = Modifier.width(mainWidth).fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun Sidebar(
    state: ViewerSnapshot,
    onOpenFile: () -> Unit,
    onSelectAllAccounts: () -> Unit,
    onSelectAccount: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("HBViewer", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Button(onClick = onOpenFile) {
                Text("Open HomeBank file")
            }
            state.importSummary?.let {
                MetaLine("Source", it.sourceName)
                MetaLine("Imported", it.importedAt)
                MetaLine("Counts", "${it.accountCount} accounts / ${it.transactionCount} transactions")
            } ?: Text("No file imported yet", color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(4.dp))
            Text("Accounts", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (state.selectedAccountId == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(14.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (state.selectedAccountId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectAllAccounts() }
                    .padding(12.dp)
            ) {
                Column {
                    Text("All accounts", fontWeight = FontWeight.SemiBold)
                    Text("Search across every transaction", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.accounts) { account ->
                    val selected = account.id == state.selectedAccountId
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectAccount(account.id) }
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(account.name, fontWeight = FontWeight.SemiBold)
                            Text("${account.transactionCount} transactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainPane(
    state: ViewerSnapshot,
    onSearch: (String) -> Unit,
    onSelectKind: (TransactionKindFilter) -> Unit,
    onMinimumAmountChange: (String) -> Unit,
    onMaximumAmountChange: (String) -> Unit,
    onSelectTransaction: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Search and filters", style = MaterialTheme.typography.titleMedium)
                FilterTextField(
                    value = state.searchQuery,
                    onValueChange = onSearch,
                    label = "Free text"
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip("All", state.transactionKindFilter == TransactionKindFilter.ALL) {
                        onSelectKind(TransactionKindFilter.ALL)
                    }
                    FilterChip("Income", state.transactionKindFilter == TransactionKindFilter.INCOME) {
                        onSelectKind(TransactionKindFilter.INCOME)
                    }
                    FilterChip("Expense", state.transactionKindFilter == TransactionKindFilter.EXPENSE) {
                        onSelectKind(TransactionKindFilter.EXPENSE)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterTextField(
                        value = state.minimumAmountText,
                        onValueChange = onMinimumAmountChange,
                        label = "Amount >",
                        modifier = Modifier.width(160.dp)
                    )
                    FilterTextField(
                        value = state.maximumAmountText,
                        onValueChange = onMaximumAmountChange,
                        label = "Amount <",
                        modifier = Modifier.width(160.dp)
                    )
                }
                if (state.errorMessage != null) {
                    Text(state.errorMessage, color = Color(0xFFFFB4AB))
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val gutter = 16.dp
            val listWidth = maxWidth * 0.56f
            val detailWidth = maxWidth - listWidth - gutter

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gutter)) {
                Card(
                    modifier = Modifier.width(listWidth).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("Transactions", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            val rows = state.searchResults
                            items(rows) { transaction ->
                                TransactionRow(transaction = transaction, onClick = { onSelectTransaction(transaction.id) })
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.width(detailWidth).fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                        Text("Transaction Detail", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        state.selectedTransaction?.let { detail ->
                            DetailLine("Date", detail.summary.dateIso)
                            DetailLine("Amount", formatAmount(detail.summary.amount), amountColor(detail.summary.amount))
                            DetailLine("Payee", detail.summary.payeeName.orEmpty())
                            DetailLine("Category", detail.summary.categoryName.orEmpty())
                            DetailLine("Memo", detail.summary.memo.orEmpty())
                            DetailLine("Info", detail.summary.info.orEmpty())
                            DetailLine("Tags", detail.summary.tagsText)
                            if (detail.splits.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text("Splits", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(8.dp))
                                detail.splits.forEach { split ->
                                    Text(
                                        "${split.position}: ${split.categoryName.orEmpty()}  ${formatAmount(split.amount)}  ${split.memo.orEmpty()}",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                }
                            }
                        } ?: Text("Select a transaction to inspect it.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionSummary,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(transaction.payeeName ?: transaction.memo ?: "(no text)", fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .background(
                            if (transaction.amount < 0.0) Color(0xFF4A1F1F) else MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        formatAmount(transaction.amount),
                        color = amountColor(transaction.amount),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(transaction.dateIso, color = MaterialTheme.colorScheme.onSurfaceVariant)
            transaction.accountName?.takeIf { it.isNotBlank() }?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            Text(listOfNotNull(transaction.categoryName, transaction.info, transaction.memo).joinToString(" - "), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FilterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                RoundedCornerShape(999.dp)
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(999.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MetaLine(label: String, value: String) {
    Text("$label: $value", color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun DetailLine(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$label:", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        Text(value, color = valueColor)
    }
    Spacer(Modifier.height(6.dp))
}

private fun formatAmount(amount: Double): String {
    val scaled = (amount * 100).roundToLong()
    val sign = if (scaled < 0) "-" else ""
    val absolute = scaled.absoluteValue
    val major = absolute / 100
    val minor = absolute % 100
    return "$sign$major.${minor.toString().padStart(2, '0')}"
}

@Composable
private fun amountColor(amount: Double): Color {
    return if (amount < 0.0) Color(0xFFFF8A80) else Color(0xFF111111)
}
