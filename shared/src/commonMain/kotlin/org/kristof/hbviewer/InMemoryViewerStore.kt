package org.kristof.hbviewer

import kotlinx.datetime.Clock

class InMemoryViewerStore : ViewerStore {
    private val parser = HomeBankParser()

    private var importSummary: ImportSummary? = null
    private var accounts: List<AccountSummary> = emptyList()
    private var transactions: List<TransactionSummary> = emptyList()
    private var transactionDetails: Map<Long, TransactionDetail> = emptyMap()

    override fun importXml(sourceName: String, xml: String): ImportSummary {
        val parsed = parser.parse(xml)
        val payeesById = parsed.payees.associateBy { it.id }
        val categoriesById = parsed.categories.associateBy { it.id }
        val tagsById = parsed.tags.associateBy { it.id }
        val accountsById = parsed.accounts.associateBy { it.id }

        accounts = parsed.accounts.map { account ->
            AccountSummary(
                id = account.id,
                position = account.position,
                name = account.name,
                type = account.type,
                flags = account.flags,
                transactionCount = parsed.transactions.count { it.accountId == account.id }.toLong()
            )
        }.sortedWith(compareBy<AccountSummary>({ it.position }, { it.name.lowercase() }))

        transactionDetails = parsed.transactions.associate { transaction ->
            val tagNames = transaction.tagIds.mapNotNull { tagsById[it]?.name }.joinToString(" ")
            val summary = TransactionSummary(
                id = transaction.id,
                accountId = transaction.accountId,
                accountName = accountsById[transaction.accountId]?.name,
                dateIso = transaction.date.toString(),
                amount = transaction.amount,
                payeeName = transaction.payeeId?.let(payeesById::get)?.name,
                categoryName = transaction.categoryId?.let(categoriesById::get)?.name,
                memo = transaction.memo,
                info = transaction.info,
                transferAccountId = transaction.transferAccountId,
                transferAmount = transaction.transferAmount,
                flags = transaction.flags,
                status = transaction.status,
                tagsText = tagNames
            )
            val detail = TransactionDetail(
                summary = summary,
                splits = transaction.splits.map { split ->
                    SplitSummary(
                        position = split.position.toLong(),
                        amount = split.amount,
                        memo = split.memo,
                        categoryId = split.categoryId,
                        categoryName = split.categoryId?.let(categoriesById::get)?.name
                    )
                }
            )
            transaction.id to detail
        }

        transactions = transactionDetails.values.map { it.summary }.sortedByDescending { it.dateIso }

        importSummary = ImportSummary(
            sourceName = sourceName,
            importedAt = Clock.System.now().toString(),
            accountCount = parsed.accounts.size,
            transactionCount = parsed.transactions.size
        )
        return importSummary!!
    }

    override fun getImportSummary(): ImportSummary? = importSummary

    override fun getAccounts(): List<AccountSummary> = accounts

    override fun getTransactionsByAccount(accountId: Long): List<TransactionSummary> {
        return transactions.filter { it.accountId == accountId }
    }

    override fun searchTransactions(
        query: String,
        accountId: Long?,
        kindFilter: TransactionKindFilter,
        minimumAmount: Double?,
        maximumAmount: Double?
    ): List<TransactionSummary> {
        val needle = query.trim().lowercase()
        return transactions.filter { summary ->
            (accountId == null || summary.accountId == accountId) &&
                matchesKind(summary, kindFilter) &&
                matchesAmount(summary, minimumAmount, maximumAmount) &&
                (needle.isBlank() || buildSearchable(summary, transactionDetails[summary.id]).contains(needle))
        }
    }

    override fun getTransactionDetail(transactionId: Long): TransactionDetail? = transactionDetails[transactionId]

    private fun buildSearchable(summary: TransactionSummary, detail: TransactionDetail?): String {
        return buildList {
            add(summary.payeeName)
            add(summary.categoryName)
            add(summary.memo)
            add(summary.info)
            add(summary.tagsText)
            detail?.splits?.forEach { split ->
                add(split.memo)
                add(split.categoryName)
            }
        }.filterNotNull().joinToString(" ").lowercase()
    }

    private fun matchesKind(summary: TransactionSummary, kindFilter: TransactionKindFilter): Boolean {
        return when (kindFilter) {
            TransactionKindFilter.ALL -> true
            TransactionKindFilter.INCOME -> summary.amount >= 0.0
            TransactionKindFilter.EXPENSE -> summary.amount < 0.0
        }
    }

    private fun matchesAmount(summary: TransactionSummary, minimumAmount: Double?, maximumAmount: Double?): Boolean {
        val absoluteAmount = kotlin.math.abs(summary.amount)
        val minimumMatches = minimumAmount?.let { absoluteAmount > it } ?: true
        val maximumMatches = maximumAmount?.let { absoluteAmount < it } ?: true
        return minimumMatches && maximumMatches
    }
}
