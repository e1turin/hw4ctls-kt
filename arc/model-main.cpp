#include "arcilator-runtime.h"
#include "model.h"
#include <chrono>
#include <cstddef>
#include <cstdint>
#include <fstream>
#include <iostream>
#include <memory>

class DutModel {
  Dut model;
  std::ofstream vcd_stream;
  std::unique_ptr<ValueChangeDump<DutLayout>> model_vcd;

public:
#define PORT(name) #name,
  static constexpr const char *PORT_NAMES[] = {DUT_PORTS};
#undef PORT
  static constexpr size_t NUM_PORTS = sizeof(PORT_NAMES) / sizeof(*PORT_NAMES);
  using Ports = std::array<uint64_t, NUM_PORTS>;

  const char *name = "unknown";
  size_t half_cycle = 0;
  std::chrono::high_resolution_clock::duration duration =
      std::chrono::high_resolution_clock::duration::zero();

  Ports get_ports() {
#define PORT(name) model.view.name,
    return {DUT_PORTS};
#undef PORT
  }

  void vcd_start(const char *of) {
    vcd_stream.open(of);
    model_vcd.reset(new ValueChangeDump<DutLayout>(model.vcd(vcd_stream)));
  }

  void vcd_dump(size_t t) {
    if (model_vcd) {
      model_vcd->time = t;
      model_vcd->writeTimestep(0);
    }
  }

  void clock() {
    // posedge
    vcd_dump(half_cycle);
    ++half_cycle;
    set_clk(true);
    eval();

    // negedge
    vcd_dump(half_cycle);
    ++half_cycle;
    set_clk(false);
    eval();
  }

  void eval() {
    auto t_before = std::chrono::high_resolution_clock::now();
    Dut_eval(&model.storage[0]);
    auto t_after = std::chrono::high_resolution_clock::now();
    duration += t_after - t_before;
  }

  void set_reset(bool reset) { /* TODO */ }

  void set_clk(bool clock) { model.view.clk = clock; }

  uint8_t get_o() { return model.view.o; }
};

int main(int argc, char *argv[]) {

  DutModel model;

  model.vcd_start("model.vcd");

  for (unsigned i = 0; i < 20; ++i) {
    // model.set_reset(i < 10);
    model.clock();
  }

  std::cout << "dut o = " << (int)model.get_o() << std::endl;
  std::cout << "total time = " << model.duration.count() << std::endl;
}
