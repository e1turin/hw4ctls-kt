	.def	@feat.00;
	.scl	3;
	.type	0;
	.endef
	.globl	@feat.00
.set @feat.00, 0
	.file	"LLVMDialectModule"
	.def	Dut_eval;
	.scl	2;
	.type	32;
	.endef
	.text
	.globl	Dut_eval                        # -- Begin function Dut_eval
	.p2align	4
Dut_eval:                               # @Dut_eval
.Lfunc_begin0:
# %bb.0:
	.file	1 "model.mlir"
	.loc	1 2 19 prologue_end             # model.mlir:2:19
	movzbl	(%rcx), %eax
	movb	%al, 1(%rcx)
	movzbl	2(%rcx), %edx
	xorb	%al, %dl
	movb	%al, 2(%rcx)
	testb	%al, %dl
	je	.LBB0_2
# %bb.1:
	.loc	1 5 9                           # model.mlir:5:9
	incb	3(%rcx)
.LBB0_2:
	.loc	1 5 9                           # model.mlir:5:9
	movzbl	3(%rcx), %eax
	.loc	1 3 10                          # model.mlir:3:10
	movb	%al, 4(%rcx)
	.loc	1 5 9                           # model.mlir:5:9
	movb	%al, 5(%rcx)
	.loc	1 2 1                           # model.mlir:2:1
	retq
.Ltmp0:
.Lfunc_end0:
                                        # -- End function
	.def	entry;
	.scl	2;
	.type	32;
	.endef
	.globl	entry                           # -- Begin function entry
	.p2align	4
entry:                                  # @entry
.Lfunc_begin1:
	.loc	1 1 0                           # model.mlir:1:0
.seh_proc entry
# %bb.0:
	pushq	%rsi
	.seh_pushreg %rsi
	subq	$32, %rsp
	.seh_stackalloc 32
	.seh_endprologue
	.loc	1 15 3 prologue_end             # model.mlir:15:3
	movl	$6, %ecx
	callq	malloc
	movq	%rax, %rsi
	movl	$0, (%rax)
	movw	$0, 4(%rax)
	.loc	1 17 5                          # model.mlir:17:5
	movb	$1, (%rax)
	.loc	1 18 5                          # model.mlir:18:5
	movq	%rax, %rcx
	callq	Dut_eval
	.loc	1 21 5                          # model.mlir:21:5
	movb	$0, (%rsi)
	.loc	1 22 5                          # model.mlir:22:5
	movq	%rsi, %rcx
	callq	Dut_eval
	.loc	1 25 13                         # model.mlir:25:13
	movzbl	4(%rsi), %edx
	.loc	1 27 5                          # model.mlir:27:5
	leaq	"_arc_sim_emit_full_Dut o"(%rip), %rcx
	callq	printf
	.loc	1 15 3                          # model.mlir:15:3
	movq	%rsi, %rcx
	callq	free
	nop
	.seh_startepilogue
	.loc	1 30 3 epilogue_begin           # model.mlir:30:3
	addq	$32, %rsp
	popq	%rsi
	.seh_endepilogue
	retq
.Ltmp1:
.Lfunc_end1:
	.seh_endproc
                                        # -- End function
	.section	.rdata,"dr"
"_arc_sim_emit_full_Dut o":             # @"_arc_sim_emit_full_Dut o"
	.asciz	"Dut o = %zx\n"

	.section	.debug_abbrev,"dr"
	.byte	1                               # Abbreviation Code
	.byte	17                              # DW_TAG_compile_unit
	.byte	0                               # DW_CHILDREN_no
	.byte	37                              # DW_AT_producer
	.byte	14                              # DW_FORM_strp
	.byte	19                              # DW_AT_language
	.byte	5                               # DW_FORM_data2
	.byte	3                               # DW_AT_name
	.byte	14                              # DW_FORM_strp
	.byte	16                              # DW_AT_stmt_list
	.byte	23                              # DW_FORM_sec_offset
	.byte	17                              # DW_AT_low_pc
	.byte	1                               # DW_FORM_addr
	.byte	18                              # DW_AT_high_pc
	.byte	6                               # DW_FORM_data4
	.byte	0                               # EOM(1)
	.byte	0                               # EOM(2)
	.byte	0                               # EOM(3)
	.section	.debug_info,"dr"
.Lcu_begin0:
	.long	.Ldebug_info_end0-.Ldebug_info_start0 # Length of Unit
.Ldebug_info_start0:
	.short	4                               # DWARF version number
	.secrel32	.debug_abbrev           # Offset Into Abbrev. Section
	.byte	8                               # Address Size (in bytes)
	.byte	1                               # Abbrev [1] 0xb:0x1b DW_TAG_compile_unit
	.secrel32	.Linfo_string0          # DW_AT_producer
	.short	2                               # DW_AT_language
	.secrel32	.Linfo_string1          # DW_AT_name
	.secrel32	.Lline_table_start0     # DW_AT_stmt_list
	.quad	.Lfunc_begin0                   # DW_AT_low_pc
	.long	.Lfunc_end1-.Lfunc_begin0       # DW_AT_high_pc
.Ldebug_info_end0:
	.section	.debug_str,"dr"
.Linfo_string0:
	.asciz	"MLIR"                          # string offset=0
.Linfo_string1:
	.asciz	"model.mlir"                    # string offset=5
	.section	.debug_line,"dr"
.Lline_table_start0:
