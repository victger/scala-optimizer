id: file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala:[5463..5464) in Input.VirtualFile("file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala", "import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

trait ExecutionPlan
trait Expression
trait Attribute(name: String)

case class UnresolvedAttribute(name: String) extends Attribute(name)
case class ResolvedAttribute(name: String, table: String) extends Attribute(name)

case class NamedExpression(name: String, attribute: Attribute) extends Expression

trait LeafNode extends ExecutionPlan
trait UnaryNode(next: ExecutionPlan) extends ExecutionPlan

trait BinaryNode(left: ExecutionPlan, right: ExecutionPlan) extends ExecutionPlan

case class Project(expression: List[NamedExpression], next: ExecutionPlan) extends UnaryNode(next)

object Project {
  def unapply(project: Project): Option[(List[NamedExpression], ExecutionPlan)] =
    Some((project.expression, project.next))
}



case class Range(start: Int, count: Int, next: ExecutionPlan) extends UnaryNode(next)

/** Represent the WHERE part of a SELECT query (eg. WHERE col1 = 'data' AND col2 < 3) */
case class Filter(expression: Expression, next: ExecutionPlan) extends UnaryNode(next)

case class Union(subquery: List[ExecutionPlan]) extends ExecutionPlan

/** Used when FROM contains more than one table or subquery */
case class Join(left: ExecutionPlan, right: ExecutionPlan) extends BinaryNode(left, right)

/** Represent the ORDER BY of a SELECT query */
case class Sort(order: List[SortOrder], next: ExecutionPlan) extends UnaryNode(next)

enum SortDirection:
  case Descending, Ascending

case class SortOrder(expression: Expression, direction: SortDirection)

/** Represent the FROM part of a SELECT query */
case class TableScan(name: String, output: List[ResolvedAttribute]) extends LeafNode

/** Used when you want to give an alias to a table or a subquery (eg. FROM orders o) */
case class SubQueryAlias(name: String, next: ExecutionPlan) extends UnaryNode(next)


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
      Filter(ResolvedAttribute(innerPlan.toString), innerPlan)

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

def optimizePlan(plan: ExecutionPlan): ExecutionPlan = plan match {
    case Project(expressions, TableScan(name, attributes)) =>
      val optimizedExpressions = expressions.map {
        case NamedExpression("*", Star) =>
          attributes.map(attr => NamedExpression(attr.name, attr))
        case NamedExpression(alias, UnresolvedAttribute(attributeName)) =>
          val resolvedAttr = attributes.find(_.name == attributeName)
            .getOrElse(throw new IllegalArgumentException(s"Unresolved attribute not found: $attributeName"))
          NamedExpression(alias, resolvedAttr)
        // Handle other cases if needed
        case expr => expr
      }.flatten
      Project(optimizedExpressions, TableScan(name, attributes))

    // Handle other cases if needed
    case _ => plan
  }

  // ... (other code)
}

This assumes that you have classes Attribute, NamedExpression, UnresolvedAttribute, and ResolvedAttribute to represent the necessary components of your plan.

Make sure to adapt the code based on your actual class hierarchy and the structure of your Expression and Star classes. Additionally, if there are other cases to handle in the optimizePlan function, you can extend the pattern matching accordingly.
User
I have the following error : Project cannot be used as an extractor in a pattern because it lacks an unapply or unapplySeq methodbloop(127)
ChatGPT

The error you're encountering indicates that the Project case class doesn't automatically provide an unapply or unapplySeq method, which is needed for pattern matching in Scala.

To fix this, you can manually define an unapply method for the Project case class. Here's an example modification:

scala

case class Project(expression: List[NamedExpression], next: ExecutionPlan) extends UnaryNode(next)

object Project {
  def unapply(project: Project): Option[(List[NamedExpression], ExecutionPlan)] =
    Some((project.expression, project.next))
}

By defining the unapply method in the companion object of Project, you provide the necessary pattern matching support. Repeat this process for other case classes if needed.

Here's how the modified SQLOptimizer might look:

scala

object SQLOptimizer {

  // ... (other code)

  object Project {
    def unapply(project: Project): Option[(List[NamedExpression], ExecutionPlan)] =
      Some((project.expression, project.next))
  }

def optimizePlan(plan: ExecutionPlan): ExecutionPlan = plan match {
  case Project(expressions, TableScan(name, attributes)) =>
    val optimizedExpressions = expressions.map {
      case NamedExpression("*", Star) =>
        attributes.map(attr => NamedExpression(attr.name, attr))
      case NamedExpression(alias, UnresolvedAttribute(attributeName)) =>
        val resolvedAttr = attributes.find(_.name == attributeName)
          .getOrElse(throw new IllegalArgumentException(s"Unresolved attribute not found: $attributeName"))
        NamedExpression(alias, resolvedAttr)
      // Handle other cases if needed
      case expr => expr
    }.flatten
    Project(optimizedExpressions, TableScan(name, attributes))

  // Handle other cases if needed
  case _ => plan
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


// Définition de la classe de tests
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
    val optimizedPlan = optimizer.optimizePlan(initialPlan)
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
    val optimizedPlan = optimizer.optimizePlan(initialPlan)
    optimizedPlan shouldBe expectedPlan
  }

  test("optimizePlan should handle UnresolvedAttribute not found") {
    val tableScan = TableScan("orders", List(ResolvedAttribute("id", "orders")))
    val initialPlan = Project(List(NamedExpression("nonExistent", UnresolvedAttribute("nonExistent"))), tableScan)
    val thrown = intercept[IllegalArgumentException] {
      optimizer.optimizePlan(initialPlan)
    }
    thrown.getMessage should include("Unresolved attribute")
  }

  test("readData should return correct data") {
    val data = optimizer.readData("testSource")
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
    val optimizedPlan = optimizer.optimizePlan(initialPlan)
    optimizedPlan shouldBe expectedPlan
  }
}

object Test {
  val sqlQuery = "SELECT id, name FROM source WHERE id = 1 ORDER BY name DESC RANGE 1, 10"
  val plan = optimize(sqlQuery)

  def main(args: Array[String]): Unit = {
    val tests = new ExecutionPlanOptimizerTests()
  }
}")
file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala
file:///C:/Users/yanis/OneDrive/Documents/ESIEE/E5/C-Scala/scala-optimizer/src/main/scala/ExecutionPlanOptimizer.scala:129: error: expected identifier; obtained dot
To fix this, you can manually define an unapply method for the Project case class. Here's an example modification:
                                                                                 ^
#### Short summary: 

expected identifier; obtained dot