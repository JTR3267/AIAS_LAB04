package acal_lab04.Lab

import chisel3._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class ShareBusTest(c: ShareBus) extends PeekPokeTester(c) {
    // Define address ranges
    val addrMap = Bus_Config.addrMap

    for (t <- 0 until 16) {
        val address = 32768 + rnd.nextInt(32768)
        var expectedSelect = 0
        // Set the master signals
        poke(c.io.masters.valid, true.B)
        poke(c.io.masters.addr,  address.U)
        poke(c.io.masters.data,  0.U)
        poke(c.io.masters.size,  0.U) // Adjust size as needed

        // Step the simulation
        step(1)

        // Check the slave selection
        if (address >= 32768 && address < 42768) {
            expectedSelect = 0 // Corresponds to the first range
        } else if (address >= 65536 && address < 65536 + 32768) {
            expectedSelect = 1 // Corresponds to the second range
        }

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
    Driver.execute(Array("-td","./generated","-tbn","verilator","--full-stacktrace"), () => new ShareBus(addrWidth, dataWidth, numMasters, numSlaves, addrMap)) {
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