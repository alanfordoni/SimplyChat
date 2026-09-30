package com.spbalkan.simplychat.chat

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.spbalkan.simplychat.chat.models.Conversation
import com.spbalkan.simplychat.chat.models.ConversationDTO
import com.spbalkan.simplychat.chat.models.ConversationUser
import com.spbalkan.simplychat.chat.models.Message
import com.spbalkan.simplychat.chat.models.MessageDTO
import com.spbalkan.simplychat.data.DataStoreRepository
import com.spbalkan.simplychat.utils.Constants
import com.spbalkan.simplychat.utils.toDailyTime
import com.spbalkan.simplychat.utils.toFirebaseTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

interface ChatRepository {
    suspend fun getUserConversations(): ChatResult
    fun listenToMessages(chatId: String): Flow<ChatResult>
    // find user by username keyword
    suspend fun findUserByKeyword(keyword: String): ChatResult
    suspend fun createNewChat(otherUsername: String, otherUserId: String): ChatResult
    suspend fun sendMessage(message: String, chatId: String): ChatResult
    suspend fun hasUnreadMessages(): Boolean
}

@Singleton
class ChatRepositoryImp @Inject constructor(
    private val auth: FirebaseAuth,
    private val firebaseDb: FirebaseFirestore,
    private val dataStoreRepository: DataStoreRepository
) : ChatRepository {
    override suspend fun getUserConversations(): ChatResult {

        return try {
            val userId = getUserAuthId()
            val userChats = firebaseDb.collection(Constants.DB_CHATS_COLLECTION)
                .whereNotEqualTo("${Constants.DB_PARTICIPANTS}.$userId", null)
                .get()
                .await()

            Log.d(TAG, "getUserConversations: snapshot size: ${userChats.size()}")
            val list = userChats.documents.mapNotNull { documentSnapshot ->
                documentSnapshot.toObject(ConversationDTO::class.java)
            }

            val domainList = list.map { convoDTO ->
                Conversation(
                    id = convoDTO.id,
                    lastMessageSenderId = convoDTO.lastMessageSenderId,
                    lastMessageText = convoDTO.lastMessageText,
                    participants = convoDTO.participants,
                    otherUser = getOtherUser(convoDTO.participants),
                    lastUpdated = convoDTO.lastUpdated
                )
            }
            ChatResult.AllConversationsSuccess(domainList)

        } catch (e: Exception) {
            val errorMsg = e.message ?: "get All Conversations failed"
            Log.e(TAG, "getAllConversations: Error: $errorMsg")
            ChatResult.Error(errorMsg)
        }
    }

    private fun getOtherUser(participants: Map<String, String>): ConversationUser {
        val userId = getUserAuthId()
        val (otherUid, otherUsername) = requireNotNull(participants.entries.firstOrNull { it.key != userId }) {
            "Participants map must contain at least one other user besides $userId."
        }

        return ConversationUser(
            uid = otherUid,
            username = otherUsername
        )
    }

    override fun listenToMessages(chatId: String): Flow<ChatResult> = callbackFlow {
        Log.d(TAG, "listenToMessages: ")
        val userId = getUserAuthId()

        val listenerRegistration = firebaseDb.collection(Constants.DB_CHATS_COLLECTION)
            .document(chatId)
            .collection(Constants.DB_MESSAGES_COLLECTION)
            .orderBy(Constants.DB_TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    trySend(ChatResult.Error(error.message ?: "get messages error"))
                }

                if (snapshot != null) {
                    val msgList = snapshot.toObjects(MessageDTO::class.java).map { msgDTO ->
                        Message(
                            id = msgDTO.id,
                            text = msgDTO.text,
                            isReceivedMessage = msgDTO.senderId != userId,
                            timestamp = msgDTO.timestamp ?: Timestamp.now()
                        )
                    }
                    trySend(ChatResult.ChatMessagesSuccess(msgList))
                }
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun sendMessage(message: String, chatId: String): ChatResult {
        Log.d(TAG, "sendMessage: chatId: $chatId")
        
        return try {
            val userId = auth.currentUser!!.uid

            val parentChatRef = firebaseDb
                .collection(Constants.DB_CHATS_COLLECTION)
                .document(chatId)
            val newMessageRef = parentChatRef
                .collection(Constants.DB_MESSAGES_COLLECTION)
                .document()

            val messageData = hashMapOf(
                Constants.DB_SENDER_ID to userId,
                Constants.DB_TEXT to message,
                Constants.DB_TIMESTAMP to FieldValue.serverTimestamp()
            )
            val parentUpdateData = hashMapOf(
                Constants.DB_LAST_UPDATED to FieldValue.serverTimestamp(),
                Constants.DB_LAST_MESSAGE_TEXT to message,
                Constants.DB_LAST_MESSAGE_SENDER_ID to userId
            )

            // Atomic Batch Write
            val batch = firebaseDb.batch()
            batch.set(newMessageRef, messageData)
            batch.update(parentChatRef, parentUpdateData)
            batch.commit().await()

            ChatResult.MessageSent
        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: error: ${e.message}")
            val errorMsg = e.message ?: "send message failed"

            ChatResult.Error(errorMsg)
        }
    }

    override suspend fun findUserByKeyword(keyword: String): ChatResult {

        return try {
            val userDoc = firebaseDb.collection(Constants.DB_USERNAMES_COLLECTION)
                .document(keyword)
                .get()
                .await()

            val username = userDoc.id
            val uid = userDoc.getString(Constants.DB_UID)

            Log.d(TAG, "getUserByUsername: $username , $uid")
            ChatResult.UserFound(username, uid!!)

        } catch (e: Exception) {
            Log.e(TAG, "getUserByUsername: error: ${e.message}")
            val errorMessage = e.message ?: "get user failed"
            ChatResult.Error(errorMessage)
        }
    }

    override suspend fun createNewChat(otherUsername: String, otherUserId: String): ChatResult {
        Log.d(TAG, "createNewChat: other user ID: $otherUserId")

        return try {
            val userId = getUserAuthId()
            val username = getUserByUid(userId)

            val participants = mapOf(
                userId to username,
                otherUserId to otherUsername
            )

            val chatData = hashMapOf(
                Constants.DB_LAST_UPDATED to FieldValue.serverTimestamp(),
                Constants.DB_PARTICIPANTS to participants
            )

            val chatRef = firebaseDb.collection(Constants.DB_CHATS_COLLECTION).document()
            chatRef.set(chatData).await()

            ChatResult.ChatCreated(chatRef.id)

        } catch (e: Exception) {
            Log.e(TAG, "sendMessage: error: ${e.message}")
            ChatResult.Error(e.message)
        }
    }

    /**
     * check if there is other chat participant message recently sent
     * possible to improve / change this feature
     */
    override suspend fun hasUnreadMessages(): Boolean {

        val currentUserId = auth.currentUser?.uid ?: return false
        val username = dataStoreRepository.username.first()
        val lastMessageSyncTimeStamp = dataStoreRepository.lastMessagesSync.first()?.toFirebaseTimestamp() ?: 0L.toFirebaseTimestamp()
        Log.d(TAG, "hasUnreadMessages: lastMessageSyncTimeStamp: ${lastMessageSyncTimeStamp.toDailyTime()}")

        return try {
            val querySnapshot: QuerySnapshot = firebaseDb.collection(Constants.DB_CHATS_COLLECTION)
                .whereEqualTo("${Constants.DB_PARTICIPANTS}.$currentUserId", username)
                .whereNotEqualTo(Constants.DB_LAST_MESSAGE_SENDER_ID, currentUserId)
                .whereGreaterThan(Constants.DB_LAST_UPDATED, lastMessageSyncTimeStamp)
                .limit(1)
                .get()
                .await()

            dataStoreRepository.saveLastMessagesSync(System.currentTimeMillis())
            !querySnapshot.isEmpty

        } catch (e: Exception) {
            Log.e(TAG, "hasUnreadMessages: error: ${e.message}")
            false
        }
    }

    // TODO: remove and store user data while logged in
    private fun getUserAuthId(): String {
        val userId = requireNotNull(auth.uid) {
            "User Auth ID must not be null after Authentication"
        }
        return userId
    }

    private suspend fun getUserByUid(userId: String): String {

        try {
            val userDoc = firebaseDb.collection(Constants.DB_USERNAMES_COLLECTION)
                .whereEqualTo(Constants.DB_UID, userId)
                .get()
                .await()

            val username = userDoc.first().id
            return username

        } catch (e: Exception) {
            Log.e(TAG, "getUserByUid: error: ${e.message}")
            throw IllegalArgumentException(e.message)
        }
    }

    companion object {
        private const val TAG = "zzChatRepository"
    }
}

sealed class ChatResult {
    data class UserFound(val username: String, val userId: String) : ChatResult()
    data object MessageSent : ChatResult()
    data class ChatCreated(val chatId: String) : ChatResult()
    data class AllConversationsSuccess(val list: List<Conversation>) : ChatResult()
    data class ChatMessagesSuccess(val list: List<Message>) : ChatResult()
    data class Error(val error: String?) : ChatResult()
}