from flask import Flask


def create_app(config_filename):
    app = Flask(__name__)
    app.config.from_pyfile(config_filename)
    return app

app = create_app(app.py)

if __name__ == "__main__":
  app.run(debug=False)