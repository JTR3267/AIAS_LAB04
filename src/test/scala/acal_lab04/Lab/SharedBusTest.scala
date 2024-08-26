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
        poke(c.io.masters(0).valid, true.B)
        poke(c.io.masters(0).addr, address.U)
        poke(c.io.masters(0).data, 0.U)
        poke(c.io.masters(0).size, 0.U) // Adjust size as needed

        // Step the simulation
        step(1)

        // Check the slave selection
        val expectedSelect = addrMap.zipWithIndex.collect {
            case ((start, size), index) if address >= start && address < (start + size) => index
        }.headOption.getOrElse(-1)

        // Validate the select output
        if (expectedSelect >= 0) {
            expect(c.decoder.io.select, expectedSelect.U)
            expect(c.io.slaves(expectedSelect).valid, true.B)
            expect(c.io.slaves(expectedSelect).addr, address.U)
            expect(c.io.slaves(expectedSelect).data, 0.U)
            expect(c.io.slaves(expectedSelect).size, 0.U) 
        } else {
            expect(c.decoder.io.select, 0.U) // Default case if no match
        }

        // Reset the master signals
        poke(c.io.masters(0).valid, false.B)
        step(1)
    }
}

object SharedBusTest extends App {
    val addrWidth  = Bus_Config.addr_width
    val dataWidth  = Bus_Config.data_width
    val numMasters = Bus_Config.numMasters
    val numSlaves  = Bus_Config.numSlaves
    val addrMap    = Bus_Config.addrMap
    Driver.execute(Array("-td","./generated","-tbn","verilator"), () => new ShareBus(addrWidth, dataWidth, numMasters, numSlaves, addrMap)) {
        c => new ShareBusTest(c)
    }
}

object Bus_Config {
  val numMasters = 1 // number of masters
  val numSlaves  = 2 // number of slaves
  val addr_width = 16
  val data_width = 32
  // allocation of 2 slaves in memory space
  val addrMap = Seq(
    (Integer.parseInt("8000", 16), Integer.parseInt("10000", 16)),
    (Integer.parseInt("10000", 16), Integer.parseInt("20000", 16))
    )
}