package acal_lab04.Hw4

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile


object wide {
  val Byte = "b000".U
  val Half = "b001".U
  val Word = "b010".U
  val UByte = "b100".U
  val UHalf = "b101".U 
}

import wide._

class DataMem extends Module {
  val io = IO(new Bundle {
    val funct3 = Input(UInt(32.W))
    val raddr = Input(UInt(10.W))
    val rdata = Output(SInt(32.W))

    val wen   = Input(Bool())
    val waddr = Input(UInt(10.W))
    val wdata = Input(UInt(32.W))
  })

  val memory = Mem(32, UInt(8.W))
  loadMemoryFromFile(memory, "./src/main/resource/DataMem.txt")

  io.rdata := 0.S

  val wa = WireDefault(0.U(10.W)) //address
  val wd = WireDefault(0.U(32.W)) //data

  wa := MuxLookup(io.funct3,0.U(10.W),Seq(
    Byte -> io.waddr,
    Half -> Cat(io.waddr(9,1),"b0".U), // needs to be changed
    Word -> Cat(io.waddr(9,2),"b00".U), // needs to be changed
  ))

  wd   := MuxLookup(io.funct3,0.U,Seq(
    Byte -> io.wdata(7,0), // needs to be changed
    Half -> io.wdata(15,0), // needs to be changed
    Word -> io.wdata,
  ))

  when(io.wen){ //STORE
    when(io.funct3===Byte){
      memory(wa) := wd(7,0)
    }.elsewhen(io.funct3===Half){
      memory(wa) := wd(15,0)
    }.elsewhen(io.funct3===Word){
      memory(wa) := wd
    }
  }.otherwise{ //LOAD
    io.rdata := MuxLookup(io.funct3,0.S,Seq(
      Byte -> memory(io.raddr).asSInt,
      Half -> memory(io.raddr(7,0)).asSInt, // needs to be changed
      Word -> memory(io.raddr).asSInt, // needs to be changed
      UByte -> memory(io.raddr).asSInt, // needs to be changed
      UHalf -> memory(io.raddr).asSInt // needs to be changed
    ))
  }
}