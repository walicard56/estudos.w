#type hints

def saudar(nome: str, repetir: int) -> str:
    return f"Olá, {nome}! " * repetir

usuarios: list[str] = ["alice", "joao", "maria"]
cadastro: dict[str, int] = {"alice": 30, "joao": 25, "maria": 28}

print(saudar(usuarios[1], 3))