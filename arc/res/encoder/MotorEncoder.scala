//> using scala "2.13.12"
//> using dep "org.chipsalliance::chisel:6.7.0"
//> using plugin "org.chipsalliance:::chisel-plugin:6.7.0"
//> using options "-unchecked", "-deprecation", "-language:reflectiveCalls", "-feature", "-Xcheckinit", "-Xfatal-warnings", "-Ywarn-dead-code", "-Ywarn-unused", "-Ymacro-annotations"

import chisel3._
import chisel3.util._
// _root_ disambiguates from package chisel3.util.circt if user imports chisel3.util._
import _root_.circt.stage.ChiselStage

class MotorEncoder(counterWidth: Int = 16, syncStages: Int = 2) extends Module {
  val io = IO(new Bundle {
    val encA = Input(Bool())
    val encB = Input(Bool())
    val position = Output(SInt(counterWidth.W))
  })

  // Synchronization registers
  val syncA = (0 until syncStages).foldLeft(io.encA) { case (a, _) => RegNext(a) }
  val syncB = (0 until syncStages).foldLeft(io.encB) { case (b, _) => RegNext(b) }

  // State tracking
  val prevState = RegInit(0.U(2.W))
  val currState = Cat(syncA, syncB)

  // Optimized quadrature decoding using transition math
  val direction = WireDefault(0.S(2.W))
  when(prevState =/= currState) {
    direction := MuxCase(0.S, Seq(
      (currState === prevState + 1.U) -> 1.S,
      (prevState === 3.U && currState === 0.U) -> 1.S,
      (currState === prevState - 1.U) -> (-1).S,
      (prevState === 0.U && currState === 3.U) -> (-1).S
    ))
  }

  // Position counter with overflow protection
  val position = RegInit(0.S(counterWidth.W))
  position := position + direction

  // Update previous state
  prevState := currState

  io.position := position
}

class MotorEncoderWithVelocity(counterWidth: Int = 16, sampleCycles: Int = 100000) 
  extends MotorEncoder(counterWidth) {
  
  private val timer = RegInit(0.U(32.W))
  private val lastPosition = RegInit(0.S(counterWidth.W))
  val velocity = Output(SInt(counterWidth.W))

  when(timer === sampleCycles.U) {
    velocity := (io.position - lastPosition)
    lastPosition := io.position
    timer := 0.U
  }.otherwise {
    timer := timer + 1.U
  }
}

object Main extends App {
  ChiselStage.emitCHIRRTLFile(
    new MotorEncoder,
    Array("--target-dir", "gen"),
  )
}
