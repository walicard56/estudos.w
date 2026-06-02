from pydantic import BaseModel, EmailStr, Field, ValidationError

#toda estrutura de dados deve ser criada a partir de uma classe que herda de BaseModel
class User(BaseModel):
    id:int
    nome:str
    email: str
    ativo: bool = True

# 1. caso de sucesso com coerção automatica
dados_api = {"id": "100", "nome": "Carlos", "email": "carlos@email.com", "ativo": True}

user = User(**dados_api)
print(user.id, user.nome, user.email, user.ativo)

try:
    user_invalido = User(id="21", nome="Maria", email="email_ruin")
except ValidationError as e:
    print("ocorreu um erro de validação:", e)
    print(e.json(indent=2))