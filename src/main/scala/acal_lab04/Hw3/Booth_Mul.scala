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
  // signed padding 後的 in1
  val in1 = Wire(UInt((2 * width).W))
  when (io.in1(width - 1)) {
    in1 := Cat(Fill(width, 1.U), io.in1)
  } .otherwise {
    in1 := io.in1
  }
  // out array，需初始化
  val out = Wire(Vec((width + 1) / 2, UInt((2*width).W)))
  for (i <- 0 until ((width + 1) / 2)) {
    out(i) := 0.U
  }

  for (i <- 0 until ((width + 1) / 2)) {
    // 取 3 bits
    val y = Wire(UInt(3.W))
    if (i == 0) {
      // padding 0 到 io.in2(-1)
      y := Cat(io.in2(1, 0), 0.U(1.W))
    } else if ((i == (width + 1) / 2 - 1) && width % 2 == 1) {
      // width 是奇數的話 io.in2(2 * i + 1) 會超過長度限制
      y := Cat(0.U(1.W), io.in2(2 * i, 2 * i - 1))
    } else {
      y := io.in2(2 * i + 1, 2 * i - 1)
    }

    switch (y) {
      is ("b000".U) {
        // 0
        // do nothing
      }
      is ("b001".U) {
        // 1
        out(i) := in1 << (2 * i)
      }
      is ("b010".U) {
        // 1
        out(i) := in1 << (2 * i)
      }
      is ("b011".U) {
        // 2
        out(i) := in1 << (2 * i + 1)
      }
      is ("b100".U) {
        // -2
        out(i) := (~(in1 << 1) + 1.U) << (2 * i)
      }
      is ("b101".U) {
        // -1
        out(i) := (~in1 + 1.U) << (2 * i)
      }
      is ("b110".U) {
        // -1
        out(i) := (~in1 + 1.U) << (2 * i)
      }
      is ("b111".U) {
        // 0
        // do nothing
      }
    }
  }
  // 全部加總
  io.out := out.reduce(_ +& _)
}