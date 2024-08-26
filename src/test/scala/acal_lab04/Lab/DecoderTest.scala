package acal_lab04.Lab

import chisel3._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}

class DecoderTestBench(c: Decoder) extends PeekPokeTester(c) {
    for(t <- 0 until 16){
        val address = 32768 + rnd.nextInt(32768) // Adjust the range to cover both address maps
        var expectedSelect = 0

        if (address >= 32768 && address < 42768) {
            expectedSelect = 0 // Corresponds to the first range
        } else if (address >= 65536 && address < 65536 + 32768) {
            expectedSelect = 1 // Corresponds to the second range
        }
        println(s"Test: $t Address: $address, Expected Select: $expectedSelect\n")
        poke(c.io.addr, address)
        step(1) // Step 1 is enough for the logic to settle
        expect(c.io.select, expectedSelect)
    }
}

object DecoderTest extends App {
  val addrWidth = 16
  val numSlaves = 2
  val addr_map = List(
    (Integer.parseInt("8000", 16), Integer.parseInt("10000", 16)), // Range: 32768 to 42768
    (Integer.parseInt("10000", 16), Integer.parseInt("20000", 16)) // Range: 65536 to 85536
)
  Driver.execute(Array("-td","./generated","-tbn","verilator"),() => new Decoder(addrWidth, numSlaves, addr_map)) {
    c => new DecoderTestBench(c)
  }
}
