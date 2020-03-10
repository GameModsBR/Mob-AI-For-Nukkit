package br.com.gamemods.mobai.entity.smart

import br.com.gamemods.mobai.entity.smart.logic.*

interface SmartEntity: VisibilityLogic, UpdateLogic, InitLogic, DespawnLogic, CombatLogic
