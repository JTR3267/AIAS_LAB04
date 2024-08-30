package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil


class MultiShareBus(val addrWidth: Int,val dataWidth: Int,val numMasters: Int,val numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, Flipped(Decoupled(new MasterInterface(addrWidth, dataWidth))))
    val slaves = Vec(numSlaves, Decoupled(new SlaveInterface(addrWidth, dataWidth)))
  })

}