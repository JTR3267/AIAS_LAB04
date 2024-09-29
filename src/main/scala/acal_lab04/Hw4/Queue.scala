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

  // memory space for queue
  val queue_mem      = Mem(depth, UInt(32.W))
  // head of circular queue
  val head           = RegInit(0.U(log2Ceil(depth+1).W))
  // tail of circular queue
  val tail           = RegInit(0.U(log2Ceil(depth+1).W))
  // element count in queue
  val count          = RegInit(0.U(log2Ceil(depth+1).W))
  // io.dataOut
  val out            = RegInit(0.U(32.W))
  // delay io.dataOut update until next io.en
  val pop_occurred   = RegInit(false.B)

  when (io.en) {
    when(pop_occurred) {
      // update io.dataOut
      out := queue_mem((head + depth.asUInt - 1.U) % depth.asUInt)
      pop_occurred := false.B
    }
    when(io.push && (count < depth.asUInt)) {
      // insert to queue tail and update count, tail
      queue_mem(tail) := io.dataIn
      count := count + 1.U
      tail := (tail + 1.U) % depth.asUInt
    } .elsewhen(io.pop && (count > 0.U)) {
      // update count, head
      count := count - 1.U
      head := (head + 1.U) % depth.asUInt
      // update pop_occurred in next cycle
      pop_occurred := RegNext(true.B)
    }
  }

  io.dataOut := out
  io.empty   := count === 0.U
  io.full    := count === depth.asUInt
}