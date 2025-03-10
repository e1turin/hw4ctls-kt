hw.module @counter(in %clk: !seq.clock, out o: i8) {
  %reg = seq.compreg %in, %clk : i8
  %one = hw.constant 1 : i8
  %in = comb.add %reg, %one : i8
  hw.output %reg : i8
}

func.func @entry() {
  %high = seq.const_clock high
  %low = seq.const_clock low

  arc.sim.instantiate @counter as %model {
    arc.sim.set_input %model, "clk" = %high : !seq.clock, !arc.sim.instance<@counter>
    arc.sim.step %model : !arc.sim.instance<@counter>
    arc.sim.set_input %model, "clk" = %low : !seq.clock, !arc.sim.instance<@counter>
    arc.sim.step %model : !arc.sim.instance<@counter>

    %out = arc.sim.get_port %model, "o" : i8, !arc.sim.instance<@counter>

    arc.sim.emit "counter value", %out : i8
  }

  return
}
