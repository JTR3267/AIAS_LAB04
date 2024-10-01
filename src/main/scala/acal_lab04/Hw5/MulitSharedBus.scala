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
  // record chosen result, init to all false
  val grants = Wire(Vec(numIn, Bool()))
  for (i <- 0 until numIn) {
    grants(i) := false.B
  }
  // init io.in.ready to 0
  for (i <- 0 until numIn) {
    io.in(i).ready := 0.U
  }
  
  // loop and get out index by rr
  for (i <- 0 until numIn) {
    val index = (last_out + numIn.U - i.U) % numIn.U
    when(io.in(index).valid) {
      io.chosen := index
      io.out.bits := io.in(index).bits
      grants(index) := true.B
      last_out := index
    } .otherwise {
      grants(index) := false.B
    }
  }

  // set out.valid to true if at least 1 in is valid
  io.out.valid := grants.asUInt.orR
}

class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface(addrWidth, dataWidth)))
  })

  // round robin arbiter
  val arbiter = Module(new RoundRobinArbiter(new MasterInterface(addrWidth, dataWidth), numMasters))
  // connect masters to rrarbiter
  for (i <- 0 until numMasters) {
    arbiter.io.in(i) <> io.masters(i)
  }
  
  // decoder
  val decoders = Seq.tabulate(numSlaves) { i =>
    Module(new Decoder(addrWidth, addrMap.slice(i, i+1)))
  }

  // Initialize signals
  for (i <- 0 until numSlaves) {
    decoders(i).io.addr := arbiter.io.out.bits.addr
    io.slaves(i).valid := arbiter.io.out.valid && decoders(i).io.select
    io.slaves(i).bits.addr := arbiter.io.out.bits.addr
    io.slaves(i).bits.data := arbiter.io.out.bits.data
    io.slaves(i).bits.size := arbiter.io.out.bits.size
  }

  arbiter.io.out.ready := io.slaves.map(_.ready).reduce(_ || _) // OR all ready signals
  // pass ready signal to the chosen master
  io.masters(arbiter.io.chosen).ready := arbiter.io.out.ready
}