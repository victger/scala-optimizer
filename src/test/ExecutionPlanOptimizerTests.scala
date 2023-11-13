import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

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
      val queryString =
        """
          |SELECT o.id, o.client, p.product_name
          |FROM orders o
          |JOIN products p ON o.product_id = p.id
        """.stripMargin

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
