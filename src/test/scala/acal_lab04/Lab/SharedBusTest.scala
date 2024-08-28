package acal_lab04.Lab

import chisel3._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import chisel3.experimental.BundleLiterals._


class ShareBusTest(c: ShareBus) extends PeekPokeTester(c) {
    // Define address ranges
    val addrMap = Bus_Config.addrMap

    for(t <- 0 until 16) {
        val address = 32768 + rnd.nextInt(20000)
        // Set the master signals
        poke(c.io.masters.valid, true.B)
        poke(c.io.masters.addr, address)
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
            expect(c.io.slaves(expectedSelect).valid, true.B)
            expect(c.io.slaves(expectedSelect).addr, address.U)
            expect(c.io.slaves(expectedSelect).data, 0.U)
            expect(c.io.slaves(expectedSelect).size, 0.U) 
        } 

        // Reset the master signals
        poke(c.io.slaves(expectedSelect).ready, true.B)
        step(1)
    }
}

object SharedBusTest extends App {
    val addrWidth  = Bus_Config.addr_width
    val dataWidth  = Bus_Config.data_width
    val numMasters = Bus_Config.numMasters
    val numSlaves  = Bus_Config.numSlaves
    val addrMap    = Bus_Config.addrMap
    Driver.execute(Array("-td","./generated"), () => new ShareBus(addrWidth, dataWidth, numSlaves, addrMap)) {
        c => new ShareBusTest(c)
    }
}

object Bus_Config {
  val numMasters = 1 // number of masters
  val numSlaves  = 2 // number of slaves
  val addr_width = 16
  val data_width = 16
  // allocation of 2 slaves in memory space
  val addrMap = Seq(
      (32768, 10000), // Range 1: 32768 to 42768
      (42768, 10000)  // Range 2: 42768 to 52768
  )
}

class TestTest(c: test) extends PeekPokeTester(c) {
    val addrMap = Seq(
      (32768, 10000), // Range 1: 32768 to 42768
    )
    for(t <- 0 until 16) {
        val address = 32768 + rnd.nextInt(20000)
        poke(c.io.masters.valid, true.B)
        poke(c.io.masters.addr, address)
        poke(c.io.masters.data, 0.U)
        poke(c.io.masters.size, 0.U)
        step(1)
        expect(c.io.slaves.valid, addrMap.exists { case (start, size) => address >= start && address < (start + size) })
        expect(c.io.slaves.addr, address)
        poke(c.io.slaves.ready, true.B)
        step(1)
    }
}

object TestTest extends App {
    val addrMap = Seq(
      (32768, 10000), // Range 1: 32768 to 42768
    )
    val addrWidth = 16
    val dataWidth = 16
    Driver.execute(Array("-td","./generated","-tbn","verilator"), () => new test(addrWidth, dataWidth, addrMap)) {
        c => new TestTest(c)
    }
}