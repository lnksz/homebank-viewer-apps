package org.kristof.hbviewer

import kotlinx.datetime.LocalDate

class HomeBankParser {
    fun parse(xml: String): HomeBankData {
        val accounts = mutableListOf<AccountRecord>()
        val payees = mutableListOf<PayeeRecord>()
        val categories = mutableListOf<CategoryRecord>()
        val tags = mutableListOf<TagRecord>()
        val transactions = mutableListOf<TransactionRecord>()
        var title: String? = null
        var nextTransactionId = 1L

        SELF_CLOSING_TAG_REGEX.findAll(xml).forEach { match ->
            val element = match.groupValues[1]
            val rawAttributes = match.groupValues[2]
            val attrs = parseAttributes(rawAttributes)

            when (element) {
                "properties" -> title = attrs["title"]?.takeIf { it.isNotBlank() }
                "account" -> accounts += AccountRecord(
                    id = attrs.long("key", 0L),
                    position = attrs.long("pos", 0L),
                    name = attrs["name"].orEmpty(),
                    type = attrs.long("type", 0L),
                    currencyId = attrs.longOrNull("curr"),
                    flags = attrs.long("flags", 0L),
                    number = attrs.blankToNull("number"),
                    bankName = attrs.blankToNull("bankname"),
                    notes = attrs.blankToNull("notes")
                )

                "pay" -> payees += PayeeRecord(
                    id = attrs.long("key", 0L),
                    name = attrs["name"].orEmpty()
                )

                "cat" -> categories += CategoryRecord(
                    id = attrs.long("key", 0L),
                    parentId = attrs.longOrNull("parent"),
                    name = attrs["name"].orEmpty()
                )

                "tag" -> tags += TagRecord(
                    id = attrs.long("key", 0L),
                    name = attrs["name"].orEmpty()
                )

                "ope" -> {
                    val splits = parseSplits(attrs)
                    val ordinal = attrs.int("date", 1)
                    transactions += TransactionRecord(
                        id = nextTransactionId++,
                        accountId = attrs.long("account", 0L),
                        dateOrdinal = ordinal,
                        date = ordinal.toHomeBankDate(),
                        amount = attrs.double("amount", 0.0),
                        payeeId = attrs.longOrNull("payee"),
                        categoryId = attrs.longOrNull("category"),
                        memo = attrs.blankToNull("wording"),
                        info = attrs.blankToNull("info"),
                        flags = attrs.long("flags", 0L),
                        status = attrs.long("st", 0L),
                        transferAccountId = attrs.longOrNull("dst_account"),
                        transferAmount = attrs.doubleOrNull("damt"),
                        tagIds = parseTags(attrs["tags"]),
                        splits = splits
                    )
                }
            }
        }

        return HomeBankData(
            title = title,
            accounts = accounts.sortedBy { it.position },
            payees = payees.sortedBy { it.name.lowercase() },
            categories = categories.sortedBy { it.name.lowercase() },
            tags = tags.sortedBy { it.name.lowercase() },
            transactions = transactions.sortedByDescending { it.dateOrdinal }
        )
    }

    private fun parseSplits(attrs: Map<String, String>): List<SplitRecord> {
        val categories = attrs["scat"]?.split("||") ?: return emptyList()
        val amounts = attrs["samt"]?.split("||") ?: return emptyList()
        val memos = attrs["smem"]?.split("||") ?: return emptyList()

        if (categories.size != amounts.size || amounts.size != memos.size) {
            return emptyList()
        }

        return categories.indices.map { index ->
            SplitRecord(
                position = index,
                categoryId = categories[index].toLongOrNull(),
                amount = amounts[index].toDoubleOrNull() ?: 0.0,
                memo = memos[index].takeIf { it.isNotBlank() }
            )
        }
    }

    private fun parseTags(raw: String?): List<Long> {
        return raw
            ?.split(' ')
            ?.mapNotNull { it.toLongOrNull() }
            ?.distinct()
            ?: emptyList()
    }

    private fun parseAttributes(raw: String): Map<String, String> {
        val attributes = linkedMapOf<String, String>()
        ATTRIBUTE_REGEX.findAll(raw).forEach { attrMatch ->
            attributes[attrMatch.groupValues[1]] = decodeXml(attrMatch.groupValues[2])
        }
        return attributes
    }

    private fun decodeXml(value: String): String {
        var result = value
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")

        HEX_ENTITY_REGEX.findAll(result).toList().forEach { match ->
            val codePoint = match.groupValues[1].toIntOrNull(16) ?: return@forEach
            result = result.replace(match.value, codePoint.toChar().toString())
        }
        DEC_ENTITY_REGEX.findAll(result).toList().forEach { match ->
            val codePoint = match.groupValues[1].toIntOrNull() ?: return@forEach
            result = result.replace(match.value, codePoint.toChar().toString())
        }
        return result
    }

    private fun Map<String, String>.long(key: String, default: Long): Long = this[key]?.toLongOrNull() ?: default

    private fun Map<String, String>.int(key: String, default: Int): Int = this[key]?.toIntOrNull() ?: default

    private fun Map<String, String>.double(key: String, default: Double): Double = this[key]?.toDoubleOrNull() ?: default

    private fun Map<String, String>.longOrNull(key: String): Long? = this[key]?.toLongOrNull()?.takeIf { it != 0L }

    private fun Map<String, String>.doubleOrNull(key: String): Double? = this[key]?.toDoubleOrNull()

    private fun Map<String, String>.blankToNull(key: String): String? = this[key]?.takeIf { it.isNotBlank() && it != "(null)" }

    companion object {
        private val SELF_CLOSING_TAG_REGEX = Regex("<([a-z]+)\\b([^<>]*)/>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        private val ATTRIBUTE_REGEX = Regex("([A-Za-z0-9_]+)=\"([^\"]*)\"", RegexOption.DOT_MATCHES_ALL)
        private val HEX_ENTITY_REGEX = Regex("&#x([0-9A-Fa-f]+);")
        private val DEC_ENTITY_REGEX = Regex("&#([0-9]+);")
    }
}

private fun Int.toHomeBankDate(): LocalDate {
    val dayOneEpoch = LocalDate(1, 1, 1).toEpochDays()
    return LocalDate.fromEpochDays(dayOneEpoch + this - 1)
}
