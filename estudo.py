def saudades(nome):
    return f"Saudades de você, {nome}!"

def idade(ano_nascimento):
    from datetime import datetime
    ano_atual = datetime.now().year
    anos = ano_atual - ano_nascimento
    return f"voce tem {anos} anos de idade."

def doença(temperatura):
    def sintomas():
        return "Você pode estar com gripe."

    if temperatura > 38:
        print(f"Você está com febre.({temperatura}°C)")

    return sintomas()

def executar(func, arg):
    print(func(arg))

executar(saudades, "Maria")
executar(idade, 1990)
executar(doença, 39)
