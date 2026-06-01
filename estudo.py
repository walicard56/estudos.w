def saudades(nome):
    return f"Saudades de você, {nome}!"

def executar(func, arg):
    print(func(arg))

executar(saudades, "Maria")