from flask import Flask
import logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    filename='app.log')

app = Flask(__name__)

class LivroNaoEncontradoError(Exception):
    """Exceção levantada quando um livro não é encontrado."""
    pass

books = [
    {"id": 1, "title": "Livro 1", "author": "Autor 1"},
    {"id": 2, "title": "Livro 2", "author": "Autor 2"},
    {"id": 3, "title": "Livro 3", "author": "Autor 3"},
]

@app.get("/livros/<int:livro_id>")
async def get_livro(livro_id: int):
    logging.info(f"Acessando a rota para o livro com ID {livro_id}")
    for livro in books:
        if livro["id"] == livro_id:
            logging.info(f"Livro encontrado: {livro}")
            return livro
    logging.error(f"Livro com ID {livro_id} não encontrado.")
    raise LivroNaoEncontradoError("Livro não encontrado.")

if __name__ == "__main__":
    app.run(debug=True)
