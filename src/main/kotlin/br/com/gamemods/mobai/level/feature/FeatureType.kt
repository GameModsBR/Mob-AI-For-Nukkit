package br.com.gamemods.mobai.level.feature

import cn.nukkit.level.generator.feature.Feature
import cn.nukkit.utils.Identifier

data class FeatureType<F: Feature>(val id: Identifier, val featureClass: Class<out F>) {
    constructor(namespace: String, name: String, feature: Class<out F>): this(Identifier.from(namespace, name), feature)
    constructor(identifier: String, feature: Class<out F>): this(Identifier.fromString(identifier), feature)
}
