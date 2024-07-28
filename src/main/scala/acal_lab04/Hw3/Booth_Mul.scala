package acal_lab04.Hw3

import chisel3._
import chisel3.util._
import scala.annotation.switch

//------------------Radix 4---------------------
class Booth_Mul(width:Int) extends Module {
  val io = IO(new Bundle{
    val in1 = Input(UInt(width.W))      //Multiplicand
    val in2 = Input(UInt(width.W))      //Multiplier
    val out = Output(UInt((2*width).W)) //product
  })
  //please implement your code below
  
  //operation
  val partial_product = Wire(Vec(width/2, UInt((2*width).W)))
  for(i <- 0 until width/2){
    partial_product(i) := 0.U
  }
  //partial product
  val extendedMultiplier = Cat(0.U(1.W), io.in2)

  for(i<-0 until width/2){
    val boothCode = extendedMultiplier(i+2, i)
    when(boothCode === "b001".U || boothCode === "b010".U){
      partial_product(i/2) := io.in1 << i.U
    }
    .elsewhen(boothCode === "b011".U){
      partial_product(i/2) := (io.in1 << 1) << i
    }
    .elsewhen(boothCode === "b100".U){
      partial_product(i/2) := (-(io.in1 << 1)).asUInt << i
    }
    .elsewhen(boothCode === "b101".U || boothCode === "b110".U){
      partial_product(i/2) := (-(io.in1)).asUInt << i
    }
  }
  io.out := partial_product.reduce(_+_)
}


