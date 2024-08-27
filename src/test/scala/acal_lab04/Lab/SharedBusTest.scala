package acal_lab04.Lab

import chisel3._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class ShareBusTest(c: ShareBus) extends PeekPokeTester(c) {
    // Define address ranges
    val addrMap = Bus_Config.addrMap

    // Test addresses for both slaves
    val testAddresses = Seq(0x8000, 0x20000)

    for (address <- testAddresses) {
        // Set the master signals
        poke(c.io.masters.valid, true.B)
        poke(c.io.masters.addr, address.U)
        poke(c.io.masters.data, 0.U)
        poke(c.io.masters.size, 0.U) // Adjust size as needed

        // Step the simulation
        step(1)

        // Check the slave selection
        val expectedSelect = addrMap.zipWithIndex.collect {
            case ((start, size), index) if address >= start && address < (start + size) => index
        }.headOption.getOrElse(-1)

        // Validate the select output
        if (expectedSelect >= 0) {
            expect(c.decoder.io.select, expectedSelect.U)
            expect(c.io.slaves.valid, true.B)
            expect(c.io.slaves.addr, address.U)
            expect(c.io.slaves.data, 0.U)
            expect(c.io.slaves.size, 0.U) 
        } else {
            expect(c.decoder.io.select, 0.U) // Default case if no match
        }

        // Reset the master signals
        poke(c.io.masters.valid, false.B)
        step(1)
    }
}

object SharedBusTest extends App {
    val addrWidth  = Bus_Config.addr_width
    val dataWidth  = Bus_Config.data_width
    val numMasters = Bus_Config.numMasters
    val numSlaves  = Bus_Config.numSlaves
    val addrMap    = Bus_Config.addrMap
    Driver.execute(Array("-td","./generated","-tbn","verilator"), () => new ShareBus(addrWidth, dataWidth, numSlaves, addrMap)) {
        c => new ShareBusTest(c)
    }
}

object Bus_Config {
  val numMasters = 1 // number of masters
  val numSlaves  = 1 // number of slaves
  val addr_width = 16
  val data_width = 16
  // allocation of 2 slaves in memory space
  val addrMap = Seq(
      (32768, 10000), // Range 1: 32768 to 42768
      (42768, 10000)  // Range 2: 42768 to 52768
  )
}