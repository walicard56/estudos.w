def decorador(funcao):
    def nova_funcao():
        print("Antes da função original")
        funcao()
        print("Depois da função original")
    return nova_funcao

@decorador
def minha_funcao():
    print("Esta é a função original")

minha_funcao()