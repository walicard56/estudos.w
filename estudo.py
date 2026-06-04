from flask import Flask

app = Flask(__name__)

books = [
    {"id": 1, "title": "Livro 1", "author": "Autor 1"},
    {"id": 2, "title": "Livro 2", "author": "Autor 2"},
    {"id": 3, "title": "Livro 3", "author": "Autor 3"},
]

@app.get("/")
async def home():
    return books

if __name__ == "__main__":
    app.run(debug=True)
