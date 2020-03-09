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
