import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.util.matching.Regex

trait ExecutionPlan
sealed trait AttributeExpression

case object Star extends AttributeExpression

case class UnresolvedAttribute(name: String) extends AttributeExpression

case class ResolvedAttribute(name: String, table: String) extends AttributeExpression

case class NamedExpression(alias: String, attribute: AttributeExpression)

case class TableScan(tableName: String, attributes: List[ResolvedAttribute])

case class Project(expressions: List[NamedExpression], child: TableScan)

trait LeafNode extends ExecutionPlan
trait UnaryNode(next: ExecutionPlan) extends ExecutionPlan

trait BinaryNode(left: ExecutionPlan, right: ExecutionPlan) extends ExecutionPlan


case class Range(start: Int, count: Int, next: ExecutionPlan) extends UnaryNode(next)

case class Filter(expression: AttributeExpression, next: ExecutionPlan) extends UnaryNode(next)

case class Union(subquery: List[ExecutionPlan]) extends ExecutionPlan

case class Join(left: ExecutionPlan, right: ExecutionPlan) extends BinaryNode(left, right)

case class Sort(order: List[SortOrder], next: ExecutionPlan) extends UnaryNode(next)

enum SortDirection:
  case Descending, Ascending

case class SortOrder(expression: AttributeExpression, direction: SortDirection)

case class SubQueryAlias(name: String, next: ExecutionPlan) extends UnaryNode(next)

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
      
      case expr => Some(expr)
    }
    Project(optimizedExpressions, TableScan(name, attributes))

  case null => plan
}

def readData(query: String): (Map[String, String], Map[String, List[String]]) = {
    val tableAliasPattern: Regex = """(?i)FROM\s+(\w+)\s+AS\s+(\w+)""".r
    val tableAliasPattern2: Regex = """(?i)JOIN\s+(\w+)\s+AS\s+(\w+)""".r
    val fieldPattern: Regex = """(?i)\b(\w+)\.\*|\b(\w+)\.(\w+)""".r

    val tableAliasMatches = tableAliasPattern.findAllMatchIn(query)
    val tableAliasMatches2 = tableAliasPattern2.findAllMatchIn(query)
    val tableAliases = tableAliasMatches.map(matchData => matchData.group(2) -> matchData.group(1)).toMap
    val tableAliases2 = tableAliasMatches2.map(matchData => matchData.group(2) -> matchData.group(1)).toMap

    val fieldMatches = fieldPattern.findAllMatchIn(query)
    val tableFields = fieldMatches.foldLeft(Map.empty[String, List[String]]) {
      (acc, matchData) =>
        val tableName = Option(matchData.group(1)).getOrElse(matchData.group(2))
        val fieldName = Option(matchData.group(3)).getOrElse("*")
        acc.updated(tableName, acc.getOrElse(tableName, List()) :+ fieldName)
    }

    (tableAliases++tableAliases2, tableFields)
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

  test("readData should extract table aliases and fields from SQL query") {
  val queryString = "SELECT o.id, o.client, p.product_name FROM orders AS o JOIN products AS p ON o.product_id = p.id"
  val (tableAliases, tableFields) = readData(queryString)

  // Check extracted table aliases
  tableAliases shouldEqual Map("o" -> "orders", "p" -> "products")

  // Check extracted table fields
  tableFields shouldEqual Map(
    "orders" -> List("id", "client"),
    "products" -> List("product_name")
  )
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