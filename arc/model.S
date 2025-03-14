
model.o:	file format coff-x86-64

Disassembly of section .text:

0000000000000000 <counter_eval>:
       0: 0f b6 01                     	movzbl	(%rcx), %eax
       3: 88 41 01                     	movb	%al, 0x1(%rcx)
       6: 0f b6 51 02                  	movzbl	0x2(%rcx), %edx
       a: 30 c2                        	xorb	%al, %dl
       c: 88 41 02                     	movb	%al, 0x2(%rcx)
       f: 84 c2                        	testb	%al, %dl
      11: 74 03                        	je	0x16 <counter_eval+0x16>
      13: fe 41 03                     	incb	0x3(%rcx)
      16: 0f b6 41 03                  	movzbl	0x3(%rcx), %eax
      1a: 88 41 04                     	movb	%al, 0x4(%rcx)
      1d: 88 41 05                     	movb	%al, 0x5(%rcx)
      20: c3                           	retq
      21: 66 2e 0f 1f 84 00 00 00 00 00	nopw	%cs:(%rax,%rax)
      2b: 0f 1f 44 00 00               	nopl	(%rax,%rax)

0000000000000030 <entry>:
      30: 56                           	pushq	%rsi
      31: 57                           	pushq	%rdi
      32: 48 83 ec 28                  	subq	$0x28, %rsp
      36: b9 06 00 00 00               	movl	$0x6, %ecx
      3b: e8 00 00 00 00               	callq	0x40 <entry+0x10>
      40: 48 89 c6                     	movq	%rax, %rsi
      43: c7 00 00 00 00 00            	movl	$0x0, (%rax)
      49: 66 c7 40 04 00 00            	movw	$0x0, 0x4(%rax)
      4f: c6 00 01                     	movb	$0x1, (%rax)
      52: 48 89 c1                     	movq	%rax, %rcx
      55: e8 00 00 00 00               	callq	0x5a <entry+0x2a>
      5a: c6 06 00                     	movb	$0x0, (%rsi)
      5d: 48 89 f1                     	movq	%rsi, %rcx
      60: e8 00 00 00 00               	callq	0x65 <entry+0x35>
      65: 0f b6 56 04                  	movzbl	0x4(%rsi), %edx
      69: 48 8d 3d 00 00 00 00         	leaq	(%rip), %rdi            # 0x70 <entry+0x40>
      70: 48 89 f9                     	movq	%rdi, %rcx
      73: e8 00 00 00 00               	callq	0x78 <entry+0x48>
      78: c6 06 01                     	movb	$0x1, (%rsi)
      7b: 48 89 f1                     	movq	%rsi, %rcx
      7e: e8 00 00 00 00               	callq	0x83 <entry+0x53>
      83: c6 06 00                     	movb	$0x0, (%rsi)
      86: 48 89 f1                     	movq	%rsi, %rcx
      89: e8 00 00 00 00               	callq	0x8e <entry+0x5e>
      8e: c6 06 01                     	movb	$0x1, (%rsi)
      91: 48 89 f1                     	movq	%rsi, %rcx
      94: e8 00 00 00 00               	callq	0x99 <entry+0x69>
      99: c6 06 00                     	movb	$0x0, (%rsi)
      9c: 48 89 f1                     	movq	%rsi, %rcx
      9f: e8 00 00 00 00               	callq	0xa4 <entry+0x74>
      a4: 0f b6 56 04                  	movzbl	0x4(%rsi), %edx
      a8: 48 89 f9                     	movq	%rdi, %rcx
      ab: e8 00 00 00 00               	callq	0xb0 <entry+0x80>
      b0: 48 89 f1                     	movq	%rsi, %rcx
      b3: e8 00 00 00 00               	callq	0xb8 <entry+0x88>
      b8: 90                           	nop
      b9: 48 83 c4 28                  	addq	$0x28, %rsp
      bd: 5f                           	popq	%rdi
      be: 5e                           	popq	%rsi
      bf: c3                           	retq
