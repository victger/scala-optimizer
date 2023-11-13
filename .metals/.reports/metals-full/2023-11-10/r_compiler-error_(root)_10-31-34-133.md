file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala
### java.lang.AssertionError: assertion failed

occurred in the presentation compiler.

action parameters:
uri: file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala
text:
```scala
ealed trait Expression

case class NamedExpression(name: String) extends Expression

case class ResolvedAttribute(name: String) extends Expression

trait ExecutionPlan

trait LeafNode extends ExecutionPlan
trait BinaryNode extends ExecutionPlan {
  {val left: ExecutionPlan
  val right: ExecutionPlan
}
  val next: ExecutionPlan
}
trait BinaryNode extends ExecutionPlan {
  val left: ExecutionPlan
class Project(expression: List[NamedExpression], next: ExecutionPlan) extends UnaryNode(next)
}

/** Represent the top part of a SELECT query (ie. SELECT col1, col2, ..., coln) */
case class Project(expression: List[NamedExpression], next: ExecutionPlan) extends UnaryNode(next)

case class Range(start: Int, count: Int, next: ExecutionPlan) extends UnaryNode(next)

/** Represent the WHERE part of a SELECT query (eg. WHERE col1 = 'data' AND col2 < 3) */
case class Filter(expression: Expression, next: ExecutionPlan) extends UnaryNode(next)

case class Union(subquery: List[ExecutionPlan]) extends ExecutionPlan

/** Used when FROM contains more than one table or subquery */
case class Join(left: ExecutionPlan, right: ExecutionPlan) extends BinaryNode(left, right)

/** Represent the ORDER BY of a SELECT query */
case class Sort(order: List[SortOrder], next: ExecutionPlan) extends UnaryNode(next)

case class SortOrder(expression: Expression, direction: SortDirection)

/** Represent the FROM part of a SELECT query */
case class TableScan(name: String, output: List[ResolvedAttribute]) extends LeafNode

/** Used when you want to give an alias to a table or a subquery (eg. FROM orders o) */
case class SubQueryAlias(name: String, next: ExecutionPlan) extends UnaryNode(next)

object SQLOptimizer {

  // Regular expressions for different parts of the SQL query
  val SelectRegex = """SELECT (.+) FROM (.+)""".r
  val WhereRegex = """WHERE (.+)""".r
  val OrderByRegex = """ORDER BY (.+)""".r
  val RangeRegex = """RANGE (\d+), (\d+)""".r
  val UnionRegex = """\((.+) UNION (.+)\)""".r
  val JoinRegex = """(.+), (.+)""".r

  def optimize(sqlQuery: String): ExecutionPlan = {
    sqlQuery.trim.toUpperCase match {
      case SelectRegex(columns, fromClause) =>
        val columnList = columns.split(",").map(_.trim).map(NamedExpression)
        val tableScan = TableScan(fromClause.trim, Nil)
        Project(columnList.toList, tableScan)

      case WhereRegex(condition) =>
        val innerPlan = optimize(condition)
        Filter(innerPlan, null) // Replace null with the appropriate next plan

      case OrderByRegex(orderBy) =>
        val columnList = orderBy.split(",").map(_.trim).map { col =>
          val direction = if (col.endsWith(" DESC")) SortDirection.Descending else SortDirection.Ascending
          val expr = NamedExpression(col.replaceAll("(ASC|DESC)$", "").trim)
          SortOrder(expr, direction)
        }
        Sort(columnList.toList, null) // Replace null with the appropriate next plan

      case RangeRegex(start, count) =>
        val innerPlan = optimize(sqlQuery.replaceAll("RANGE .+", "").trim)
        Range(start.toInt, count.toInt, innerPlan)

      case UnionRegex(subquery1, subquery2) =>
        val plan1 = optimize(subquery1.trim)
        val plan2 = optimize(subquery2.trim)
        Union(List(plan1, plan2))

      case JoinRegex(left, right) =>
        val leftPlan = optimize(left.trim)
        val rightPlan = optimize(right.trim)
        Join(leftPlan, rightPlan)

      case _ =>
        // Handle other cases or raise an exception for unsupported queries
        throw new IllegalArgumentException(s"Unsupported SQL query: $sqlQuery")
    }
  }
}

  def main(args: Array[String]): Unit = {
    val test1 = SQLOptimizer.optimize("SELECT * FROM table")
    val test2 = SQLOptimizer.optimize("SELECT * FROM table WHERE col1 = col2")
    val test3 = SQLOptimizer.optimize("SELECT * FROM table ORDER BY col1")
    val test4 = SQLOptimizer.optimize("SELECT * FROM table RANGE 0, 10")
    val test5 = SQLOptimizer.optimize("(SELECT * FROM table1) UNION (SELECT * FROM table2)")
    val test6 = SQLOptimizer.optimize("SELECT * FROM table1 t1, (SELECT * FROM table2) t2")

    println(test1)
    println(test2)
    println(test3)
    println(test4)
    println(test5)
    println(test6)
  }

}
```



#### Error stacktrace:

```
scala.runtime.Scala3RunTime$.assertFailed(Scala3RunTime.scala:11)
	dotty.tools.dotc.core.Annotations$LazyAnnotation.tree(Annotations.scala:136)
	dotty.tools.dotc.core.Annotations$Annotation$Child$.unapply(Annotations.scala:242)
	dotty.tools.dotc.typer.Namer.insertInto$1(Namer.scala:477)
	dotty.tools.dotc.typer.Namer.addChild(Namer.scala:488)
	dotty.tools.dotc.typer.Namer$Completer.register$1(Namer.scala:911)
	dotty.tools.dotc.typer.Namer$Completer.registerIfChild$$anonfun$1(Namer.scala:920)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:333)
	dotty.tools.dotc.typer.Namer$Completer.registerIfChild(Namer.scala:920)
	dotty.tools.dotc.typer.Namer$Completer.complete(Namer.scala:815)
	dotty.tools.dotc.core.SymDenotations$SymDenotation.completeFrom(SymDenotations.scala:174)
	dotty.tools.dotc.core.Denotations$Denotation.completeInfo$1(Denotations.scala:187)
	dotty.tools.dotc.core.Denotations$Denotation.info(Denotations.scala:189)
	dotty.tools.dotc.core.Types$NamedType.info(Types.scala:2313)
	dotty.tools.dotc.core.Types$TermLambda.dotty$tools$dotc$core$Types$TermLambda$$_$compute$1(Types.scala:3826)
	dotty.tools.dotc.core.Types$TermLambda.foldArgs$2(Types.scala:3833)
	dotty.tools.dotc.core.Types$TermLambda.dotty$tools$dotc$core$Types$TermLambda$$_$compute$1(Types.scala:4453)
	dotty.tools.dotc.core.Types$TermLambda.dotty$tools$dotc$core$Types$TermLambda$$depStatus(Types.scala:3853)
	dotty.tools.dotc.core.Types$TermLambda.dependencyStatus(Types.scala:3867)
	dotty.tools.dotc.core.Types$TermLambda.isResultDependent(Types.scala:3889)
	dotty.tools.dotc.core.Types$TermLambda.isResultDependent$(Types.scala:3783)
	dotty.tools.dotc.core.Types$MethodType.isResultDependent(Types.scala:3928)
	dotty.tools.dotc.typer.TypeAssigner.assignType(TypeAssigner.scala:292)
	dotty.tools.dotc.typer.TypeAssigner.assignType$(TypeAssigner.scala:16)
	dotty.tools.dotc.typer.Typer.assignType(Typer.scala:116)
	dotty.tools.dotc.ast.tpd$.Apply(tpd.scala:49)
	dotty.tools.dotc.ast.tpd$TreeOps$.appliedToTermArgs$extension(tpd.scala:951)
	dotty.tools.dotc.ast.tpd$.New(tpd.scala:537)
	dotty.tools.dotc.ast.tpd$.New(tpd.scala:528)
	dotty.tools.dotc.core.Annotations$Annotation$Child$.makeChildLater$1(Annotations.scala:231)
	dotty.tools.dotc.core.Annotations$Annotation$Child$.later$$anonfun$1(Annotations.scala:234)
	dotty.tools.dotc.core.Annotations$LazyAnnotation.tree(Annotations.scala:140)
	dotty.tools.dotc.core.Annotations$Annotation$Child$.unapply(Annotations.scala:242)
	dotty.tools.dotc.typer.Namer.insertInto$1(Namer.scala:477)
	dotty.tools.dotc.typer.Namer.addChild(Namer.scala:488)
	dotty.tools.dotc.typer.Namer$Completer.register$1(Namer.scala:911)
	dotty.tools.dotc.typer.Namer$Completer.registerIfChild$$anonfun$1(Namer.scala:920)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:333)
	dotty.tools.dotc.typer.Namer$Completer.registerIfChild(Namer.scala:920)
	dotty.tools.dotc.typer.Namer$Completer.complete(Namer.scala:815)
	dotty.tools.dotc.core.SymDenotations$SymDenotation.completeFrom(SymDenotations.scala:174)
	dotty.tools.dotc.core.Denotations$Denotation.completeInfo$1(Denotations.scala:187)
	dotty.tools.dotc.core.Denotations$Denotation.info(Denotations.scala:189)
	dotty.tools.dotc.core.SymDenotations$SymDenotation.ensureCompleted(SymDenotations.scala:393)
	dotty.tools.dotc.typer.Typer.retrieveSym(Typer.scala:2989)
	dotty.tools.dotc.typer.Typer.typedNamed$1(Typer.scala:3014)
	dotty.tools.dotc.typer.Typer.typedUnadapted(Typer.scala:3111)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3184)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3188)
	dotty.tools.dotc.typer.Typer.traverse$1(Typer.scala:3210)
	dotty.tools.dotc.typer.Typer.typedStats(Typer.scala:3256)
	dotty.tools.dotc.typer.Typer.typedPackageDef(Typer.scala:2812)
	dotty.tools.dotc.typer.Typer.typedUnnamed$1(Typer.scala:3081)
	dotty.tools.dotc.typer.Typer.typedUnadapted(Typer.scala:3112)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3184)
	dotty.tools.dotc.typer.Typer.typed(Typer.scala:3188)
	dotty.tools.dotc.typer.Typer.typedExpr(Typer.scala:3300)
	dotty.tools.dotc.typer.TyperPhase.typeCheck$$anonfun$1(TyperPhase.scala:44)
	dotty.tools.dotc.typer.TyperPhase.typeCheck$$anonfun$adapted$1(TyperPhase.scala:54)
	scala.Function0.apply$mcV$sp(Function0.scala:42)
	dotty.tools.dotc.core.Phases$Phase.monitor(Phases.scala:440)
	dotty.tools.dotc.typer.TyperPhase.typeCheck(TyperPhase.scala:54)
	dotty.tools.dotc.typer.TyperPhase.runOn$$anonfun$3(TyperPhase.scala:88)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:333)
	dotty.tools.dotc.typer.TyperPhase.runOn(TyperPhase.scala:88)
	dotty.tools.dotc.Run.runPhases$1$$anonfun$1(Run.scala:246)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.ArrayOps$.foreach$extension(ArrayOps.scala:1321)
	dotty.tools.dotc.Run.runPhases$1(Run.scala:262)
	dotty.tools.dotc.Run.compileUnits$$anonfun$1(Run.scala:270)
	dotty.tools.dotc.Run.compileUnits$$anonfun$adapted$1(Run.scala:279)
	dotty.tools.dotc.util.Stats$.maybeMonitored(Stats.scala:67)
	dotty.tools.dotc.Run.compileUnits(Run.scala:279)
	dotty.tools.dotc.Run.compileSources(Run.scala:194)
	dotty.tools.dotc.interactive.InteractiveDriver.run(InteractiveDriver.scala:165)
	scala.meta.internal.pc.MetalsDriver.run(MetalsDriver.scala:45)
	scala.meta.internal.pc.PcCollector.<init>(PcCollector.scala:42)
	scala.meta.internal.pc.PcSemanticTokensProvider$Collector$.<init>(PcSemanticTokensProvider.scala:60)
	scala.meta.internal.pc.PcSemanticTokensProvider.Collector$lzyINIT1(PcSemanticTokensProvider.scala:60)
	scala.meta.internal.pc.PcSemanticTokensProvider.Collector(PcSemanticTokensProvider.scala:60)
	scala.meta.internal.pc.PcSemanticTokensProvider.provide(PcSemanticTokensProvider.scala:81)
	scala.meta.internal.pc.ScalaPresentationCompiler.semanticTokens$$anonfun$1(ScalaPresentationCompiler.scala:99)
```
#### Short summary: 

java.lang.AssertionError: assertion failed