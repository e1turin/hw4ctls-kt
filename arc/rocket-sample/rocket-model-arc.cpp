#include "rocket-arc.h"
#include "rocket-model.h"
#include <fstream>
#include <optional>

namespace {
class ArcilatorRocketModel : public RocketModel {
  RocketSystem model;
  std::ofstream vcd_stream;
  std::unique_ptr<ValueChangeDump<RocketSystemLayout>> model_vcd;

public:
  ArcilatorRocketModel() { name = "arcs"; }

  void vcd_start(const char *outputFile) override {
    vcd_stream.open(outputFile);
    model_vcd.reset(
        new ValueChangeDump<RocketSystemLayout>(model.vcd(vcd_stream)));
  }

  void vcd_dump(size_t t) override {
    if (model_vcd) {
      model_vcd->time = t;
      model_vcd->writeTimestep(0);
    }
  }

  void eval() override { RocketSystem_eval(&model.storage[0]); }

  Ports get_ports() override {
    return {
#define PORT(name) model.view.name,
#include "ports.def"
    };
  }

  void set_reset(bool reset) override { model.view.reset = reset; }

  void set_clock(bool clock) override { model.view.clock = clock; }

  void set_mem(AxiInputs &in) override {
    model.view.mem_axi4_0_aw_ready = in.aw_ready;
    model.view.mem_axi4_0_w_ready = in.w_ready;
    model.view.mem_axi4_0_b_valid = in.b_valid;
    model.view.mem_axi4_0_b_bits_id = in.b_id;
    model.view.mem_axi4_0_b_bits_resp = in.b_resp;
    model.view.mem_axi4_0_ar_ready = in.ar_ready;
    model.view.mem_axi4_0_r_valid = in.r_valid;
    model.view.mem_axi4_0_r_bits_id = in.r_id;
    model.view.mem_axi4_0_r_bits_data = in.r_data;
    model.view.mem_axi4_0_r_bits_resp = in.r_resp;
    model.view.mem_axi4_0_r_bits_last = in.r_last;
  }

  AxiOutputs get_mem() override {
    AxiOutputs out;
    out.aw_valid = model.view.mem_axi4_0_aw_valid;
    out.aw_id = model.view.mem_axi4_0_aw_bits_id;
    out.aw_addr = model.view.mem_axi4_0_aw_bits_addr;
    out.aw_len = model.view.mem_axi4_0_aw_bits_len;
    out.aw_size = model.view.mem_axi4_0_aw_bits_size;
    out.w_valid = model.view.mem_axi4_0_w_valid;
    out.w_data = model.view.mem_axi4_0_w_bits_data;
    out.w_strb = model.view.mem_axi4_0_w_bits_strb;
    out.w_last = model.view.mem_axi4_0_w_bits_last;
    out.b_ready = model.view.mem_axi4_0_b_ready;
    out.ar_valid = model.view.mem_axi4_0_ar_valid;
    out.ar_id = model.view.mem_axi4_0_ar_bits_id;
    out.ar_addr = model.view.mem_axi4_0_ar_bits_addr;
    out.ar_len = model.view.mem_axi4_0_ar_bits_len;
    out.ar_size = model.view.mem_axi4_0_ar_bits_size;
    out.r_ready = model.view.mem_axi4_0_r_ready;
    return out;
  }

  void set_mmio(AxiInputs &in) override {
    model.view.mmio_axi4_0_aw_ready = in.aw_ready;
    model.view.mmio_axi4_0_w_ready = in.w_ready;
    model.view.mmio_axi4_0_b_valid = in.b_valid;
    model.view.mmio_axi4_0_b_bits_id = in.b_id;
    model.view.mmio_axi4_0_b_bits_resp = in.b_resp;
    model.view.mmio_axi4_0_ar_ready = in.ar_ready;
    model.view.mmio_axi4_0_r_valid = in.r_valid;
    model.view.mmio_axi4_0_r_bits_id = in.r_id;
    model.view.mmio_axi4_0_r_bits_data = in.r_data;
    model.view.mmio_axi4_0_r_bits_resp = in.r_resp;
    model.view.mmio_axi4_0_r_bits_last = in.r_last;
  }

  AxiOutputs get_mmio() override {
    AxiOutputs out;
    out.aw_valid = model.view.mmio_axi4_0_aw_valid;
    out.aw_id = model.view.mmio_axi4_0_aw_bits_id;
    out.aw_addr = model.view.mmio_axi4_0_aw_bits_addr;
    out.aw_len = model.view.mmio_axi4_0_aw_bits_len;
    out.aw_size = model.view.mmio_axi4_0_aw_bits_size;
    out.w_valid = model.view.mmio_axi4_0_w_valid;
    out.w_data = model.view.mmio_axi4_0_w_bits_data;
    out.w_strb = model.view.mmio_axi4_0_w_bits_strb;
    out.w_last = model.view.mmio_axi4_0_w_bits_last;
    out.b_ready = model.view.mmio_axi4_0_b_ready;
    out.ar_valid = model.view.mmio_axi4_0_ar_valid;
    out.ar_id = model.view.mmio_axi4_0_ar_bits_id;
    out.ar_addr = model.view.mmio_axi4_0_ar_bits_addr;
    out.ar_len = model.view.mmio_axi4_0_ar_bits_len;
    out.ar_size = model.view.mmio_axi4_0_ar_bits_size;
    out.r_ready = model.view.mmio_axi4_0_r_ready;
    return out;
  }
};
} // namespace

std::unique_ptr<RocketModel> makeArcilatorModel() {
  return std::make_unique<ArcilatorRocketModel>();
}
