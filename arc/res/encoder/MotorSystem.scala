//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.7.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.7.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ywarn-unused", "-Ymacro-annotations"

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage

class QuadratureEncoder(counterWidth: Int = 16) extends Module {
  val io = IO(new Bundle {
    val encA = Input(Bool())
    val encB = Input(Bool())
    val position = Output(SInt(counterWidth.W))
    val velocity = Output(SInt(counterWidth.W))
  })

  // Synchronization registers
  val syncA = RegNext(RegNext(io.encA))
  val syncB = RegNext(RegNext(io.encB))

  // State tracking
  val prevState = RegInit(0.U(2.W))
  val currState = Cat(syncA, syncB)

  // Direction detection with corrected operators
  val direction = WireDefault(0.S(2.W))
  when(prevState =/= currState) {
    direction := MuxCase(0.S, Seq(
      ((currState === prevState + 1.U) | 
       (prevState === 3.U & currState === 0.U)) -> 1.S,
      ((currState === prevState - 1.U) | 
       (prevState === 0.U & currState === 3.U)) -> (-1).S
    ))
  }

  // Position counter
  val positionReg = RegInit(0.S(counterWidth.W))
  positionReg := positionReg + direction
  io.position := positionReg

  // Velocity calculation
  val velocityReg = RegInit(0.S(counterWidth.W))
  val timer = RegInit(0.U(32.W))
  val sampleInterval = 1000000.U // Adjust as needed

  when(timer === sampleInterval) {
    velocityReg := positionReg - RegNext(positionReg)
    timer := 0.U
  }.otherwise {
    timer := timer + 1.U
  }
  io.velocity := velocityReg

  // Update previous state
  prevState := currState
}

// Top module with example usage
class MotorSystem extends Module {
  val io = IO(new Bundle {
    val encA = Input(Bool())
    val encB = Input(Bool())
    val position = Output(SInt(16.W))
    val velocity = Output(SInt(16.W))
  })

  val encoder = Module(new QuadratureEncoder())
  
  encoder.io.encA := io.encA
  encoder.io.encB := io.encB
  io.position := encoder.io.position
  io.velocity := encoder.io.velocity
}


// class EncoderTest extends AnyFlatSpec with ChiselScalatestTester {
//   "Encoder" should "track position correctly" in {
//     test(new QuadratureEncoder(16)) { dut =>
//       def pulse(a: Boolean, b: Boolean): Unit = {
//         dut.io.encA.poke(a.B)
//         dut.io.encB.poke(b.B)
//         dut.clock.step()
//       }

//       // Simulate clockwise rotation
//       pulse(false, false)
//       pulse(true,  false)
//       pulse(true,  true)
//       pulse(false, true)
//       pulse(false, false)
      
//       dut.io.position.expect(4.S) // 4 steps forward
//     }
//   }
// }


object Main extends App {
  ChiselStage.emitCHIRRTLFile(
    new MotorSystem,
    Array("--target-dir", "gen"),
  )
}
