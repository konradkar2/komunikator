from db import db

# todo: split user class into two - one with personal data, second with presentation data


class UserModel(db.Model):
    __tablename__ = 'users'
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80))
    password = db.Column(db.String(80))
    about = db.Column(db.String(200))
    salt = db.Column(db.String(80))

    def __init__(self, username, password, about, salt):
        self.username = username
        self.password = password
        self.salt = salt
        self.about = about

    def json(self):
        return {
            "username": self.username,
            "id": self.id,
            "about": self.about,
            "image_url": "work_in_progress"

        }

    @classmethod
    def find_by_username(cls, username):
        return cls.query.filter_by(username=username).first()

    @classmethod  # case insensetive
    def find_many_by_username(cls, username):
        return cls.query.filter(cls.username.contains(username)).all()

    @classmethod
    def find_by_id(cls, _id):
        return cls.query.filter_by(id=_id).first()

    def save_to_db(self):
        db.session.add(self)
        db.session.commit()
