package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil

class RoundRobinArbiter[T <: Data](gen: T, numIn: Int) extends Module {
  val io = IO(new ArbiterIO(gen, numIn))

  // record last out index, init to 0
  val last_out = RegInit(0.U(log2Ceil(numIn).W))

  // init chosen, out.bits
  io.chosen := 0.U
  io.out.bits := io.in(0).bits
  // init out.valid to false
  io.out.valid := false.B
  // init io.in.ready to false
  for (i <- 0 until numIn) {
    io.in(i).ready := false.B
  }
  
  // loop and get out index by rr
  for (i <- 0 until numIn) {
    val index = (last_out + numIn.U - i.U) % numIn.U
    // if any io.in is valid, update io.chosen and io.out.valid to true
    when(io.in(index).valid) {
      io.chosen := index
      io.out.valid := true.B
      // record last_out
      last_out := index
    }
  }

  // delay one cycle to update io.in.ready, caused by one cycle delay of io.out.ready update
  io.in(RegNext(io.chosen)).ready := RegNext(io.out.ready)
}

class MyDecoder(addrWidth: Int, addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val addr = Input(UInt(addrWidth.W))
    val select = Output(Bool())
  })
  // init select to false
  io.select := false.B

  // iterate the map
  for ((startAddress, size) <- addrMap) {
    // set select to true if any of the address is in the range
    when(io.addr >= startAddress.U && io.addr < (startAddress + size).U) {
      io.select := true.B
    }
  }
}

class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface(addrWidth, dataWidth)))
  })

  // round robin arbiter
  val arbiter = Module(new RoundRobinArbiter(new MasterInterface(addrWidth, dataWidth), numMasters))
  for (i <- 0 until numMasters) {
    // connect masters' valid, ready to arbiter
    arbiter.io.in(i).valid <> io.masters(i).valid
    arbiter.io.in(i).ready <> io.masters(i).ready
    // set arbiter.io.in.bits to 0, dont care
    arbiter.io.in(i).bits.addr := 0.U
    arbiter.io.in(i).bits.data := 0.U
    arbiter.io.in(i).bits.size := 0.U
  }
  
  // decoder
  val decoders = Seq.tabulate(numSlaves) { i =>
    Module(new MyDecoder(addrWidth, addrMap.slice(i, i+1)))
  }

  // Output dicision mux
  for (i <- 0 until numSlaves) {
    decoders(i).io.addr := io.masters(arbiter.io.chosen).bits.addr
    io.slaves(i).valid := arbiter.io.out.valid && decoders(i).io.select
    io.slaves(i).bits.addr := io.masters(arbiter.io.chosen).bits.addr
    io.slaves(i).bits.data := io.masters(arbiter.io.chosen).bits.data
    io.slaves(i).bits.size := io.masters(arbiter.io.chosen).bits.size
  }

  // set arbiter.io.out.ready to update io.masters(arbiter.io.chosen).ready
  arbiter.io.out.ready := io.slaves.map(_.ready).reduce(_ || _) // OR all ready signals
}