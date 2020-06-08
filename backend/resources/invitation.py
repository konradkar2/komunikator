from flask_restful import Resource, reqparse
from flask_jwt_extended import jwt_required, get_jwt_identity
from models.invitation import InvitationModel

from models.user import UserModel
from models.friendship import FriendshipModel
from models.conversation import ConversationModel
from resources.conversation import create_conversation

class MyInvitations(Resource):
    @jwt_required
    def get(self):
        myId = get_jwt_identity()
        sentByMe = InvitationModel.find_by_sender_id(myId)
        receivedByMe = InvitationModel.find_by_recipient_id(myId)

        return {'invitations': {
            'sent': [invitationA.recipient.json() for invitationA in sentByMe],
            'received': [invitationB.json() for invitationB in receivedByMe]
        }}


class InvitationSender(Resource):
    @jwt_required
    def post(self, recipient_id):

        user = UserModel.find_by_id(recipient_id)
        if not user:
            return {'message': 'User does not exist.'}, 400
        myId = get_jwt_identity()
        if myId == recipient_id:
            return {'message': 'You cant invite yourself.'}, 400
        result = InvitationModel.find_by_sender_and_recipient_id(
            myId, recipient_id)
        if result:
            return {'message': 'User already invited.'}, 400
        if FriendshipModel.check_if_are_friends(myId, recipient_id):
            return {'message': 'User is already in friend list.'}, 400

        invitation = InvitationModel(myId, recipient_id)
        invitation.save_to_db()

        return {'message': "Invitation succesfully sent."}, 201


# todo wrong id format detection
class InvitationManager(Resource):
    parser = reqparse.RequestParser()
    parser.add_argument(
        "invitation_id", type=str, required=True, help="This field cannot be left blank!"
    )
    parser.add_argument(
        "accept", type=bool, required=True, help="This field cannot be left blank!"
    )
    @jwt_required
    def post(self):
        data = InvitationManager.parser.parse_args()
        invitation_id = data['invitation_id']
        accept = data['accept']
        if(accept):
            invitation = InvitationModel.find_by_id(invitation_id)
            if not invitation:
                return {'message': 'Invitation not found.'}, 404
            invitation.delete_from_db()

            friendship = FriendshipModel(
                invitation.sender_id, invitation.recipient_id)
            friendship.save_to_db()

            create_conversation(
                invitation.sender_id, invitation.recipient_id)

            return {'message': "Invitation accepted succesfully."}, 200
        else:
            invitation = InvitationModel.find_by_id(invitation_id)
            if not invitation:
                return {'message': 'Invitation not found.'}, 404
            invitation.delete_from_db()

            return {'message': "Invitation declined succesfully."}, 200
