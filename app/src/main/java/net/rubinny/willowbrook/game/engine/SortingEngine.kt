package net.rubinny.willowbrook.game.engine

import net.rubinny.willowbrook.game.model.SortItem
import kotlin.random.Random

/** One item at a time is placed into a category. Wrong placements are not counted against the player. */
class SortingEngine(stepId: String, items: List<SortItem>, val categories: List<String>) {
    val items: List<SortItem> = items.shuffled(Random(stepId.hashCode()))
    private var position = 0
    private val placed = mutableListOf<SortItem>()

    val current: SortItem? get() = items.getOrNull(position)
    val placedItems: List<SortItem> get() = placed.toList()
    val isComplete: Boolean get() = position >= items.size
    val correctCategoryIndex: Int? get() = current?.let { categories.indexOf(it.category) }

    sealed interface Result {
        data class Correct(val item: String, val complete: Boolean) : Result
        data class Wrong(val item: String, val chosenCategory: String, val rightCategory: String) : Result
        data object Ignored : Result
    }

    fun place(categoryIndex: Int): Result {
        val item = current ?: return Result.Ignored
        val chosen = categories[categoryIndex]
        return if (chosen == item.category) {
            placed += item
            position++
            Result.Correct(item.item, isComplete)
        } else {
            Result.Wrong(item.item, chosen, item.category)
        }
    }

    companion object {
        /** Gentle mode keeps at most [max] items, alternating categories so every category is used. */
        fun pick(items: List<SortItem>, categories: List<String>, max: Int): List<SortItem> {
            if (items.size <= max) return items
            val byCategory = categories.map { c -> items.filter { it.category == c }.toMutableList() }
            val result = mutableListOf<SortItem>()
            while (result.size < max && byCategory.any { it.isNotEmpty() }) {
                byCategory.forEach { list -> if (result.size < max && list.isNotEmpty()) result += list.removeAt(0) }
            }
            return result
        }
    }
}
