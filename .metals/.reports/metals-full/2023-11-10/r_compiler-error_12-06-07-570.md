file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/ExecutionPlanOptimizerTests.scala
### java.lang.AssertionError: NoDenotation.owner

occurred in the presentation compiler.

action parameters:
uri: file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/ExecutionPlanOptimizerTests.scala
text:
```scala
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers


// Regular expressions for different parts of the SQL query
val SelectRegex = """SELECT (.+) FROM (.+)""".r
val WhereRegex = """WHERE (.+)""".r
val OrderByRegex = """ORDER BY (.+)""".r
val RangeRegex = """RANGE (\d+), (\d+)""".r
val UnionRegex = """\((.+) UNION (.+)\)""".r
val JoinRegex = """(.+), (.+)""".r

def optimizePlan(plan: Project): Project = plan match {
  case Project(expressions, TableScan(name, attributes)) =>
    val optimizedExpressions = expressions.flatMap {
      case NamedExpression("*", Star) =>
        attributes.map(attr => NamedExpression(attr.name, attr))
      case NamedExpression(alias, UnresolvedAttribute(attributeName)) =>
        val resolvedAttr = attributes.find(_.name == attributeName)
          .getOrElse(throw new IllegalArgumentException(s"Unresolved attribute not found: $attributeName"))
        Some(NamedExpression(alias, resolvedAttr))
      // Handle other cases if needed
      case expr => Some(expr)
    }
    Project(optimizedExpressions, TableScan(name, attributes))

  // Handle other cases if needed
  case null => plan
}


def readData(source: String): Seq[Map[String, Any]] = {
    // Implémentation fictive
  Seq.empty
}

def filterData(data: Seq[Map[String, Any]], condition: Map[String, Any] => Boolean): Seq[Map[String, Any]] = {
  // Implémentation fictive
  data.filter(condition)
}

def transformData(data: Seq[Map[String, Any]], transformation: Map[String, Any] => Map[String, Any]): Seq[Map[String, Any]] = {
  // Implémentation fictive
  data.map(transformation)
}

def groupData(data: Seq[Map[String, Any]], key: String): Map[Any, Seq[Map[String, Any]]] = {
  // Implémentation fictive
  data.groupBy(_(key))
}

def joinData(dataSet1: Seq[Map[String, Any]], dataSet2: Seq[Map[String, Any]], key: String): Seq[Map[String, Any]] = {
  // Implémentation fictive
  for {
    data1 <- dataSet1
    data2 <- dataSet2 if data1(key) == data2(key)
  } yield data1 ++ data2
}

class ExecutionPlanOptimizerTests extends AnyFunSuite with Matchers {

  // Test 1 : Vérifie si la fonction readData lit correctement les données depuis la source
  test("readData should read data from the source") {
    val data = readData("source")
    data shouldBe a[Seq[_]]
  }

  // Test 2 : Vérifie si la fonction filterData filtre correctement les données
  test("filterData should filter the data based on a condition") {
    val data = Seq(Map("id" -> 1), Map("id" -> 2))
    val filteredData = filterData(data, _ == Map("id" -> 1))
    filteredData should contain only Map("id" -> 1)
  }

  // Test 3 : Vérifie si la fonction transformData applique correctement une transformation aux données
  test("transformData should apply a transformation to the data") {
    val data = Seq(Map("id" -> 1))
    val transformedData = transformData(data, _ + ("name" -> "test"))
    transformedData should contain only Map("id" -> 1, "name" -> "test")
  }

  // Test 4 : Vérifie si la fonction groupData groupe correctement les données par une clé spécifiée
  test("groupData should group data by a specified key") {
    val data = Seq(Map("group" -> "a"), Map("group" -> "b"), Map("group" -> "a"))
    val groupedData = groupData(data, "group")
    groupedData.keys should contain allOf ("a", "b")
    groupedData("a") should have size 2
    groupedData("b") should have size 1
  }

  // Test 5 : Vérifie si la fonction joinData joint correctement deux ensembles de données sur une clé spécifiée
  test("joinData should join two data sets on a specified key") {
    val dataSet1 = Seq(Map("id" -> 1, "value1" -> "A"), Map("id" -> 2, "value1" -> "B"))
    val dataSet2 = Seq(Map("id" -> 1, "value2" -> "C"), Map("id" -> 3, "value2" -> "D"))
    val joinedData = joinData(dataSet1, dataSet2, "id")
    joinedData should contain only Map("id" -> 1, "value1" -> "A", "value2" -> "C")
  }

  test("optimizePlan should replace star with explicit named expressions") {
    val tableScan = TableScan("orders", List(
      ResolvedAttribute("id", "orders"),
      ResolvedAttribute("client", "orders"),
      ResolvedAttribute("timestamp", "orders"),
      ResolvedAttribute("product", "orders"),
      ResolvedAttribute("price", "orders")
    ))
    val initialPlan = Project(List(NamedExpression("*", Star)), tableScan)
    val expectedExpressions = tableScan.attributes.map(attr => NamedExpression(attr.name, attr))
    val expectedPlan = Project(expectedExpressions, tableScan)
    val optimizedPlan = optimizePlan(initialPlan)
    optimizedPlan shouldBe expectedPlan
  }

  test("optimizePlan should resolve UnresolvedAttributes to ResolvedAttributes") {
    val tableScan = TableScan("orders", List(
      ResolvedAttribute("id", "orders"),
      ResolvedAttribute("client", "orders"),
      ResolvedAttribute("timestamp", "orders"),
      ResolvedAttribute("product", "orders"),
      ResolvedAttribute("price", "orders")
    ))
    val initialPlan = Project(List(NamedExpression("client", UnresolvedAttribute("client"))), tableScan)
    val expectedPlan = Project(List(NamedExpression("client", ResolvedAttribute("client", "orders"))), tableScan)
    val optimizedPlan = optimizePlan(initialPlan)
    optimizedPlan shouldBe expectedPlan
  }

  test("optimizePlan should handle UnresolvedAttribute not found") {
  val tableScan = TableScan("orders", List(ResolvedAttribute("id", "orders")))
  val initialPlan = Project(List(NamedExpression("nonExistent", UnresolvedAttribute("nonExistent"))), tableScan)
  
  val thrown = intercept[IllegalArgumentException] {
    optimizePlan(initialPlan)
  }
  
  thrown.getMessage should include("Unresolved attribute")
}

  test("readData should return correct data") {
    val data = readData("testSource")
    data should not be empty
  }

  test("optimizePlan should handle multiple expressions correctly") {
    val tableScan = TableScan("orders", List(
      ResolvedAttribute("id", "orders"),
      ResolvedAttribute("client", "orders")
    ))
    val initialPlan = Project(
      List(NamedExpression("id", ResolvedAttribute("id", "orders")),
           NamedExpression("client", UnresolvedAttribute("client"))),
      tableScan
    )
    val expectedPlan = Project(
      List(NamedExpression("id", ResolvedAttribute("id", "orders")),
           NamedExpression("client", ResolvedAttribute("client", "orders"))),
      tableScan
    )
    val optimizedPlan = optimizePlan(initialPlan)
    optimizedPlan shouldBe expectedPlan
  }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$NoDenotation$.owner(SymDenotations.scala:2582)
	dotty.tools.dotc.core.SymDenotations$SymDenotation.isSelfSym(SymDenotations.scala:714)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:160)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1627)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1629)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1660)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:281)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1668)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:281)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1627)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1629)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1666)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:281)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$13(ExtractSemanticDB.scala:221)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:333)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:221)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1711)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:184)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1627)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1629)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1660)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:281)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1668)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:281)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1627)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1629)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1666)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:281)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1715)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:184)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$11(ExtractSemanticDB.scala:207)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:333)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:207)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1719)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1633)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1762)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:181)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$1(ExtractSemanticDB.scala:145)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:333)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:145)
	scala.meta.internal.pc.SemanticdbTextDocumentProvider.textDocument(SemanticdbTextDocumentProvider.scala:38)
	scala.meta.internal.pc.ScalaPresentationCompiler.semanticdbTextDocument$$anonfun$1(ScalaPresentationCompiler.scala:178)
```
#### Short summary: 

java.lang.AssertionError: NoDenotation.owner