package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil


class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface(addrWidth, dataWidth)))
  })
    // decoder
    val decoders = Seq.tabulate(numSlaves) { i =>
      Module(new Decoder(addrWidth, addrMap.slice(i, i+1)))
    }
    // arbiter
    val arbiter = Module(new Arbiter(new MasterInterface(addrWidth, dataWidth),numMasters))

    // Connect masters to arbiter
    arbiter.io.in <> io.masters

    // Connect arbiter to slaves
    val select = arbiter.io.out.bits.addr
    val chosen = arbiter.io.chosen
    arbiter.io.out.ready := io.slaves.map(_.ready).reduce(_ || _) // OR all ready signals

    // Initialize signals
    for (i <- 0 until numSlaves) {
      io.slaves(i).valid := io.masters(chosen).valid && decoders(i).io.select
      io.slaves(i).bits.addr  := io.masters(chosen).bits.addr
      io.slaves(i).bits.data  := io.masters(chosen).bits.data
      io.slaves(i).bits.size  := io.masters(chosen).bits.size
      decoders(i).io.addr := io.masters(chosen).bits.addr
    }

}