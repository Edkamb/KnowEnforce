import com.github.owlcs.ontapi.OntManagers
import com.github.owlcs.ontapi.OntologyManager
import org.apache.jena.graph.Graph
import org.apache.jena.graph.Node
import org.apache.jena.graph.NodeFactory
import org.apache.jena.graph.Triple
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.reasoner.ReasonerRegistry
import org.semanticweb.owlapi.reasoner.OWLReasoner
import java.io.*
import java.util.concurrent.TimeUnit

var total = 0L
var totalBuild = 0L
val ontManager: OntologyManager = OntManagers.createManager()

class State (val stack : List<String>){
    override fun toString(): String = stack.toString()
}
data class Trace (val list : List<Event>){

    override fun toString(): String = list.toString()
    fun getSMT(s: String): Boolean {
        var res =
            """
            (declare-datatype Event ( (Stego) (TRex) (Deposit) (Erode) (Empty)))
            (declare-fun trace (Int) Event)
            (assert (forall ((i Int)) (=> (or (< i 0) (>= i ${list.size})) (= (trace i) Empty))))

            """.trimIndent()
        res += "\n"
        var i = 0
        while(i < list.size) {
            res += list[i].getSmt(i)
            i++
        }
        res += "\n $s \n (check-sat)"

        File("/tmp/new.smt").writeText(res)
        val smt = "z3 /tmp/new.smt".runCommand()!!.trim()
       // println(smt)
        if(smt.contains("(")) System.exit(0)
        return smt == "sat"
    }
    var i = 0;
    fun validateAsRDF(hermit : Boolean = false, output : Boolean = false) : Boolean {
        var model = ModelFactory.createDefaultModel()


        val m = ontManager.getGraphModel("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide")
        ontManager.clearOntologies()
        //println("could find gag? ${m != null}")
        if(m == null){
            model.read("geo_core.owl")//backgr)
        }
        else model = m

        //model.graph.prefixMapping.nsPrefixMap["GeoAgeGuide"] = "http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#"
        //model.graph.prefixMapping.nsPrefixMap["rdf"] = "http://www.w3.org/1999/02/22-rdf-syntax-ns#"

        val startBuild = System.currentTimeMillis()
        var i = 0
        while(i < list.size){
            val id = NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#depos$i")
            model.graph.add(Triple(id, NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#Deposition")))

            list[i].addTriples(i, id, model.graph)

            if(i > 0)
                model.graph.add(Triple(NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#depos${i-1}"),NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#beforeDeposit"), id))
            i++
        }

        totalBuild += System.currentTimeMillis() - startBuild
        val conf = ontManager.ontologyLoaderConfiguration
        conf.isPerformTransformation = true

        val start = System.currentTimeMillis()
        val ret = if(hermit){
            val reasoner : OWLReasoner = org.semanticweb.HermiT.ReasonerFactory().createReasoner(ontManager.addOntology(model.graph, conf))
            reasoner.isConsistent
        } else {
            val reasoner = ReasonerRegistry.getOWLReasoner()// Get correct reasoner based on settings
            val rModel = ModelFactory.createInfModel(reasoner, model)
            if(output) {
                File("output${this.hashCode()}.ttl").createNewFile()
                model.write(FileWriter("output${this.hashCode()}.ttl"),"TTL")
            }
            rModel.validate().isValid
        }
        total += System.currentTimeMillis() - start
        return ret
    }
}
data class Event (val name : String, val param : String = "_") {
    override fun toString(): String = "Ev($name, $param)"
    fun getSmt(i : Int) : String = when(name) {
        "Deposit"  -> "(assert ( = (trace $i) Deposit))\n"
        "Stego"  -> "(assert ( = (trace $i) Stego))\n"
        "TRex"  -> "(assert ( = (trace $i) TRex))\n"
        else -> "(assert ( = (trace $i) Erode))\n"
    }

    fun addTriples(i : Int, id : Node, graph : Graph){
        val n = NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#x$i")
        when(name) {
            "Deposit" -> {
                //graph.add(Triple(id, NodeFactory.createURI("GeoAgeGuide:contains"), n))
                graph.add(Triple(id, NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#Deposition")))
            }
            "Stego" -> {
                //graph.add(Triple(id, NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#contains"), n))
                //graph.add(Triple(id, NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#contains"), n))
                graph.add(Triple(id, NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#contains"), NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#Stegosaurus")))
            }
            "TRex" -> {
                graph.add(Triple(id, NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#contains"), NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#Tyrannosaurus")))
                //graph.add(Triple(id, NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#contains"), n))
                //graph.add(Triple(n, NodeFactory.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), NodeFactory.createURI("http://www.semanticweb.org/edkam/ontologies/2022/2/GeoAgeGuide#Tyrannosaurus")))
            }
            else -> {
                //graph.add(Triple(id, NodeFactory.createURI("rdf:type"), NodeFactory.createURI("GeoAgeGuide:blank")))
            }
        }
    }
}

abstract class Rule {
    abstract fun trigger(state: State, trace: Trace): Boolean
    abstract fun execute(state: State): Pair<State, Event>
}

class Engine (private val rules : List<Rule>){
    fun explore(state: State, n : Int, prefix: Trace) : List<Trace>{
        val collect = mutableListOf<Trace>()
        if(n <= 0)  return listOf(prefix)

        for(r in rules){
            if(r.trigger(state, prefix)){
                val (st, ev) = r.execute(state)
                collect += explore(st, n-1, Trace(prefix.list + ev))
            }
        }
        return if(collect.isEmpty()) listOf(prefix) else collect
    }
}




fun main(/*args: Array<String>*/) {
    val n = 6
    println("IMPL")
    val implList = runImplemented(n)
    println("SMT")
    val smtList = runSMT(n)
    println("RDF")
    val rdfList = runRDF(n)
    /*println("\n\tin rdf but not impl\n")
    for( t in rdfList ){
        if( !implList.toSet().contains(t) ) {
            println(t)
            t.validateAsRDF(output = true)
        }
    }

    println("\n\tin impl but not rdf\n")
    for( t in implList ){
        if( !rdfList.toSet().contains(t) ) {
            println(t)
            t.validateAsRDF(output = true)
        }
    }*/
    /*val my = Trace(listOf(Event(("TRex"))))
    val st = State(listOf("TRex"))
    val smt = DepositStegoSMT.trigger(st, my)
    val impl = DepositStegoImpl.trigger(st, my)
    println(smt)
    println(impl)*/

}

fun runRDF(n : Int) : List<Trace> {
    totalBuild = 0
    total = 0
    val engine = Engine(rdfRule)
    val tStart = System.currentTimeMillis()
    val traces = engine.explore(State(emptyList()),n,Trace(emptyList()))
    val tEnd = System.currentTimeMillis()
    println("total:" + (tEnd - tStart))
    println("building: $totalBuild")
    println("reasoning: $total")
    println("#traces: ${traces.size}")
    return traces
}

fun runSMT(n : Int) : List<Trace> {
    totalBuild = 0
    total = 0
    val engine = Engine(smtRule)
    val tStart = System.currentTimeMillis()
    val traces = engine.explore(State(emptyList()),n,Trace(emptyList()))
    val tEnd = System.currentTimeMillis()
    println("total:" + (tEnd - tStart))
    println("building: $totalBuild")
    println("reasoning: $total")
    println("#traces: ${traces.size}")
    return traces
}

fun runImplemented(n : Int) : List<Trace> {
    totalBuild = 0
    total = 0
    val engine = Engine(implemented)
    val tStart = System.currentTimeMillis()
    val traces = engine.explore(State(emptyList()),n,Trace(emptyList()))
    val tEnd = System.currentTimeMillis()
    println("total:" + (tEnd - tStart))
    println("building: $totalBuild")
    println("reasoning: $total")
    println("#traces: ${traces.size}")
    return traces
}





/* https://stackoverflow.com/questions/35421699 */
fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String? = try {
    ProcessBuilder(split("\\s".toRegex()))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().apply { waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
} catch (e: java.io.IOException) {
    e.printStackTrace()
    null
}