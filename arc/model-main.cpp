#include "arcilator-runtime.h"
#include "model.h"
#include <chrono>
#include <cstddef>
#include <cstdint>
#include <cstring>
#include <fstream>
#include <iostream>
#include <memory>
#include <numeric>

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

  void vcd_start(const char *output_file) {
    vcd_stream.open(output_file);
    model_vcd.reset(new ValueChangeDump<DutLayout>(model.vcd(vcd_stream)));
  }

  void vcd_dump(size_t t) {
    if (model_vcd) {
      model_vcd->time = t;
      model_vcd->writeTimestep(0);
    }
  }

  void eval() {
    auto t_before = std::chrono::high_resolution_clock::now();
    Dut_eval(&model.storage[0]);
    auto t_after = std::chrono::high_resolution_clock::now();
    duration += t_after - t_before;
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

  void set_reset(bool reset) { /* TODO */ }

  void set_clk(bool clock) { model.view.clk = clock; }

  uint8_t get_o() { return model.view.o; }
};

int main(int argc, char *argv[]) {
  std::string vcd_output_file;

  // Process args

  char **extra_args = argv + 1;
  for (char **arg = argv + 1, **argEnd = argv + argc; arg != argEnd; ++arg) {
    if (strcmp(*arg, "-o") == 0) {
      ++arg;
      if (arg == argEnd) {
        std::cerr << "Missing output file name after `-o`";
        return 1;
      }
      vcd_output_file = *arg;
      if (strcmp(*arg, "") == 0) {
        std::cerr << "Empty output file name for option `-o`";
        return 1;
      }
      continue;
    }
    *extra_args++ = *arg; // shift args
  }
  argc = extra_args - argv;

  if (argc != 1) {
    std::cerr << "Bad arguments\n";
    std::cerr << "Usage: " << argv[0] << " [options]\n";
    std::cerr << "options:\n";
    std::cerr << "  -o <VCD>    write trace to <VCD> file\n";
    return 1;
  }

  // Simulation begin

  DutModel model;

  model.vcd_start(vcd_output_file.data());

  for (unsigned i = 0; i < 20; ++i) {
    // model.set_reset(i < 10);
    model.clock();
  }

  std::cout << "dut.o = " << (int)model.get_o() << '\n';
  std::cout << "total time = " << model.duration.count() << '\n';
  std::cout << "VCD have written to file '" << vcd_output_file << "'\n";
}
