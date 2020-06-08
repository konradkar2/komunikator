import sqlite3
from flask_restful import Resource, reqparse
from models.user import UserModel
from models.friendship import FriendshipModel
from flask_jwt_extended import jwt_required, get_jwt_identity


class FriendList(Resource):
    @jwt_required
    def get(self):
        friends = []
        friendships = FriendshipModel.find_friends_by_id(get_jwt_identity())
        for friendship in friendships:
            if friendship.friend_a.id == get_jwt_identity():
                friends.append(friendship.friend_b)
            else:
                friends.append(friendship.friend_a)
         # friend is a UserModel object
        return {'friends': [user.json() for user in friends]}
