object DepositEmptyImpl : Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return true
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("Deposit")+state.stack), Event("Deposit"))
}
object DepositStegoImpl : Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return !trace.list.any { it.name == "TRex" }
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("Stego")+state.stack), Event("Stego"))
}
object DepositTRexImpl: Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return true
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("TRex")+state.stack), Event("TRex"))
}
object ErodeImpl: Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return state.stack.isNotEmpty()
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(state.stack.subList(1, state.stack.size)), Event("Erode"))
}

val implemented = listOf(DepositEmptyImpl, DepositStegoImpl, DepositTRexImpl, ErodeImpl)

/*
object AddA : Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return state.stack.isEmpty() || state.stack[0] != "A"
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("A")+state.stack), Event("A"))
}
object AddB: Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return state.stack.isNotEmpty() && state.stack[0] == "A"
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("B")+state.stack), Event("B"))
}
object AddC: Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return state.stack.isNotEmpty() && state.stack[0] == "A"
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("C")+state.stack), Event("C"))
}
object Pop: Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return state.stack.isNotEmpty()
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(state.stack.subList(1, state.stack.size)), Event("-"))
}

val implemented = listOf(AddA, AddB, AddC, Pop)*/