global main
extern printf
extern putchar
extern scanf 
section .text

main:
push ebp
push dword [_@DSP + (0)]
mov ebp, esp
mov [_@DSP + 0], ebp
sub esp, 12 
mov edx, ebp 
lea eax, [edx + -4] 
push eax 
push _@Integer 
call scanf 
add esp, 8 
mov edx, ebp 
lea eax, [edx + -8] 
push eax 
push _@Integer 
call scanf 
add esp, 8 
push dword [EBP + (-4)] 
push 10 
pop eax
cmp dword [ESP], eax 
jnge Falso0 
mov dword [ESP], 1
jmp Fim0 
Falso0: 
mov dword [ESP], 0 
Fim0: 
pop eax 
cmp eax, 1 
jne rotuloElse0 
mov dword[_@DSP +0 ], ebp
push dword[ebp + (-4) ]
push @message1
call printf
add esp, 8
jmp rotuloFim1
rotuloElse0: 
mov dword[_@DSP +0 ], ebp
push dword[ebp + (-8) ]
push @message2
call printf
add esp, 8
rotuloFim1: 
add esp, 12
mov esp, ebp
pop dword [_@DSP + 0]
pop ebp
ret
section .data
_@Integer: db '%d',0 
@message1: db '%d',0 
@message2: db '%d',0 
_@DSP: times 4 db 0