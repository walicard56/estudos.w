from dataclasses import dataclass, field

@dataclass
class Estudo:
    nome: str 
    preco: float
    estoque: int = 0 #valor padrão simples
    tags: list[str] = field(default_factory=list) #valor padrão complexo

p1 = Estudo(nome="android", preco=1000.0, estoque=10, tags=["celular", "sistema operacional"])


# O problema: aceita dados incorretos sem gerar erro
p2 = Estudo(nome="Mouse", preco="texto incorreto", estoque=10) 
print(p2)