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

  val fullAdder = Seq.fill(4)(Module(new FullAdder).io) //full adder

  val carry = Wire(Vec(5, UInt(1.W))) //carry out 
  val sum = Wire(Vec(4, UInt(1.W))) //sum

  carry(0) := io.op //carry in 

  for(i <- 0 until 4){ //4bit adder
    fullAdder(i).A := io.in_1(i)
    fullAdder(i).B := io.in_2(i) ^ io.op //XOR
    fullAdder(i).Cin := carry(i)
    carry(i+1) := fullAdder(i).Cout
    sum(i) := fullAdder(i).Sum
  }

  io.out := sum.asUInt //output
  io.o_f := carry(4) ^ carry(3) //overflow flag

}
