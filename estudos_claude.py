import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    filename='app.log')

class saldoInsuficienteerror(Exception):
    """Exceção levantada quando o saldo é menor que o valor do saque."""
    pass

def sacar(saldo, valor_saque):
    if valor_saque > saldo:
        raise saldoInsuficienteerror("Saldo insuficiente para realizar o saque.")
    return saldo - valor_saque
    
try:
    novo_saldo = sacar(50,30)
except saldoInsuficienteerror as erro:
    logging.error(f"Erro ao realizar o saque: {erro}")
    print("erro: saldo insuficente para realizar o saque.")
except Exception as erro:
    logging.error(f"Erro inesperado: {erro}")
    print("Ocorreu um erro inesperado.")    
else:
    logging.info(f"Saque realizado com sucesso. Novo saldo: {novo_saldo}")
    print(f"Saque realizado com sucesso. Novo saldo: {novo_saldo}")
finally:    
    logging.info("Operação de saque finalizada.")
    print("Operação de saque finalizada.")

