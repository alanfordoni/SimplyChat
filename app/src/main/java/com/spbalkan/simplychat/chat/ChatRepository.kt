package com.spbalkan.simplychat.chat

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.spbalkan.simplychat.ServiceLocator
import com.spbalkan.simplychat.chat.models.Conversation
import com.spbalkan.simplychat.chat.models.ConversationDTO
import com.spbalkan.simplychat.chat.models.ConversationUser
import com.spbalkan.simplychat.chat.models.Message
import com.spbalkan.simplychat.chat.models.MessageDTO
import com.spbalkan.simplychat.utils.Constants
import kotlinx.coroutines.tasks.await

interface ChatRepository {
    suspend fun getAllConversations(): ChatResult
    suspend fun getChatMessages(chatId: String, userId: String): ChatResult
    suspend fun getUserByUsername(username: String): ChatResult
    suspend fun getUserByUid(userId: String): ChatResult
    suspend fun createNewChat(username: String, userId: String): ChatResult
    suspend fun sendMessage(message: String, chatId: String): ChatResult
}

class ChatRepositoryImp(
    private val auth: FirebaseAuth,
    private val firebaseDb: FirebaseFirestore
) : ChatRepository {
    override suspend fun getAllConversations(): ChatResult {
        return try {
            val userDoc = firebaseDb.collection(Constants.DB_CHATS_COLLECTION)
                .get()
                .await()

            val list = userDoc.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(ConversationDTO::class.java)
            }

            val domainList = list.map { convoDTO ->
                Conversation(
                    id = convoDTO.id,
                    participants = convoDTO.participants,
                    otherUser = getOtherUserUsername(convoDTO.participants),
                    lastUpdated = convoDTO.lastUpdated
                )
            }
            ChatResult.AllConversationsSuccess(domainList)

        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "get All Conversations failed"
            Log.e(TAG, "getAllConversations: Error: $errorMsg")
            ChatResult.Error(errorMsg)
        }
    }

    private fun getOtherUserUsername(participants: Map<String, String>): ConversationUser {
        val myUid = ServiceLocator.authRepository.getUserAuthId()
        val (otherUid, otherUsername) = requireNotNull(participants.entries.firstOrNull { it.key != myUid }) {
            "Participants map must contain at least one other user besides $myUid."
        }

        return ConversationUser(
            uid = otherUid,
            username = otherUsername
        )
    }

    override suspend fun getChatMessages(chatId: String, userId: String): ChatResult {
        return try {
            val queryPath = "${Constants.DB_CHATS_COLLECTION}/$chatId/${Constants.DB_MESSAGES_COLLECTION}"

            val userDoc = firebaseDb.collection(queryPath)
                .orderBy(Constants.DB_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .await()

            val list = userDoc.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(MessageDTO::class.java)
            }
            val domainList = list.map { msgDTO ->
                Message(
                    id = msgDTO.id,
                    text = msgDTO.text,
                    isReceivedMessage = msgDTO.senderId == userId,
                    timestamp = msgDTO.timestamp
                )
            }

            ChatResult.ChatMessagesSuccess(domainList)

        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "get Chat Messages failed"
            Log.e(TAG, "getChatMessages: Error: $errorMsg")
            ChatResult.Error(errorMsg)
        }
    }

    override suspend fun sendMessage(message: String, chatId: String): ChatResult {
        return try {
            val queryPath = "${Constants.DB_CHATS_COLLECTION}/$chatId/${Constants.DB_MESSAGES_COLLECTION}"
            val myUid = auth.currentUser!!.uid

            val chatData = hashMapOf(
                "senderId" to myUid,
                "text" to message,
                "timestamp" to FieldValue.serverTimestamp()
            )

            firebaseDb.collection(queryPath)
                .document()
                .set(chatData)
                .await()

            ChatResult.MessageSent
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: error: ${e.localizedMessage}")
            val errorMsg = e.localizedMessage ?: "send message failed"

            ChatResult.Error(errorMsg)
        }
    }


    override suspend fun getUserByUsername(username: String): ChatResult {

        return try {
            val userDoc = firebaseDb.collection(Constants.DB_USERNAMES_COLLECTION)
                .document(username)
                .get()
                .await()

            val username = userDoc.id
            val uid = userDoc.getString(Constants.DB_UID)

            Log.d(TAG, "getUser: $username , $uid")
            ChatResult.UserFound(username, uid!!)

        } catch (e: Exception) {
            Log.e(TAG, "getUser: error: ${e.localizedMessage}")
            val errorMessage = e.localizedMessage ?: "get user failed"
            ChatResult.Error(errorMessage)
        }
    }

    override suspend fun getUserByUid(userId: String): ChatResult {

        return try {
            val userDoc = firebaseDb.collection(Constants.DB_USERNAMES_COLLECTION)
                .whereEqualTo(Constants.DB_UID, userId)
                .get()
                .await()

            val username = userDoc.first().id

            Log.d(TAG, "getUser: ${username}, $userId")
            ChatResult.UserFound(username, userId)

        } catch (e: Exception) {
            Log.e(TAG, "getUser: error: ${e.localizedMessage}")
            val errorMessage = e.localizedMessage ?: "get user failed"
            ChatResult.Error(errorMessage)
        }
    }

    override suspend fun createNewChat(username: String, userId: String): ChatResult {

        return try {
            val myUid = auth.currentUser!!.uid
            val myDataResult = getUserByUid(myUid)
            var myUsername = ""

            when (myDataResult) {
                is ChatResult.UserFound -> {
                    myUsername = myDataResult.username
                }

                else -> {}
            }

            val docName = "${myUid}_${userId}"

            val participants = mapOf(
                myUid to myUsername,
                userId to username
            )

            val chatData = hashMapOf(
                "lastUpdated" to FieldValue.serverTimestamp(),
                "participants" to participants
            )

            firebaseDb.collection(Constants.DB_CHATS_COLLECTION)
                .document(docName)
                .set(chatData)
                .await()

            ChatResult.ChatCreated
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: error: ${e.localizedMessage}")
            val errorMsg = e.localizedMessage ?: "send message failed"

            ChatResult.Error(errorMsg)
        }
    }

    companion object {
        private const val TAG = "zzChatRepository"
    }
}

sealed class ChatResult {
    data class UserFound(val username: String, val userId: String) : ChatResult()
    data object MessageSent : ChatResult()
    data object ChatCreated : ChatResult()
    data class AllConversationsSuccess(val list: List<Conversation>) : ChatResult()
    data class ChatMessagesSuccess(val list: List<Message>) : ChatResult()
    data class Error(val error: String) : ChatResult()
}