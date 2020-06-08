import base64
import os
from models.user import UserModel
from Crypto.Cipher import AES
from Crypto.Protocol.KDF import PBKDF2
from Crypto.Hash import SHA512

# input: (string, bytes), output: string


def hashPassword(password, salt):
    password = password.encode()  # encoding string to bytes
    _hash = PBKDF2(password, salt, 64, 1000, hmac_hash_module=SHA512)
    return (base64.b64encode(_hash)).decode() 


