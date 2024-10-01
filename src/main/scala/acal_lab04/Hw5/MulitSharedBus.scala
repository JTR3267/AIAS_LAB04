package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil


class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface(addrWidth, dataWidth)))
  })

  // round robin arbiter
  val arbiter = Module(new RRArbiter(new MasterInterface(addrWidth, dataWidth), numMasters))
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
  for (i <- 0 until numMasters) {
    when (arbiter.io.chosen === i.U) {
      io.masters(i).ready := arbiter.io.out.ready
    } .otherwise {
      io.masters(i).ready := 0.U // 不 set 0 會錯
    }
  }
}