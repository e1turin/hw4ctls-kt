#include "arcilator-runtime.h"
extern "C" {
void counter_eval(void* state);
}

class counterLayout {
public:
  static const char *name;
  static const unsigned numStates;
  static const unsigned numStateBytes;
  static const std::array<Signal, 2> io;
  static const Hierarchy hierarchy;
};

const char *counterLayout::name = "counter";
const unsigned counterLayout::numStates = 2;
const unsigned counterLayout::numStateBytes = 4;
const std::array<Signal, 2> counterLayout::io = {
  Signal{"clk", 0, 1, Signal::Input},
  Signal{"o", 3, 8, Signal::Output},
};

const Hierarchy counterLayout::hierarchy = Hierarchy{"internal", 0, 0, (Signal[]){}, (Hierarchy[]){}};

class counterView {
public:
  uint8_t &clk;
  uint8_t &o;
  struct {} internal;
  uint8_t *state;

  counterView(uint8_t *state) :
    clk(*(uint8_t*)(state+0)),
    o(*(uint8_t*)(state+3)),
    internal({}),
    state(state) {}
};

class counter {
public:
  std::vector<uint8_t> storage;
  counterView view;

  counter() : storage(counterLayout::numStateBytes, 0), view(&storage[0]) {
  }
  void eval() { counter_eval(&storage[0]); }
  ValueChangeDump<counterLayout> vcd(std::basic_ostream<char> &os) {
    ValueChangeDump<counterLayout> vcd(os, &storage[0]);
    vcd.writeHeader();
    vcd.writeDumpvars();
    return vcd;
  }
};

#define COUNTER_PORTS \
  PORT(clk) \
  PORT(o)
