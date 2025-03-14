#include "arcilator-runtime.h"
extern "C" {
void Dut_eval(void* state);
}

class DutLayout {
public:
  static const char *name;
  static const unsigned numStates;
  static const unsigned numStateBytes;
  static const std::array<Signal, 2> io;
  static const Hierarchy hierarchy;
};

const char *DutLayout::name = "Dut";
const unsigned DutLayout::numStates = 5;
const unsigned DutLayout::numStateBytes = 6;
const std::array<Signal, 2> DutLayout::io = {
  Signal{"clk", 0, 1, Signal::Input},
  Signal{"o", 5, 8, Signal::Output},
};

const Hierarchy DutLayout::hierarchy = Hierarchy{"internal", 3, 0, (Signal[]){
    Signal{"clk", 1, 1, Signal::Wire},
    Signal{"reg", 3, 8, Signal::Register},
    Signal{"o", 4, 8, Signal::Wire}
  }, (Hierarchy[]){}};

class DutView {
public:
  uint8_t &clk;
  uint8_t &o;
  struct {
    uint8_t &clk;
    uint8_t &reg;
    uint8_t &o;
  } internal;
  uint8_t *state;

  DutView(uint8_t *state) :
    clk(*(uint8_t*)(state+0)),
    o(*(uint8_t*)(state+5)),
    internal({
      .clk = *(uint8_t*)(state+1),
      .reg = *(uint8_t*)(state+3),
      .o = *(uint8_t*)(state+4)
    }),
    state(state) {}
};

class Dut {
public:
  std::vector<uint8_t> storage;
  DutView view;

  Dut() : storage(DutLayout::numStateBytes, 0), view(&storage[0]) {
  }
  void eval() { Dut_eval(&storage[0]); }
  ValueChangeDump<DutLayout> vcd(std::basic_ostream<char> &os) {
    ValueChangeDump<DutLayout> vcd(os, &storage[0]);
    vcd.writeHeader();
    vcd.writeDumpvars();
    return vcd;
  }
};

#define DUT_PORTS \
  PORT(clk) \
  PORT(o)
