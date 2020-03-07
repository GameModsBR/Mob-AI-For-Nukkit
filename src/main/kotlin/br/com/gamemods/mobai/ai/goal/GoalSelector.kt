package br.com.gamemods.mobai.ai.goal

import java.util.*

class GoalSelector {
    private val goals = mutableSetOf<WeightedGoal>()
    private val disabledControls = EnumSet.noneOf(Goal.Control::class.java)!!
    private val goalsByControl = EnumMap<Goal.Control, WeightedGoal>(Goal.Control::class.java)

    fun add(weight: Int, goal: Goal) {
        goals += WeightedGoal(goal, weight)
    }

    fun remove(goal: Goal) {
        val weighted = goals.firstOrNull { it.goal == goal } ?: return
        weighted.stop()
        goals -= weighted
    }

    fun enableControl(control: Goal.Control) {
        disabledControls -= control
    }

    fun disableControl(control: Goal.Control) {
        disabledControls += control
    }

    fun enableAllControls() {
        disabledControls.clear()
    }

    fun disableAllControls() {
        disabledControls += Goal.Control.values()
    }

    fun runningGoals(): Sequence<Goal> = goals.asSequence().filter { it.isRunning }

    fun tick(): Boolean {
        cleanup()
        update()
        tickGoals()
        return true
    }

    private fun cleanup() {
        runningGoals().filter {
            it.controls.any(disabledControls::contains) || !it.shouldContinue()
        }.forEach(Goal::stop)
        goalsByControl.values.removeIf { !it.isRunning }
    }

    private fun update() {
        goals.asSequence()
            .filter { !it.isRunning }
            .filter { it.controls.none(disabledControls::contains) }
            .filter { it.controls.all { ctr-> goalsByControl.getOrDefault(ctr, NoOpWeightedGoal).canBeReplacedBy(it) } }
            .filter(WeightedGoal::canStart)
            .forEach { goal ->
                goal.controls.forEach { ctr ->
                    goalsByControl[ctr]?.stop()
                    goalsByControl[ctr] = goal
                }
                goal.start()
            }
    }

    private fun tickGoals() {
        runningGoals().forEach(Goal::tick)
    }

    private object NoOpGoal: Goal() {
        override fun canStart() = false
    }

    private object NoOpWeightedGoal: WeightedGoal(NoOpGoal, Int.MAX_VALUE) {
        override var isRunning: Boolean
            get() = false
            set(_) {}
    }

    private open class WeightedGoal(val goal: Goal, val weight: Int): Goal() {
        open var isRunning = false; protected set

        override val controls get() = goal.controls

        fun canBeReplacedBy(goal: WeightedGoal): Boolean {
            return canStop() && goal.weight < this.weight
        }

        override fun canStart(): Boolean {
            return goal.canStart()
        }

        override fun shouldContinue(): Boolean {
            return goal.shouldContinue()
        }

        override fun canStop(): Boolean {
            return goal.canStop()
        }

        override fun start() {
            if (!isRunning) {
                isRunning = true
                goal.start()
            }
        }

        override fun stop() {
            if (this.isRunning) {
                this.isRunning = false
                goal.stop()
            }
        }

        override fun tick() {
            goal.tick()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WeightedGoal

            if (goal != other.goal) return false

            return true
        }

        override fun hashCode(): Int {
            return goal.hashCode()
        }

        override fun toString(): String {
            return "$weight:$goal:$isRunning"
        }
    }
}
