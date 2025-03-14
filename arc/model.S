
model.o:	file format coff-x86-64

Disassembly of section .text:

0000000000000000 <Dut_eval>:
       0: 0f b6 01                     	movzbl	(%rcx), %eax
       3: 88 41 01                     	movb	%al, 0x1(%rcx)
       6: 0f b6 51 02                  	movzbl	0x2(%rcx), %edx
       a: 30 c2                        	xorb	%al, %dl
       c: 88 41 02                     	movb	%al, 0x2(%rcx)
       f: 84 c2                        	testb	%al, %dl
      11: 74 03                        	je	0x16 <Dut_eval+0x16>
      13: fe 41 03                     	incb	0x3(%rcx)
      16: 0f b6 41 03                  	movzbl	0x3(%rcx), %eax
      1a: 88 41 04                     	movb	%al, 0x4(%rcx)
      1d: 88 41 05                     	movb	%al, 0x5(%rcx)
      20: c3                           	retq
      21: 66 2e 0f 1f 84 00 00 00 00 00	nopw	%cs:(%rax,%rax)
      2b: 0f 1f 44 00 00               	nopl	(%rax,%rax)

0000000000000030 <entry>:
      30: 56                           	pushq	%rsi
      31: 48 83 ec 20                  	subq	$0x20, %rsp
      35: b9 06 00 00 00               	movl	$0x6, %ecx
      3a: e8 00 00 00 00               	callq	0x3f <entry+0xf>
      3f: 48 89 c6                     	movq	%rax, %rsi
      42: c7 00 00 00 00 00            	movl	$0x0, (%rax)
      48: 66 c7 40 04 00 00            	movw	$0x0, 0x4(%rax)
      4e: c6 00 01                     	movb	$0x1, (%rax)
      51: 48 89 c1                     	movq	%rax, %rcx
      54: e8 00 00 00 00               	callq	0x59 <entry+0x29>
      59: c6 06 00                     	movb	$0x0, (%rsi)
      5c: 48 89 f1                     	movq	%rsi, %rcx
      5f: e8 00 00 00 00               	callq	0x64 <entry+0x34>
      64: 0f b6 56 04                  	movzbl	0x4(%rsi), %edx
      68: 48 8d 0d 00 00 00 00         	leaq	(%rip), %rcx            # 0x6f <entry+0x3f>
      6f: e8 00 00 00 00               	callq	0x74 <entry+0x44>
      74: 48 89 f1                     	movq	%rsi, %rcx
      77: e8 00 00 00 00               	callq	0x7c <entry+0x4c>
      7c: 90                           	nop
      7d: 48 83 c4 20                  	addq	$0x20, %rsp
      81: 5e                           	popq	%rsi
      82: c3                           	retq
