package br.com.gamemods.mobai.level

import br.com.gamemods.mobai.delegators.reflection.field
import cn.nukkit.level.generator.NetherGenerator
import cn.nukkit.level.generator.NormalGenerator
import cn.nukkit.level.generator.populator.type.Populator

var NormalGenerator.populators by field<NormalGenerator, List<Populator>>("populators")
var NetherGenerator.populators by field<NetherGenerator, List<Populator>>("populators")
