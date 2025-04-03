module motor_encoder (
    input clk,          // Clock input
    input reset,        // Reset input
    input enc_a,        // Encoder channel A
    input enc_b,        // Encoder channel B
    output reg signed [15:0] position  // 16-bit signed position counter
);

// Synchronization registers for metastability prevention
reg [1:0] enc_a_sync;
reg [1:0] enc_b_sync;

// Encoder state registers
reg [1:0] prev_state;
reg [1:0] curr_state;

// Direction determination
wire signed [1:0] direction;

// Synchronize encoder inputs to clock domain
always @(posedge clk or posedge reset) begin
    if (reset) begin
        enc_a_sync <= 2'b00;
        enc_b_sync <= 2'b00;
        prev_state <= 2'b00;
        curr_state <= 2'b00;
        position <= 0;
    end else begin
        // Double synchronization for metastability prevention
        enc_a_sync <= {enc_a_sync[0], enc_a};
        enc_b_sync <= {enc_b_sync[0], enc_b};
        
        // Update state history
        prev_state <= curr_state;
        curr_state <= {enc_a_sync[1], enc_b_sync[1]};
        
        // Update position counter
        position <= position + direction;
    end
end

// Quadrature decoder direction logic
assign direction = 
    ((prev_state == 2'b00 && curr_state == 2'b01) ||  // Forward transitions
     (prev_state == 2'b01 && curr_state == 2'b11) ||
     (prev_state == 2'b11 && curr_state == 2'b10) ||
     (prev_state == 2'b10 && curr_state == 2'b00)) ? 1 :
    ((prev_state == 2'b00 && curr_state == 2'b10) ||  // Reverse transitions
     (prev_state == 2'b10 && curr_state == 2'b11) ||
     (prev_state == 2'b11 && curr_state == 2'b01) ||
     (prev_state == 2'b01 && curr_state == 2'b00)) ? -1 : 0;

endmodule
