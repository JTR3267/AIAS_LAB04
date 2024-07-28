package acal_lab04.Hw1

import chisel3._
import acal_lab04.Lab._

class MixAdder (n:Int) extends Module{
  val io = IO(new Bundle{
      val Cin = Input(UInt(1.W))
      val in1 = Input(UInt((4*n).W))
      val in2 = Input(UInt((4*n).W))
      val Sum = Output(UInt((4*n).W))
      val Cout = Output(UInt(1.W))
  })
  //please implement your code below
  val cladders = Seq.fill(n)(Module(new CLAdder).io) // n CLAdders
  
  val carry = Wire(Vec(n+1, UInt(1.W))) // n+1 carry bits
  val sum = Wire(Vec(n, UInt(4.W))) // n sum bits
  
  carry(0) := io.Cin // initial carry bit
  
  for(i <- 0 until n) { // connect the CLAdders
    cladders(i).in1 := io.in1(4*i+3, 4*i)
    cladders(i).in2 := io.in2(4*i+3, 4*i)
    cladders(i).Cin := carry(i)
    
    carry(i+1) := cladders(i).Cout
    sum(i) := cladders(i).Sum
  }
  
  io.Sum := sum.asUInt // output sum
  io.Cout := carry(n) // output carry
}