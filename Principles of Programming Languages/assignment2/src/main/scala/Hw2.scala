package Hw2

import fastparse._
import MultiLineWhitespace._
import scala.collection.immutable.HashMap

sealed trait Val
case class IntVal(n: Int) extends Val
case class BoolVal(b: Boolean) extends Val
case class ProcVal(v: Var, expr: Expr, env: Env) extends Val
case class RecProcVal(fv: Var, av: Var, body: Expr, expr: Expr, env: Env) extends Val

case class Env(hashmap: HashMap[Var,Val]) {
  def apply(variable: Var): Val = hashmap(variable)
  def exists(v: Var): Boolean = 
    hashmap.exists((a: (Var, Val)) => a._1 == v)
  def add(v: Var, value: Val) = Env(hashmap + (v -> value))
  
}

sealed trait Program
sealed trait Expr extends Program
case class Const(n: Int) extends Expr
case class Var(s: String) extends Expr
case class Add(l: Expr, r: Expr) extends Expr
case class Sub(l: Expr, r: Expr) extends Expr
case class Iszero(c: Expr) extends Expr
case class Ite(c: Expr, t: Expr, f: Expr) extends Expr
case class Let(name: Var, value: Expr, body: Expr) extends Expr
case class Paren(expr: Expr) extends Expr
case class Proc(v: Var, expr: Expr) extends Expr
case class PCall(ftn: Expr, arg: Expr) extends Expr
case class LetRec(fname: Var, aname: Var, fbody: Expr, ibody: Expr) extends Expr

sealed trait IntExpr
case class IntConst(n: Int) extends IntExpr
case object IntVar extends IntExpr
case class IntAdd(l: IntExpr, r: IntExpr) extends IntExpr
case class IntSub(l: IntExpr, r: IntExpr) extends IntExpr
case class IntMul(l: IntExpr, r: IntExpr) extends IntExpr
case class IntSigma(f: IntExpr, t: IntExpr, b: IntExpr) extends IntExpr
case class IntPow(b: IntExpr, e: IntExpr) extends IntExpr



package object Hw2 {

  

}

object IntInterpreter {
  def evalInt(expr: IntExpr, env: Option[Int]): Int = expr match {
    case IntConst(n) => n
    case IntVar => env match {
      case None => throw new Exception("1")
      case Some(v) => v
    }
    case IntAdd(l,r) => (evalInt(l,env), evalInt(r,env)) match {
      case (v1: Int, v2: Int) => v1+v2
      case _ =>   throw  new Exception("Type Error")
    }
    case IntSub(l,r) => (evalInt(l,env), evalInt(r,env)) match {
      case (v1: Int, v2: Int) => v1-v2
      case _ => throw new Exception("Type Error")
    }
    case IntMul(l,r) => (evalInt(l,env), evalInt(r,env)) match {
      case (v1: Int, v2: Int) => v1*v2
      case _ => throw new Exception("Type Error")
    }
    case IntPow(b,e) => (evalInt(b,env), evalInt(e,env)) match {
      case (v1:Int, v2:Int) => if(v2 == 0) 1 else if (v2>0) evalInt(IntMul(IntPow(b,IntConst(v2-1)),IntConst(v1)),env) else throw new Exception("negative")
      case _ => throw new Exception("Type Error")
    }
    case IntSigma(f,t,b) => (evalInt(f,env), evalInt(t,env)) match {
      case (v1: Int, v2: Int) => if(v1>v2) 0 else evalInt(IntSigma(IntConst(v1+1),t,b),env) + evalInt(b,Some(v1))
      case _ => throw new Exception("Type Error")
    }
    case _ => throw new Exception("Not Defined")
  }

  def apply(s: String): Int = {
    val parsed = IntParser(s)
    evalInt(parsed, None)
  }
}

object LetRecInterpreter {
  
  def eval(env: Env, expr: Expr): Val = expr match{
    case Const(n) => IntVal(n)
    case Var(s) =>
      if (env.exists(Var(s)))
        env(Var(s))
      else throw new Exception("1")
    case Add(a,b) => (eval(env,a), eval(env,b)) match {
      case (x: IntVal, y: IntVal) => IntVal(x.n + y.n)
      case _ => throw new Exception("Type Error")
    }
    case Sub(a,b) => (eval(env,a), eval(env,b)) match {
      case (x: IntVal, y: IntVal) => IntVal(x.n - y.n)
      case _ => throw new Exception("Type Error")
    }
    case Iszero(c) => eval(env,c) match {
      case (x: IntVal) => BoolVal(x.n == 0)
      case _ => BoolVal(false)
    }
    case Ite(c, t, f) => eval(env,c) match {
      case v: BoolVal => if (v.b) eval(env,t) else eval(env,f)
      case _ => throw new Exception("Type Error")
    }
    case Let(name, value, body) => {
        val new_env = env.add(name, eval(env, value))
        eval(new_env, body)
    }
    case Paren(expr: Expr) => eval(env,expr)

    case LetRec(fname,aname,fbody,ibody) => {

      val temp = env.add(fname, RecProcVal(fname, aname, fbody, ibody, env))
      eval(temp, ibody) match {
        case (r: RecProcVal) => {
          RecProcVal(fname, aname, fbody, ibody, temp)
        }
        case _ => {
          eval(temp, ibody)
        }
      }
    }
    case Proc(v,expr) => {
      ProcVal(v,expr,env)
    }
    case PCall(ftn,expr)  => (eval(env,ftn), eval(env,expr)) match {
      case (x: ProcVal, y: IntVal) => eval(x.env,Let(x.v,Const(y.n),x.expr))
      case (x: RecProcVal, y: IntVal) => {
          val temp = Env(env.hashmap.concat(x.env.hashmap))
          eval(temp,Let(x.av,Const(y.n),x.body))
      }
      case _ => throw new Exception("Type Error")
    }
    case _ => throw new Exception("Not Defined")
  }
  
  
  def apply(program: String): Val = {
    val parsed = LetRecParserDriver(program)
    eval(Env(new HashMap[Var,Val]()), parsed)
  }

}

object LetRecToString {
  def apply(expr: Expr): String = expr match {
    case Const(n) => s"${n}"
    case Var(s) => s
    case Add(l,r) =>  s"${apply(l)} + ${apply(r)}"
    case Sub(l,r) => s"${apply(l)} - ${apply(r)}"
    case Iszero(c) => s"iszero ${apply(c)}"
    case Ite(c,t,f) => s"if ${apply(c)} then ${apply(t)} else ${apply(f)}"
    case Let(name,value,body) => s"let ${apply(name)} = ${apply(value)} in ${apply(body)}"
    case Paren(expr) =>   s"(${apply(expr)})"
    case LetRec(fname,aname,fbody,ibody) => s"letrec ${apply(fname)}(${apply(aname)}) = ${apply(fbody)} in ${apply(ibody)}"
    case Proc(v,expr) => s"proc ${apply(v)} ${apply(expr)}"
    case PCall(ftn, expr) => s"${apply(ftn)} ${apply(expr)}"
    case _ => throw new Exception("Not Defined")
  }
}

object Hw2App extends App {
  println("Hello from Hw2!")
  val int_prog = """let x = 6 in letrec f(y) = if iszero (x - y) then 0 else y + (f y-1) in let x = 1 in (f 8)"""
  val int_prog2 = """letrec f(x) = if iszero x then 0 else (letrec g(x) = if iszero(x) then 0 else x + (g x-1) in g x) + (f x-1) in f 10"""
  val int_prog3 = """letrec f(x) = if iszero x then 0 else x + (f x-1) in f 8"""
  val parsed = LetRecParserDriver(int_prog)
  println(parsed)
  val res = LetRecInterpreter.eval(Env(new HashMap[Var,Val]()), parsed);
  print(res)
}