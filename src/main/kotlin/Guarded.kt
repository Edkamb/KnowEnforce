object DepositEmptySMT : Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return trace.getSMT("(assert true)")
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("Deposit")+state.stack), Event("Deposit"))
}
object DepositStegoSMT : Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        val assertion =
            """
                (assert (not (exists ((i Int)) (and (>= i 0) ( <= i ${trace.list.size}) ( = (trace i) TRex)))))
            """.trimIndent()
        return trace.getSMT(assertion)
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("Stego")+state.stack), Event("Stego"))
}
object DepositTRexSMT: Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return trace.getSMT("(assert true)")
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("TRex")+state.stack), Event("TRex"))
}
object ErodeSMT: Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return state.stack.isNotEmpty() && trace.getSMT("(assert true)")
    }
    override fun execute(state: State): Pair<State, Event> = Pair(State(state.stack.subList(1, state.stack.size)), Event("Erode"))
}

val smtRule = listOf(DepositEmptySMT, DepositStegoSMT, DepositTRexSMT, ErodeSMT)