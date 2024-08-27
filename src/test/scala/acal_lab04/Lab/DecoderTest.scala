package acal_lab04.Lab

import chisel3._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class DecoderTestBench(c: Decoder) extends PeekPokeTester(c) {
    for(t <- 0 until 16){
        val address = 32768 + rnd.nextInt(20000) // Adjust the range to cover both address maps
        var expectedSelect = 0

        if (address >= 32768 && address < 42768) {
            expectedSelect = 0 // Corresponds to the first range
        } else if (address >= 42768 && address < 52768) {
            expectedSelect = 1 // Corresponds to the second range
        }
        poke(c.io.addr, address)
        val actualSelect = peek(c.io.select).toInt
        step(1) // Step 1 is enough for the logic to settle
        println(s"Test: $t Address: $address, Expected Select: $expectedSelect, Selected: $actualSelect")
        expect(c.io.select, expectedSelect)
    }
}

object DecoderTest extends App {
  val addrWidth = 16
  val numSlaves = 2
  val addr_map = Seq(
      (32768, 10000), // Range 1: 32768 to 42768
      (42768, 10000)  // Range 2: 42768 to 52768
  )
  Driver.execute(Array("-td","./generated","-tbn","verilator"),() => new Decoder(addrWidth, numSlaves, addr_map)) {
    c => new DecoderTestBench(c)
  }
}
