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
    val masterReady = Wire(Bool())
    val masterValid = Wire(Bool())
    val masterAddr = Wire(UInt(addrWidth.W))
    val masterData = Wire(UInt(dataWidth.W))
    val masterSize = Wire(UInt(log2Ceil(dataWidth).W))
    
    // decoder
    val decoder = Module(new Decoder(addrWidth, numSlaves, addrMap))

    // Connect master address to decoder
    decoder.io.addr := io.masters(0).addr

    // decoder select slave
    val slaveSelect = WireInit(0.U)
    slaveSelect := decoder.io.select

    // Connect master to wire
    io.masters(0).ready := io.slaves.map(_.ready).reduce(_ || _)
    masterValid := io.masters(0).valid
    masterAddr := io.masters(0).addr
    masterData := io.masters(0).data
    masterSize := io.masters(0).size

    // Connect slave to wire
    io.slaves(slaveSelect).valid := masterValid
    io.slaves(slaveSelect).addr := masterAddr
    io.slaves(slaveSelect).data := masterData
    io.slaves(slaveSelect).size := masterSize

    // Return ready to Master
    for (i <- 0 until numSlaves) {
        io.masters(0).ready := io.slaves.map(_.ready).reduce(_ || _)
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
