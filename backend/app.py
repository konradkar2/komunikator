from flask import Flask, render_template,jsonify, request
from flask_restful import Api
from flask_jwt_extended import JWTManager, decode_token, get_jwt_identity
from db import db
from flask_socketio import SocketIO,send

from resources.user import (UserRegister,
                           UserSearch,
                           UserLogin,
                           UserPassword,
                           UserAbout)
from resources.invitation import MyInvitations, InvitationSender, InvitationManager
from resources.friendship import FriendList
from resources.conversation import (ConversationList,
                                    MessagesFinder,
                                    MessageSender,
                                    ConversationManager,
                                    ConversationMaker)

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['JWT_SECRET_KEY'] = 'tajnykluczyk'
app.config['PROPAGATE_EXCEPTIONS'] = True
api = Api(app)

socketio = SocketIO(app,cors_allowed_origins='*')

users_session_id = {}

@socketio.on('message')
def handleMessage(msg):
    print("Message: "+ msg)
    send(msg,broadcast=True)
@socketio.on('token')
def handleToken(token):
    print("Token: "+ token)   
    decoded_token = decode_token(token)
    user_id = decoded_token['identity']
    users_session_id[user_id] = request.sid
    
    




jwt = JWTManager(app)

@jwt.expired_token_loader
def expired_token_callback():
    return jsonify({
        'description': 'The token has expired',
        'error': 'expired_token'
    }), 401

@jwt.invalid_token_loader
def invalid_token_callback(error):
    return jsonify({
        'description': 'Signature verification failed',
        'error': 'invalid_token'
    }), 401

@jwt.unauthorized_loader
def missing_token_callback(error):
    return jsonify({
        'description': 'Request does not contain access token',
        'error': 'authorization_required'
    }), 401

@jwt.revoked_token_loader
def revoked_token_callback():
    return jsonify({
        'description': 'The token has been revoked',
        'error': 'revoked_token'
    }), 401


@app.before_first_request
def create_tables():
    db.create_all()
    app.logger.debug('Headers: %s', request.headers)
    app.logger.debug('Body: %s', request.get_data())

    




api.add_resource(UserRegister, '/register')
api.add_resource(UserLogin,'/auth')
api.add_resource(UserPassword,'/reset')
api.add_resource(UserAbout,'/aboutuser')

api.add_resource(UserSearch, '/search/<string:username>')

api.add_resource(MyInvitations, '/invitations')
api.add_resource(InvitationSender, '/invite/<int:recipient_id>')
api.add_resource(InvitationManager, '/invitation/manage')

api.add_resource(FriendList, '/friends')

api.add_resource(ConversationList, '/conversations')
api.add_resource(ConversationManager, '/adduser')
api.add_resource(ConversationMaker, '/create')
api.add_resource(MessagesFinder, '/message/<string:conversation_id>/<int:last_message_id>')
api.add_resource(MessageSender, '/message/<string:conversation_id>',resource_class_kwargs={'socket': socketio})

if __name__ == '__main__':
    db.init_app(app)
    socketio.run(app,host="0.0.0.0")
