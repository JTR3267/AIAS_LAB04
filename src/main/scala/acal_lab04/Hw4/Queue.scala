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

  when (io.en) {
    when(io.pop && (count > 0.U)) {
      // update count, head
      count := count - 1.U
      head := (head + 1.U) % depth.asUInt
      // update out
      when (count === 1.U) {
        // when pop the last one, out = 0
        out := 0.U
      }.otherwise {
        // else out = next one
        out := queue_mem((head + 1.U) % depth.asUInt)
      }
      // push only when io.pop = 0
    } .elsewhen(!io.pop && io.push && (count < depth.asUInt)) {
      // insert to queue tail and update count, tail
      queue_mem(tail) := io.dataIn
      count := count + 1.U
      tail := (tail + 1.U) % depth.asUInt
      // update out when first insert to queue
      when (count === 0.U) {
        out := io.dataIn
      }
    }
  }

  io.dataOut := out
  io.empty   := count === 0.U
  io.full    := count === depth.asUInt
}