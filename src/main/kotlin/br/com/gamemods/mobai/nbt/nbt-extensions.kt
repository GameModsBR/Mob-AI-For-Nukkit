package br.com.gamemods.mobai.nbt

import cn.nukkit.nbt.tag.ListTag
import cn.nukkit.nbt.tag.Tag

inline fun <T: Tag, R> ListTag<T>.listen(index: Int, callback: (T) -> R): R? {
    if (size() > index) {
        return callback(get(index))
    }
    return null
}

inline fun <T: Tag> ListTag<T>.forEach(start: Int = 0, end: Int = size(), action: (tag: T) -> Unit) {
    (start until end).forEach {
        action(get(it))
    }
}

inline fun <T: Tag> ListTag<T>.forEachIndexed(start: Int = 0, end: Int = size(), action: (index: Int, tag: T) -> Unit) {
    for (i in start until end) {
        action(i, get(i))
    }
}

fun <T: Tag> ListTag<T>.getOrNull(index: Int) = if (index < 0 || index >= size()) null else get(index)

fun <T: Tag> ListTag<T>.first(): T = get(0)
fun <T: Tag> ListTag<T>.last(): T = get(size() - 1)
fun <T: Tag> ListTag<T>.firstOrNull() = getOrNull(0)
fun <T: Tag> ListTag<T>.lastOrNull() = getOrNull(size() - 1)
