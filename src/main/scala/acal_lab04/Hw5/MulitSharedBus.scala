package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil


class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    // val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val masters = Flipped(Decoupled(new MasterInterface_(addrWidth, dataWidth)))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface_(addrWidth, dataWidth)))
  })
    // decoder
    val decoders = Seq.tabulate(numSlaves) { i =>
      Module(new Decoder(addrWidth, addrMap.slice(i, i+1)))
    }
    // arbiter
    // val arbiter = Module(new Arbiter(numSlaves))
    // Initialize signals
    for (i <- 0 until numSlaves) {
      io.slaves(i).valid := io.masters.valid && decoders(i).io.select
      io.slaves(i).bits.addr  := io.masters.bits.addr
      io.slaves(i).bits.data  := io.masters.bits.data
      io.slaves(i).bits.size  := io.masters.bits.size
      decoders(i).io.addr := io.masters.bits.addr
    }

    io.masters.ready := io.slaves.map(_.ready).reduce(_ || _) // OR all ready signals
}