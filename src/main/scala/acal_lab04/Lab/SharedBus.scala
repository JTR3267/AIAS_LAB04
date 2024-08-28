package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil

class MasterInterface(val addrWidth: Int, val dataWidth: Int) extends Bundle {
    val valid = Input(Bool())
    val ready = Output(Bool())
    val addr = Input(UInt(addrWidth.W))
    val data = Input(UInt(dataWidth.W))
    val size = Input(UInt((log2Ceil(dataWidth / 8)).W))
    override def clone: MasterInterface = {
      new MasterInterface(addrWidth, dataWidth).asInstanceOf[this.type]
    }
}

class SlaveInterface(val addrWidth: Int, val dataWidth: Int) extends Bundle {
    val valid = Output(Bool())
    val ready = Input(Bool())
    val addr = Output(UInt(addrWidth.W))
    val data = Output(UInt(dataWidth.W))
    val size = Output(UInt((log2Ceil(dataWidth / 8)).W))
    override def clone: SlaveInterface = {
      new SlaveInterface(addrWidth, dataWidth).asInstanceOf[this.type]
    }
}

class Decoder(addrWidth: Int, addrMap: Seq[(Int, Int)]) extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(addrWidth.W))
        val select = Output(Bool())
    })
    val select = addrMap.zipWithIndex.foldLeft(false.B) { case (result, ((startAddress, size), index)) =>
      result || (io.addr >= startAddress.U && io.addr < (startAddress + size).U)
    }
    io.select := select
}

class ShareBus( addrWidth: Int, dataWidth: Int, numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = new MasterInterface(addrWidth, dataWidth)
    val slaves = Vec(numSlaves, new SlaveInterface(addrWidth, dataWidth))
  })
    // decoder
    val decoders = Seq.tabulate(numSlaves) { i =>
      Module(new Decoder(addrWidth, addrMap.slice(i, i+1)))
    }

    // Initialize signals
    for (i <- 0 until numSlaves) {
      io.slaves(i).valid := 0.U
      io.slaves(i).addr  := io.masters.addr
      io.slaves(i).data  := io.masters.data
      io.slaves(i).size  := io.masters.size
      decoders(i).io.addr := io.masters.addr
    }

    io.masters.ready := io.slaves.map(_.ready).reduce(_ || _)

    when(io.masters.valid) {
      for (i <- 0 until numSlaves) {
        when(decoders(i).io.select) {
          io.slaves(i).valid := true.B
        }
      }
    }
}

class test ( addrWidth: Int, dataWidth: Int, val addrMap: Seq[(Int, Int)]) extends Module{
  val io = IO(new Bundle {
    val masters = new MasterInterface(addrWidth, dataWidth)
    val slaves = new SlaveInterface(addrWidth, dataWidth)
  })
  val decoder = Module(new Decoder(addrWidth, addrMap))
  
  // Initialize signals
  io.masters.ready := io.slaves.ready

  io.slaves.valid := decoder.io.select && io.masters.valid
  io.slaves.addr := io.masters.addr
  io.slaves.data := io.masters.data
  io.slaves.size := io.masters.size

  decoder.io.addr := io.masters.addr
}
