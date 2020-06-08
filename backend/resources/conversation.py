from flask_restful import Resource, reqparse
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_socketio import SocketIO, emit

from datetime import datetime
import uuid

from models.conversation import ConversationModel
from models.conversationInfo import ConversationInfoModel


def maxLength(s):
    maxContentLength = 300
    minContentLength = 1
    if len(s) > maxContentLength:
        raise ValueError(
            "Maximum length of content is {} characters.".format(maxContentLength))
    if len(s) < minContentLength:
        raise ValueError(
            "Minimum length of content is {} characters.".format(minContentLength))
    return s
class MessageSender(Resource):
    def __init__(self,socket):
        self.socket = socket;
    parser_post = reqparse.RequestParser()
    parser_post.add_argument(
        "content", type=maxLength, required=True, help="Field Cannot be blank!"
    )
    @jwt_required
    def post(self, conversation_id):
        member_id = get_jwt_identity()

        data = MessageSender.parser_post.parse_args()
        content = data['content']
        if not ConversationModel.check_if_user_is_a_member(
                member_id, conversation_id):
            return {'message': 'Conversation does not exist or you are not a member.'}, 403
        
        date = datetime.utcnow()
        conv = ConversationModel(conversation_id, member_id, date, content)
        conv.save_to_db()
        update(conversation_id, content, member_id, date)

        self.socket.emit('newMessage', conversation_id, broadcast=True)
        return {'message': "Message sucessfully sent!"},201


class MessagesFinder(Resource):    
    @jwt_required
    def get(self, conversation_id, last_message_id):     
        member_id = get_jwt_identity()
        if not ConversationModel.check_if_user_is_a_member(
                member_id, conversation_id):
            return {'message': 'Conversation does not exist or you are not a member.'}, 403           
        new_messages = ConversationModel.find_by_conversation_id_and_last_message_id(conversation_id,last_message_id)        
        return {
            "new_messages" : [new_message.messageJson() for new_message in new_messages]
        }
   


class ConversationList(Resource):
    @jwt_required
    def get(self):
        memberId = get_jwt_identity()
        convs = ConversationModel.find_by_member_id(memberId)

        return {'conversations': [conv.conversationJson() for conv in convs]}

def create_conversation(member_id_1, member_id_2):    
    date = datetime.utcnow()
    _id = uuid.uuid4().hex
    conv_info = ConversationInfoModel(_id,2, date)
    conv1 = ConversationModel(_id,member_id_1, date, None)
    conv2 = ConversationModel(_id,member_id_2, date, None)
    conv1.save_to_db()
    conv2.save_to_db()
    conv_info.save_to_db()                 
    

# updates conversationInfoModel table, method called when new message is sent
def update(_id, last_message, last_message_user_id, sent_on, member_count=None):
    conv = ConversationInfoModel.find_by_id(_id)
    conv.message_count = conv.message_count + 1
    conv.last_message = last_message
    conv.last_message_user_id = last_message_user_id
    if(member_count):
        conv.member_count = member_count
    conv.last_message_sent_on = sent_on

    conv.save_to_db()