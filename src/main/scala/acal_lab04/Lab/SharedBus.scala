package acal_lab04.Lab

import chisel3._
import chisel3.util._
import chisel3.util.log2Ceil

class ShareBus( addrWidth: Int, dataWidth: Int, numMasters: Int, numSlaves: Int, val addrMap: Seq[(Int, Int)]) extends Module {
  val io = IO(new Bundle {
    val masters = Vec(numMasters, new MasterInterface(addrWidth, dataWidth))
    val slaves = Vec(numSlaves, new SlaveInterface(addrWidth, dataWidth))
  })
    // Wire define
    val masterReady = WireInit(false.B)
    val masterValid = WireInit(false.B)
    val masterAddr = WireInit(0.U(addrWidth.W))
    val masterData = WireInit(0.U(dataWidth.W))
    val masterSize = WireInit(0.U(log2Ceil(dataWidth).W))
    
    // decoder
    val decoder = Module(new Decoder(addrWidth, numSlaves, addrMap))
    
    // Initialize signals
    for (i <- 0 until numSlaves) {
      io.slaves(i).valid := false.B
      io.slaves(i).addr  := 0.U
      io.slaves(i).data  := 0.U
      io.slaves(i).size  := 0.U
      io.slaves(i).ready := true.B
    }
    for (i <- 0 until numMasters){
      io.masters(i).ready := false.B
      io.masters(i).addr  := 0.U
      io.masters(i).data  := 0.U
      io.masters(i).size  := 0.U
      io.masters(i).valid := false.B
    }

    // Connect master address to decoder
    decoder.io.addr := io.masters(0).addr

    // decoder select slave
    val slaveSelect = WireInit(0.U)
    slaveSelect := decoder.io.select

    // Connect master to wire
    io.masters(0).ready := io.slaves.map(_.ready).reduce(_ || _)
    masterValid := io.masters(0).valid
    masterAddr  := io.masters(0).addr
    masterData  := io.masters(0).data
    masterSize  := io.masters(0).size
    
    // Connect slave to wire
    when(masterValid) {
      io.slaves(slaveSelect).valid := masterValid
      io.slaves(slaveSelect).addr  := masterAddr
      io.slaves(slaveSelect).data  := masterData
      io.slaves(slaveSelect).size  := masterSize
    }

}

class MasterInterface(addrWidth: Int, dataWidth: Int) extends Bundle {
    val valid = Output(Bool())
    val ready = Input(Bool())
    val addr = Output(UInt(addrWidth.W))
    val data = Output(UInt(dataWidth.W))
    val size = Output(UInt(log2Ceil(dataWidth).W))
}

class SlaveInterface(addrWidth: Int, dataWidth: Int) extends Bundle {
    val valid = Output(Bool())
    val ready = Input(Bool())
    val addr = Output(UInt(addrWidth.W))
    val data = Output(UInt(dataWidth.W))
    val size = Output(UInt(log2Ceil(dataWidth).W))
}

class Decoder(addrWidth: Int, numSlaves: Int, addrMap: Seq[(Int, Int)]) extends Module {
    val io = IO(new Bundle {
        val addr = Input(UInt(addrWidth.W))
        val select = Output(UInt(addrMap.length.W))
    })
    val select = MuxCase(0.U, addrMap.zipWithIndex.map { case ((startAddress, size), index) =>
        (io.addr >= startAddress.U && io.addr < (startAddress + size).U) -> index.U
      }
    )
    io.select := select
}
