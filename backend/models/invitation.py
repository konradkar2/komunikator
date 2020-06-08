from db import db
from sqlalchemy import or_, and_
from sqlalchemy_utils import UUIDType
import uuid


class InvitationModel(db.Model):
    __tablename__ = 'invitations'

    id = db.Column(UUIDType(binary=False), primary_key=True) 
    sender_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    sender = db.relationship('UserModel', foreign_keys=[sender_id])
    recipient_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    recipient = db.relationship('UserModel', foreign_keys=[recipient_id])

    def __init__(self, senderId, recipientId):
        self.sender_id = senderId
        self.recipient_id = recipientId
        self.id = uuid.uuid4().hex

    def json(self):
        return {'invitation_id': str(self.id), 'sender': self.sender.json()}

    @classmethod
    def find_by_id(cls, _id):
        return cls.query.filter_by(id=_id).first()

    @classmethod
    def find_by_sender_id(cls, _id):
        return cls.query.filter_by(sender_id=_id).all()

    @classmethod
    def find_by_recipient_id(cls, _id):
        return cls.query.filter_by(recipient_id=_id).all()

    @classmethod
    def find_by_sender_and_recipient_id(cls, idS, idR):
        return cls.query.filter(and_(cls.sender_id == idS, cls.recipient_id == idR)).first()

    def save_to_db(self):
        db.session.add(self)
        db.session.commit()

    def delete_from_db(self):
        db.session.delete(self)
        db.session.commit()
