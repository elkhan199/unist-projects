import hw3._
import hw3._
import scala.collection.immutable.HashMap
import scala.collection.JavaConverters._


class Hw3Test extends org.scalatest.funsuite.AnyFunSuite {
val fibo = """def fibo(x) = 
        if 0 >= x then {
          0
        } else {
          if iszero x-1 then {
            0 
          } 
          else {
            if iszero x-2 then {
              1
            } else {
              (fibo x-1) + (fibo (x-2))
            }
          }
        }"""
  val f = """def f(x) =
          if x >= 0 then {
            1
          } else {
            0
          }"""
  val ptwo = """def ptwo(x) =
    if x > 0 then {
      (ptwo (x-1)) * 2
    } else {
      1
    }"""
  val sqr = """"""
val miniScalaTestCases = List(
      ("1",IntVal(1))
      ,("1 + 1", IntVal(2))
      ,("iszero 1",BoolVal(false))
      ,("if iszero 0 then 1 else 2",IntVal(1))
      ,("{ val x = 1 ; x+1 }",IntVal(2))
      ,("{ var x = 1 ; x+1 }",IntVal(2))
      ,("(x) => x + 10",ProcVal(Var("x"),Add(Var("x"),Const(10)),new Env()))
      ,("((x) => x + 1) 1",IntVal(2))
      ,("{def f(x) = if iszero x then 0 else x + (f x-1) ; f 1}",IntVal(1))
      ,("((x) => x - 8) 5",IntVal(-3))
      ,("{val x = 1 ; {def f(y) = if iszero (x - y) then 0 else 1 ; { val x = 2 ; (f 2)}}}",IntVal(1))
      ,("{var x = 1; (x := 2; x)}", IntVal(2))
      ,("if 0 > 1 then 1 else 2",IntVal(2))
      ,("{val x = 1; if 0 > x then 1 else 2}",IntVal(2))
      ,("""{def x(y) = 
      y;
      (if 0 > (x 1) then 1 else 2)}""",IntVal(2))
      ,(s"{${fibo}; (fibo 0)}",IntVal(0))
      ,(s"{${fibo}; (fibo 10)}",IntVal(34))
//      ,("{var x = 1; x := 2; x; x := 3}",IntVal(0))
      ,("{var x = 1; {x := 2; {x; x := 3}}}",IntVal(3))
      ,("{var a = 1; {def f(x) = a := a + 1; {(f 1); (f 1)}}}",IntVal(3))
      ,("1",IntVal(1))
      ,(s"{var y = 3; {def sq(x) = y := x * x; (sq y)}}", IntVal(9))
//      ,(s"{def sq(x) = y := x * x; {var y = 3; (sq y)}}", IntVal(9))// => fail
//      ,(s"{var x = 3; {def sq(x) = x := x * x; (sq x)}}", IntVal(9))// => fail
      ,(s"{${f}; (f 4)}", IntVal(1))
      ,(s"{${ptwo}; {(ptwo 4); {var a = 10; (ptwo a)}}}", IntVal(1024))
      ,("{var x = 1; {x := 2; {x; x := 3}}}", IntVal(3))
      ,("{var a = 1; {def f(x) = a := a + 1; {(f 1); (f 1)}}}",IntVal(3))
    )
  test("Hw3.parser") {  
    miniScalaTestCases.foreach((a: (String, Val)) => {assert(MiniScalaInterpreter(a._1) === a._2)} )
    assertThrows[Exception] {
      MiniScalaInterpreter("(iszero 1) + 1")
    }
  }

}
