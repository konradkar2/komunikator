from db import db
from sqlalchemy import or_, and_


class FriendshipModel(db.Model):
    __tablename__ = 'friends'

    id = db.Column(db.Integer, primary_key=True)
    friend_a_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    friend_a = db.relationship('UserModel', foreign_keys=[friend_a_id])
    friend_b_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    friend_b = db.relationship('UserModel', foreign_keys=[friend_b_id])

    def __init__(self, friendIdA, friendIdB):
        self.friend_a_id = friendIdA
        self.friend_b_id = friendIdB

    def json(self):
        return {'friends': [self.friend_a.username, self.friend_b.username]}

    @classmethod
    def find_friends_by_id(cls, _id):
        return cls.query.filter(or_(cls.friend_a_id == _id, cls.friend_b_id == _id)).all()

    @classmethod
    def check_if_are_friends(cls, idA, idB):
        result = cls.query.filter(or_(cls.friend_a_id == idA, cls.friend_a_id == idB)).filter(
            or_(cls.friend_b_id == idA, cls.friend_b_id == idB)).first()
        if result:
            return True
        return False

    def save_to_db(self):
        db.session.add(self)
        db.session.commit()

    # todo: only sender can delete
    def delete_from_db(self):
        db.session.delete(self)
        db.session.commit()
