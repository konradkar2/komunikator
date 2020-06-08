from db import db
from datetime import datetime
from sqlalchemy import or_
import uuid
from sqlalchemy_utils import UUIDType
from models.conversationInfo import ConversationInfoModel


class ConversationModel(db.Model):
    __tablename__ = 'conversation'

    message_id = db.Column(db.Integer, primary_key=True)    
    conversation_id = db.Column(UUIDType(binary=False), db.ForeignKey(
        'conversationInfo.id'), unique=False)
    conversationInfo = db.relationship(
        'ConversationInfoModel', foreign_keys=[conversation_id])
    member_id = db.Column(db.Integer, db.ForeignKey('users.id'))
    member = db.relationship('UserModel', foreign_keys=[member_id])

    content = db.Column(db.String(300))
    sent_on = db.Column(db.DateTime)

    # do_not_use_constructor to create new conversation, use create_conversation instead
    # use it to send message(send_message method)
    def __init__(self, conversation_id,member_id, sent_on, content):
        self.conversation_id = conversation_id        
        self.member_id = member_id
        self.sent_on = sent_on
        self.content = content
    def conversationJson(self):
        info = self.conversationInfo.json()
        info['members'] = [member.json() for member in self.get_members_by_conversation_id(self.conversation_id)]
        return info
    def messageJson(self):
        return {
            "message_id": self.message_id,
            "sent_on" : str(self.sent_on),
            "content": self.content,
            "sender" : self.member.json(),
        }

    @classmethod
    def find_by_member_id(cls, memberId):
        return cls.query.filter(cls.member_id == memberId).group_by(
            cls.conversation_id).all()
    @classmethod
    def find_by_conversation_id_and_last_message_id(cls, conversation_id, last_message_id):
        return cls.query.filter(cls.conversation_id == conversation_id).filter(cls.message_id > last_message_id).all()

    @classmethod
    def get_members_by_conversation_id(cls, conversation_id):
        result = cls.query.filter(
            cls.conversation_id == conversation_id).group_by(cls.member_id).all()
        return [conv.member for conv in result]

    @classmethod
    def check_if_user_is_a_member(cls, user_id, conversation_id):
        result = cls.query.filter_by(conversation_id=conversation_id).filter(
            cls.member_id == user_id).first()
        if result:
            return True
        return False

    
    def save_to_db(self):
        db.session.add(self)
        db.session.commit()
    
    def delete_from_db(self):
        db.session.delete(self)
        db.session.commit()
