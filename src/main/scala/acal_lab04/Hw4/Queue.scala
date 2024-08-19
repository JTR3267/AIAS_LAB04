package acal_lab04.Lab

import chisel3._
import chisel3.util.log2Ceil

class Queue(val depth: Int) extends Module {
  val io = IO(new Bundle {
    val push    = Input(Bool())
    val pop     = Input(Bool())
    val en      = Input(Bool())
    val dataIn  = Input(UInt(32.W))
    val dataOut = Output(UInt(32.W))
    val empty   = Output(Bool())
    val full    = Output(Bool())
  })

  val queue_mem = Mem(depth, UInt(32.W))
  val head      = RegInit(0.U(log2Ceil(depth+1).W))
  val tail      = RegInit(0.U(log2Ceil(depth+1).W))
  val out       = RegInit(0.U(32.W))

  when (io.en) {
    when(io.push) {
      queue_mem(tail) := io.dataIn
      tail := tail + 1.U
    }.elsewhen(io.pop && (tail > head)) {
      out := queue_mem(head)
      head := head + 1.U
    }
  }
  io.dataOut := out
  io.empty := head === tail
  io.full := tail - head === depth.asUInt
}
