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
  val CLA_Array = Array.fill(n)(Module(new CLAdder()).io)
  val carry = Wire(Vec(n+1, UInt(1.W)))
  // 每個 sum 都是 4 bits
  val sum   = Wire(Vec(n, UInt(4.W)))

  carry(0) := io.Cin

  for (i <- 0 until n) {
    // 取 4 bits
    CLA_Array(i).in1 := io.in1(4*i+3, 4*i)
    CLA_Array(i).in2 := io.in2(4*i+3, 4*i)
    CLA_Array(i).Cin := carry(i)
    // 更新 carry
    carry(i+1) := CLA_Array(i).Cout
    sum(i) := CLA_Array(i).Sum
  }

  io.Sum := sum.asUInt
  io.Cout := carry(n)
}