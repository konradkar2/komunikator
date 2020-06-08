from db import db
from datetime import datetime
from sqlalchemy import or_
from sqlalchemy_utils import UUIDType



class ConversationInfoModel(db.Model):
    __tablename__ = 'conversationInfo'

    id = db.Column(
        UUIDType(binary=False), unique=True, primary_key=True)
    created_on = db.Column(db.DateTime)
    message_count = db.Column(db.Integer)
    last_message_sent_on = db.Column(db.DateTime)
    last_message = db.Column(db.String(300))
    last_message_user_id = db.Column(db.String(80), db.ForeignKey('users.id'))
    last_message_user = db.relationship(
        'UserModel', foreign_keys=[last_message_user_id])
    member_count = db.Column(db.Integer)

    def __init__(self, conversation_id, member_count, created_date=None):      
        self.id = conversation_id  
        self.member_count = member_count
        if(created_date):
            self.created_on = created_date
        else:
            self.created_on = datetime.utcnow()
        self.message_count = 0

    def json(self):
        if self.last_message_user_id:
            user_json = self.last_message_user.json()
        else:
            user_json = None
        return {
            "id": str(self.id),
            "created_on": str(self.created_on),
            "member_count": self.member_count,
            "message_count": self.message_count,
            "last_message": self.last_message,
            "last_message_sent_on": str(self.last_message_sent_on),
            "last_message_sender": user_json,
        }
    
   

    @classmethod
    def find_by_id(cls, conversationId):
        return cls.query.filter(
            cls.id == conversationId).first()

    def save_to_db(self):
        db.session.add(self)
        db.session.commit()

    # todo: only sender can delete
    def delete_from_db(self):
        db.session.delete(self)
        db.session.commit()
