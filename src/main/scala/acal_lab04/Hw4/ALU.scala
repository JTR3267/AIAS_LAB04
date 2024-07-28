package acal_lab04.Hw4

import chisel3._
import chisel3.util._


object ALU_funct3{
  val ADD_SUB = "b000".U
  val SLL     = "b001".U
  val SLT     = "b010".U
  val SLTU    = "b011".U
  val XOR     = "b100".U
  val SRL_SRA = "b101".U
  val OR      = "b110".U
  val AND     = "b111".U
}

object ALU_funct7{
  val SUB_SRA = "b0100000".U
}

import ALU_funct3._,ALU_funct7._,opcode_map._

class ALUIO extends Bundle{
  val src1    = Input(UInt(32.W))
  val src2    = Input(UInt(32.W))
  val funct3  = Input(UInt(3.W))
  val funct7  = Input(UInt(7.W))
  val opcode  = Input(UInt(7.W))
  val out  = Output(UInt(32.W))
}

class ALU extends Module{
  val io = IO(new ALUIO)

  io.out := MuxLookup(io.funct3,0.U,Seq(
    ADD_SUB -> Mux(io.opcode(5),Mux(io.funct7 === SUB_SRA,io.src1 - io.src2,io.src1 + io.src2), io.src1 + io.src2),
    SLL     -> Mux(io.opcode(5),io.src1 << io.src2(4,0),io.src1 << (io.src2(4,0))),
    SLT     -> (io.src1< io.src2),
    SLTU    -> (io.src1.asUInt < io.src2.asUInt),
    XOR     -> (io.src1 ^ io.src2),
    SRL_SRA -> Mux(io.opcode(5),Mux(io.funct7 === SUB_SRA,io.src1 >> io.src2,io.src1 >> (io.src2(4,0))),io.src1 >> (io.src2(4,0))),
    OR      -> (io.src1 | io.src2),
    AND     -> (io.src1 & io.src2)
  ))

}