package acal_lab04.Lab

import chisel3._
import chisel3.iotesters.{PeekPokeTester, Driver, ChiselFlatSpec}
import chisel3.experimental.BundleLiterals._


class MultiShareBusTest(c: MultiShareBus) extends PeekPokeTester(c) {
    // Define Configurations
    val addrMap    = Hw_Config.addrMap
    val numMasters = c.numMasters
    val numSlaves  = c.numSlaves
    val mem_range  = 10000
    case class ExpectedValue(address: Int, data: Int)
    val request    = Array.fill(numMasters)(scala.collection.mutable.Queue[ExpectedValue]())
    val answer     = Array.fill(numSlaves, mem_range)((0))
    val receiverd  = Array.fill(numSlaves, mem_range)((0))

    for(t <- 0 until 100) {
        // Set the master signals
        for(i <- 0 until numMasters) {
            val address = 32768 + rnd.nextInt(30000)
            val data    = rnd.nextInt(100)
            request(i).enqueue(ExpectedValue(address, data))
        }

        // Input to design
        while(request.exists(_.nonEmpty)) {
            for(i <- 0 until numMasters) {
                val valid = rnd.nextBoolean() // Randomly set valid signal
                if(request(i).nonEmpty) {
                    val req = request(i).front
                    poke(c.io.masters(i).valid, valid.B)
                    poke(c.io.masters(i).bits.addr, req.address.U)
                    poke(c.io.masters(i).bits.data, req.data.U)
                    poke(c.io.masters(i).bits.size, 0.U)
                    if(valid) {
                        // println(s"Master $i: Address ${req.address} is valid with data ${req.data}")
                        for(j <- 0 until numSlaves){
                            for(k <- 0 until mem_range){
                                if(addrMap(j)._1 <= req.address && req.address < addrMap(j)._1 + addrMap(j)._2) {
                                    answer(j)(k) = req.data.toInt
                                }
                            }
                            
                        }
                    } else{
                        request(i).dequeue()
                    }
                    
                } 
                else {
                    poke(c.io.masters(i).valid, false.B)
                    // request(i).dequeue()
                }
            }
            
            step(1)

            for(j <- 0 until numSlaves) {
                for(k <- 0 until mem_range){
                    if(peek(c.io.slaves(j).valid) == 1) {
                        receiverd(j)(k) = peek(c.io.slaves(j).bits.data).toInt
                        poke(c.io.slaves(j).ready, true.B)
                        // println(s"Slave $j: Address ${peek(c.io.slaves(j).bits.addr)} is ready with data ${peek(c.io.slaves(j).bits.data)}")
                    }
                }
            }

            step(1)

            for(i <- 0 until numMasters){
                if (peek(c.io.masters(i).ready) == 1 && request(i).nonEmpty) {
                    request(i).dequeue()
                }
            }

        }

    }
    var correct = 0
    for (i <- 0 until numSlaves) {
        for (j <- 0 until mem_range) {
            val expectedValue = answer(i)(j)
            val receivedValue = receiverd(i)(j)
            if (expectedValue != receivedValue) {
                println(s"Slave $i: Address $j: Expected $expectedValue, Received $receivedValue")
            } else{
                correct += 1
            }
        }
    }
    if (correct == numSlaves * mem_range) {
        println("Test Passed")
    } else {
        println("Test Failed")
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
  val numMasters = 2 // number of masters
  val numSlaves  = 3 // number of slaves
  val addr_width = 16
  val data_width = 16
  val addrMap = Seq(
      (30000, 10000), 
      (40000, 10000),
      (50000, 10000)  
  )
}
