hw.module @counter(in %clk: !seq.clock, out o: i8) {
  %reg = seq.compreg %in, %clk : i8
  %one = hw.constant 1 : i8
  %in = comb.add %reg, %one : i8
  hw.output %reg : i8
}

!counter_t = !arc.sim.instance<@counter>

func.func @entry() {
  %high = seq.const_clock high
  %low = seq.const_clock low

  arc.sim.instantiate @counter as %model {
    arc.sim.set_input %model, "clk" = %high : !seq.clock, !counter_t
    arc.sim.step %model : !counter_t
    arc.sim.set_input %model, "clk" = %low : !seq.clock, !counter_t
    arc.sim.step %model : !counter_t

    %out1 = arc.sim.get_port %model, "o" : i8, !counter_t
    arc.sim.emit "counter value", %out1 : i8

    // repeat twice

    arc.sim.set_input %model, "clk" = %high : !seq.clock, !counter_t
    arc.sim.step %model : !counter_t
    arc.sim.set_input %model, "clk" = %low : !seq.clock, !counter_t
    arc.sim.step %model : !counter_t

    arc.sim.set_input %model, "clk" = %high : !seq.clock, !counter_t
    arc.sim.step %model : !counter_t
    arc.sim.set_input %model, "clk" = %low : !seq.clock, !counter_t
    arc.sim.step %model : !counter_t

    %out2 = arc.sim.get_port %model, "o" : i8, !counter_t
    arc.sim.emit "counter value", %out2 : i8
  }

  return
}
