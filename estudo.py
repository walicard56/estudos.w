from flask import Flask, jsonify
import logging
from flasgger import Swagger


logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    filename='app.log')

app = Flask(__name__)


# Configura o Swagger para responder em /docs em vez do padrão /apidocs
app.config['SWAGGER'] = {
    'title': 'API de Livros',
    'uiversion': 3,
    'specs_route': '/docs' 
}

Swagger(app)

class LivroNaoEncontradoError(Exception):
    """Exceção levantada quando um livro não é encontrado."""
    pass

# 2. Registrador de Erro (Error Handler) do Flask
@app.errorhandler(LivroNaoEncontradoError)
def handle_livro_nao_encontrado(error):
    # Transforma a exceção em uma resposta HTTP 404 estruturada
    response = jsonify({"error": str(error)})
    response.status_code = 404
    return response

books = [
    {"id": 1, "title": "Livro 1", "author": "Autor 1", "category": 'science'},
    {"id": 2, "title": "Livro 2", "author": "Autor 2", "category": 'fiction'},
    {"id": 3, "title": "Livro 3", "author": "Autor 3", "category": 'science'},
    {"id": 4, "title": "Livro 4", "author": "Autor 4", "category": 'history'},
]


@app.get("/books/<string:book_title>")
async def read_books(book_title: str):
    for book in books:
        if book['title'].casefold() == book_title.casefold():
            return book
    raise LivroNaoEncontradoError(f"Livro '{book_title}' não encontrado.")


@app.get("/books/category/<string:category>")
async def read_books_by_category_query(category: str):
    books_to_return = []
    for book in books:
        if book['category'].casefold() == category.casefold():
            books_to_return.append(book)
    if not books_to_return:
        raise LivroNaoEncontradoError(f"Categoria '{category}' não encontrada.")
    return jsonify(books_to_return)

@app.get("/books/author/<string:author>")
async def read_books_by_author_query(author: str):
    books_to_return = []
    for book in books:
        if book['author'].casefold() == author.casefold():
            books_to_return.append(book)
    if not books_to_return:
        raise LivroNaoEncontradoError(f"Autor '{author}' não encontrado.")
    return jsonify(books_to_return)

@app.post("/books/create_book/<string:book>")
async def create_book(book: dict):
    if not all(key in book for key in ("id", "title", "author", "category")):
        return jsonify({"error": "Dados do livro incompletos."}), 400
    books.append(book)
    return jsonify({"message": "Livro criado com sucesso.", "book": book}), 201


@app.put("/books/update_book/<int:book_id>")
async def update_book(book_id: int, updated_book: dict):
    for book in books:
        if book['id'] == book_id:
            book.update(updated_book)
            return jsonify({"message": "Livro atualizado com sucesso.", "book": book})
    raise LivroNaoEncontradoError(f"Livro com ID '{book_id}' não encontrado.")


@app.route("/books/delete_book/<int:book_id>", methods=["DELETE", "POST"])
async def delete_book(book_id: int):
    for book in books:
        if book['id'] == book_id:
            books.remove(book)
            return jsonify({"message": "Livro deletado com sucesso."})
    raise LivroNaoEncontradoError(f"Livro com ID '{book_id}' não encontrado.")

@app.get("/books/byauthor/<string:author>")
async def read_books_by_author(author: str):
    books_to_return = []
    for book in books:
        if book['author'].casefold() == author.casefold():
            books_to_return.append(book)
    if not books_to_return:
        raise LivroNaoEncontradoError(f"Autor '{author}' não encontrado.")
    return jsonify(books_to_return)



if __name__ == "__main__":
    app.run(debug=False)
