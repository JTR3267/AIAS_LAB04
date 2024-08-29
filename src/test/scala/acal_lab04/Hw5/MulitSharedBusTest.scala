package acal_lab04.Lab

import chisel3._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import chisel3.experimental.BundleLiterals._


class MultiShareBusTest(c: MultiShareBus) extends PeekPokeTester(c) {
    // Define address ranges
    val addrMap = Hw_Config.addrMap

    for(t <- 0 until 16) {
        val address = 32768 + rnd.nextInt(20000)
        // Set the master signals
        // for(i <- 0 until c.numMasters) {
            poke(c.io.masters.valid, true.B)
            poke(c.io.masters.bits.addr, address)
            poke(c.io.masters.bits.data, 0.U)
            poke(c.io.masters.bits.size, 0.U) // Adjust size as needed
        // }

        // Step the simulation
        step(1)

        // Check the slave selection
        val expectedSelect = addrMap.zipWithIndex.collect {
            case ((start, size), index) if address >= start && address < (start + size) => index
        }.headOption.getOrElse(-1)

        // Validate the select output
        if (expectedSelect >= 0) {
            expect(c.io.slaves(expectedSelect).valid, true.B)
            expect(c.io.slaves(expectedSelect).bits.addr, address.U)
            expect(c.io.slaves(expectedSelect).bits.data, 0.U)
            expect(c.io.slaves(expectedSelect).bits.size, 0.U) 
        } 

        // Reset the master signals
        poke(c.io.slaves(expectedSelect).ready, true.B)
        step(1)
    }
}

object MultiShareBusTest extends App {
    val addrWidth  = Hw_Config.addr_width
    val dataWidth  = Hw_Config.data_width
    val numMasters = Hw_Config.numMasters
    val numSlaves  = Hw_Config.numSlaves
    val addrMap    = Hw_Config.addrMap
    Driver.execute(Array("-td","./generated"), () => new MultiShareBus(addrWidth, dataWidth,numMasters, numSlaves, addrMap)) {
        c => new MultiShareBusTest(c)
    }
}

object Hw_Config {
  val numMasters = 1 // number of masters
  val numSlaves  = 2 // number of slaves
  val addr_width = 16
  val data_width = 16
  val addrMap = Seq(
      (32768, 10000), 
      (42768, 10000),
    //   (52768, 10000)  
  )
}
