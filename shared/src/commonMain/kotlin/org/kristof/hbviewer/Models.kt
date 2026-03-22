package org.kristof.hbviewer

import kotlinx.datetime.LocalDate

data class AccountRecord(
    val id: Long,
    val position: Long,
    val name: String,
    val type: Long,
    val currencyId: Long?,
    val flags: Long,
    val number: String?,
    val bankName: String?,
    val notes: String?
)

data class PayeeRecord(
    val id: Long,
    val name: String
)

data class CategoryRecord(
    val id: Long,
    val parentId: Long?,
    val name: String
)

data class TagRecord(
    val id: Long,
    val name: String
)

data class SplitRecord(
    val position: Int,
    val categoryId: Long?,
    val amount: Double,
    val memo: String?
)

data class TransactionRecord(
    val id: Long,
    val accountId: Long,
    val dateOrdinal: Int,
    val date: LocalDate,
    val amount: Double,
    val payeeId: Long?,
    val categoryId: Long?,
    val memo: String?,
    val info: String?,
    val flags: Long,
    val status: Long,
    val transferAccountId: Long?,
    val transferAmount: Double?,
    val tagIds: List<Long>,
    val splits: List<SplitRecord>
)

data class HomeBankData(
    val title: String?,
    val accounts: List<AccountRecord>,
    val payees: List<PayeeRecord>,
    val categories: List<CategoryRecord>,
    val tags: List<TagRecord>,
    val transactions: List<TransactionRecord>
)

data class ImportSummary(
    val sourceName: String,
    val importedAt: String,
    val accountCount: Int,
    val transactionCount: Int
)

data class AccountSummary(
    val id: Long,
    val position: Long,
    val name: String,
    val type: Long,
    val flags: Long,
    val transactionCount: Long
)

data class TransactionSummary(
    val id: Long,
    val accountId: Long,
    val accountName: String?,
    val dateIso: String,
    val amount: Double,
    val payeeName: String?,
    val categoryName: String?,
    val memo: String?,
    val info: String?,
    val transferAccountId: Long?,
    val transferAmount: Double?,
    val flags: Long,
    val status: Long,
    val tagsText: String
)

data class SplitSummary(
    val position: Long,
    val amount: Double,
    val memo: String?,
    val categoryId: Long?,
    val categoryName: String?
)

data class TransactionDetail(
    val summary: TransactionSummary,
    val splits: List<SplitSummary>
)

enum class TransactionKindFilter {
    ALL,
    INCOME,
    EXPENSE
}

data class ViewerSnapshot(
    val importSummary: ImportSummary?,
    val accounts: List<AccountSummary>,
    val selectedAccountId: Long?,
    val transactions: List<TransactionSummary>,
    val searchQuery: String,
    val searchResults: List<TransactionSummary>,
    val transactionKindFilter: TransactionKindFilter,
    val minimumAmountText: String,
    val maximumAmountText: String,
    val selectedTransaction: TransactionDetail?,
    val errorMessage: String? = null
) {
    companion object {
        fun empty(): ViewerSnapshot = ViewerSnapshot(
            importSummary = null,
            accounts = emptyList(),
            selectedAccountId = null,
            transactions = emptyList(),
            searchQuery = "",
            searchResults = emptyList(),
            transactionKindFilter = TransactionKindFilter.ALL,
            minimumAmountText = "",
            maximumAmountText = "",
            selectedTransaction = null,
            errorMessage = null
        )
    }
}
