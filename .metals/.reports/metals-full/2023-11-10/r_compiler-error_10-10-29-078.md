file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala
### file%3A%2F%2F%2FC%3A%2FUsers%2Fyanis%2FOneDrive%2FDocuments%2FESIEE%2FE5%2FC-Scala%2Fscala-optimizer%2Fsrc%2Fmain%2Fscala%2FExecutionPlanOptimizer.scala:26: error: case classes must have a parameter list; try 'case class SortDirection()' or 'case object SortDirection'
case class SortDirection:
                        ^

occurred in the presentation compiler.

action parameters:
uri: file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala
text:
```scala
import scala.util.parsing.combinator.RegexParsers

sealed trait Expression

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

case class SortDirection:
  case Descending 
  case Ascending

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

object SQLOptimizer extends RegexParsers {

  def identifier: Parser[String] = """[a-zA-Z_]\w*""".r

  def namedExpression: Parser[NamedExpression] = identifier ^^ NamedExpression

  def resolvedAttribute: Parser[ResolvedAttribute] = identifier ^^ ResolvedAttribute

  def expression: Parser[Expression] = namedExpression | resolvedAttribute

  def tableScan: Parser[TableScan] = "FROM" ~> identifier ~ rep("," ~> identifier) ^^ {
    case name ~ aliases => TableScan(name, aliases.map(ResolvedAttribute))
  }

  def subQueryAlias: Parser[SubQueryAlias] = identifier ~ opt(identifier) <~ "(" <~ rep(" ") ~ "SELECT" <~ rep(" ") ~ "FROM" <~ rep(" ") ~ ")" ^^ {
    case alias ~ nameOption => SubQueryAlias(alias, TableScan(nameOption.getOrElse(alias), Nil))
  }

  def leafNode: Parser[LeafNode] = tableScan | subQueryAlias

  def range: Parser[Range] = "RANGE" ~> """\d+""".r ~ "," ~ """\d+""".r ^^ {
    case start ~ _ ~ count => Range(start.toInt, count.toInt, null)
  }

  def unaryNode: Parser[UnaryNode] = range ^^ { r => UnaryNode(r) } | filter | sort

  def join: Parser[Join] = leafNode ~ rep("," ~> leafNode) ^^ {
    case left ~ rights => rights.foldLeft(left)((l, r) => Join(l, r))
  }

  def sortDirection: Parser[SortDirection] = "ASC" ^^^ SortDirection.Ascending | "DESC" ^^^ SortDirection.Descending

  def sortOrder: Parser[SortOrder] = expression ~ sortDirection ^^ {
    case exp ~ dir => SortOrder(exp, dir)
  }

  def sort: Parser[Sort] = "ORDER" ~ "BY" ~> rep1(sortOrder) ^^ {
    case orders => Sort(orders, null)
  }

  def filter: Parser[Filter] = "WHERE" ~> expression ^^ { exp => Filter(exp, null) }

  def project: Parser[Project] = "SELECT" ~ rep1(namedExpression) ~ unaryNode ^^ {
    case _ ~ exps ~ next => Project(exps, next)
  }

  def union: Parser[Union] = rep1sep(project, "UNION") ^^ {
    case projects => Union(projects)
  }

  def optimize(sqlQuery: String): Any = parseAll(union, sqlQuery) match {
    case Success(result, _) => result
    case failure: NoSuccess => throw new IllegalArgumentException(s"Failed to parse input: $failure")
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
scala.meta.internal.parsers.Reporter.syntaxError(Reporter.scala:16)
	scala.meta.internal.parsers.Reporter.syntaxError$(Reporter.scala:16)
	scala.meta.internal.parsers.Reporter$$anon$1.syntaxError(Reporter.scala:22)
	scala.meta.internal.parsers.Reporter.syntaxError(Reporter.scala:17)
	scala.meta.internal.parsers.Reporter.syntaxError$(Reporter.scala:17)
	scala.meta.internal.parsers.Reporter$$anon$1.syntaxError(Reporter.scala:22)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$classDef$1(ScalametaParser.scala:3953)
	scala.meta.internal.parsers.ScalametaParser.autoEndPos(ScalametaParser.scala:368)
	scala.meta.internal.parsers.ScalametaParser.autoEndPos(ScalametaParser.scala:373)
	scala.meta.internal.parsers.ScalametaParser.classDef(ScalametaParser.scala:3933)
	scala.meta.internal.parsers.ScalametaParser.tmplDef(ScalametaParser.scala:3894)
	scala.meta.internal.parsers.ScalametaParser.topLevelTmplDef(ScalametaParser.scala:3877)
	scala.meta.internal.parsers.ScalametaParser$$anonfun$2.applyOrElse(ScalametaParser.scala:4483)
	scala.meta.internal.parsers.ScalametaParser$$anonfun$2.applyOrElse(ScalametaParser.scala:4471)
	scala.PartialFunction.$anonfun$runWith$1$adapted(PartialFunction.scala:145)
	scala.meta.internal.parsers.ScalametaParser.statSeqBuf(ScalametaParser.scala:4462)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$batchSource$13(ScalametaParser.scala:4696)
	scala.Option.getOrElse(Option.scala:189)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$batchSource$1(ScalametaParser.scala:4696)
	scala.meta.internal.parsers.ScalametaParser.atPos(ScalametaParser.scala:319)
	scala.meta.internal.parsers.ScalametaParser.autoPos(ScalametaParser.scala:365)
	scala.meta.internal.parsers.ScalametaParser.batchSource(ScalametaParser.scala:4652)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$source$1(ScalametaParser.scala:4645)
	scala.meta.internal.parsers.ScalametaParser.atPos(ScalametaParser.scala:319)
	scala.meta.internal.parsers.ScalametaParser.autoPos(ScalametaParser.scala:365)
	scala.meta.internal.parsers.ScalametaParser.source(ScalametaParser.scala:4645)
	scala.meta.internal.parsers.ScalametaParser.entrypointSource(ScalametaParser.scala:4650)
	scala.meta.internal.parsers.ScalametaParser.parseSourceImpl(ScalametaParser.scala:135)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$parseSource$1(ScalametaParser.scala:132)
	scala.meta.internal.parsers.ScalametaParser.parseRuleAfterBOF(ScalametaParser.scala:59)
	scala.meta.internal.parsers.ScalametaParser.parseRule(ScalametaParser.scala:54)
	scala.meta.internal.parsers.ScalametaParser.parseSource(ScalametaParser.scala:132)
	scala.meta.parsers.Parse$.$anonfun$parseSource$1(Parse.scala:29)
	scala.meta.parsers.Parse$$anon$1.apply(Parse.scala:36)
	scala.meta.parsers.Api$XtensionParseDialectInput.parse(Api.scala:25)
	scala.meta.internal.semanticdb.scalac.ParseOps$XtensionCompilationUnitSource.toSource(ParseOps.scala:17)
	scala.meta.internal.semanticdb.scalac.TextDocumentOps$XtensionCompilationUnitDocument.toTextDocument(TextDocumentOps.scala:206)
	scala.meta.internal.pc.SemanticdbTextDocumentProvider.textDocument(SemanticdbTextDocumentProvider.scala:54)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$semanticdbTextDocument$1(ScalaPresentationCompiler.scala:356)
```
#### Short summary: 

file%3A%2F%2F%2FC%3A%2FUsers%2Fyanis%2FOneDrive%2FDocuments%2FESIEE%2FE5%2FC-Scala%2Fscala-optimizer%2Fsrc%2Fmain%2Fscala%2FExecutionPlanOptimizer.scala:26: error: case classes must have a parameter list; try 'case class SortDirection()' or 'case object SortDirection'
case class SortDirection:
                        ^