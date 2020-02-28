package br.com.gamemods.mobai.ai.goal

import java.util.*

abstract class Goal {
    open val controls = EnumSet.noneOf(Control::class.java)!!

    abstract fun canStart(): Boolean

    open fun shouldContinue() = canStart()

    open fun canStop() = true

    open fun start() {
    }

    open fun stop() {
    }

    open fun tick() {
    }

    protected fun setControls(vararg newControls: Control) {
        controls.clear()
        addControls(*newControls)
    }

    protected fun addControls(vararg newControls: Control) {
        controls.addAll(newControls)
    }

    override fun toString(): String {
        return "${javaClass.simpleName}@${hashCode()}"
    }

    enum class Control {
        MOVE, LOOK, JUMP, TARGET
    }
}
