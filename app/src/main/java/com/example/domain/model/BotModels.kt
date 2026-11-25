package com.example.domain.model

data class BotTemplate(
    val id: String,
    val name: String,
    val shortTagline: String,
    val description: String,
    val priceSom: Long,
    val monthlyServerFeeSom: Long,
    val category: String,
    val version: String,
    val author: String,
    val commands: List<String>,
    val features: List<String>,
    val pythonCode: String,
    val infoJson: String,
    val drawableResName: String? = null,
    val iconFilePath: String? = null,
    val isCustomImported: Boolean = false,
    val sourceFolderPath: String? = null,
    val uptimeSla: String = "99.9%",
    val speedScore: Int = 96,
    val reliabilityScore: Int = 98,
    val popularityScore: Int = 92
)

enum class CatalogFilter(val label: String) {
    ALL("Barchasi"),
    FREE("Bepul (0 so'm)"),
    PAID("Pullik"),
    CUSTOM("Yuklanganlar")
}

fun formatSom(amount: Long): String {
    val absStr = kotlin.math.abs(amount).toString()
    val grouped = absStr.reversed().chunked(3).joinToString(" ").reversed()
    return if (amount < 0) "-$grouped so'm" else "$grouped so'm"
}
