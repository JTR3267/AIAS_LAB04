package acal_lab04.Hw2

import chisel3._
import chisel3.util._
import acal_lab04.Lab._

class Add_Suber extends Module{
  val io = IO(new Bundle{
  val in_1 = Input(UInt(4.W))
	val in_2 = Input(UInt(4.W))
	val op = Input(Bool()) // 0:ADD 1:SUB
	val out = Output(UInt(4.W))
	val o_f = Output(Bool())
  })

  //please implement your code below
  // 實際 in2
  val in2 = Wire(UInt(4.W))
  // 抓 2's complemnt 後 overflow
  val o_f = Wire(UInt(1.W))
  o_f := 0.U

  when(io.op){
    //SUB
    in2 := ~io.in_2 + 1.U
    // -8 做 2's complemnt 後會 overflow
    when(in2(3) & io.in_2(3)){
      o_f := 1.U
    }
  }.otherwise{
    in2 := io.in_2
  }

  val FA_Array = Array.fill(4)(Module(new FullAdder).io)
  val carry = Wire(Vec(5, UInt(1.W)))
  val sum   = Wire(Vec(4, UInt(1.W)))

  carry(0) := 0.U

  for(i <- 0 until 4){
      FA_Array(i).A := io.in_1(i)
      FA_Array(i).B := in2(i)
      FA_Array(i).Cin := carry(i)
      carry(i+1) := FA_Array(i).Cout
      sum(i) := FA_Array(i).Sum
  }

  io.o_f := (carry(3) ^ carry(4)) | o_f
  io.out := sum.asUInt
}