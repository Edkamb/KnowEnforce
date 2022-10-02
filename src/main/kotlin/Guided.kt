abstract class RDFRule : Rule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        val (st, ev) = execute(state)
        val hypothesis = Trace(trace.list + ev)
        return hypothesis.validateAsRDF(false, false)
    }
}


object DepositEmptyRDF : RDFRule() {
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("Deposit")+state.stack), Event("Deposit"))
}
object DepositStegoRDF : RDFRule() {
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("Stego")+state.stack), Event("Stego"))
}
object DepositTRexRDF: RDFRule() {
    override fun execute(state: State): Pair<State, Event> = Pair(State(listOf("TRex")+state.stack), Event("TRex"))
}
object ErodeRDF: RDFRule() {
    override fun trigger(state: State, trace: Trace): Boolean {
        return state.stack.isNotEmpty()
    }
    override fun execute(state: State): Pair<State, Event> {
        return Pair(State(state.stack.subList(1, state.stack.size)), Event("Erode"))
    }
}

val rdfRule = listOf(DepositEmptyRDF, DepositStegoRDF, DepositTRexRDF, ErodeRDF)