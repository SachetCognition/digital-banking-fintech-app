import React, { useState, useEffect, useRef } from 'react';
import { http } from '../api/http';

interface ChatMessage {
  id: string;
  senderId: string;
  senderType: 'customer' | 'agent' | 'system';
  message: string;
  messageType: 'text' | 'image' | 'file' | 'system';
  attachmentUrl?: string;
  attachmentName?: string;
  isRead: boolean;
  readAt?: string;
  createdAt: string;
}

interface ChatSession {
  id: string;
  customerId: string;
  agentId?: string;
  sessionStatus: 'active' | 'closed' | 'transferred';
  priority: 'low' | 'normal' | 'high' | 'urgent';
  subject?: string;
  category: 'general' | 'technical' | 'billing' | 'complaint';
  startedAt: string;
  endedAt?: string;
  lastActivityAt: string;
  messages: ChatMessage[];
}

const ChatSupport: React.FC = () => {
  const [session, setSession] = useState<ChatSession | null>(null);
  const [newMessage, setNewMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    loadOrCreateSession();
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [session?.messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadOrCreateSession = async () => {
    try {
      setLoading(true);
      // Try to load existing active session
      const response = await http.get('/api/v1/chat/sessions/active');
      setSession(response.data);
    } catch (err) {
      // No active session, create a new one
      try {
        const response = await http.post('/api/v1/chat/sessions', {
          category: 'general',
          priority: 'normal'
        });
        setSession(response.data);
      } catch (createErr) {
        setError('Failed to start chat session');
      }
    } finally {
      setLoading(false);
    }
  };

  const sendMessage = async () => {
    if (!newMessage.trim() || !session) return;

    try {
      const response = await http.post(`/api/v1/chat/sessions/${session.id}/messages`, {
        message: newMessage.trim(),
        messageType: 'text'
      });

      // Update local state with new message
      setSession(prev => prev ? {
        ...prev,
        messages: [...prev.messages, response.data]
      } : null);

      setNewMessage('');
    } catch (err) {
      setError('Failed to send message');
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const getSenderName = (message: ChatMessage) => {
    switch (message.senderType) {
      case 'customer':
        return 'You';
      case 'agent':
        return 'Support Agent';
      case 'system':
        return 'System';
      default:
        return 'Unknown';
    }
  };

  const getMessageAlignment = (message: ChatMessage) => {
    return message.senderType === 'customer' ? 'justify-end' : 'justify-start';
  };

  const getMessageColor = (message: ChatMessage) => {
    switch (message.senderType) {
      case 'customer':
        return 'bg-blue-600 text-white';
      case 'agent':
        return 'bg-gray-200 text-gray-900';
      case 'system':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-200 text-gray-900';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">Connecting to support...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 text-6xl mb-4">⚠️</div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">Connection Error</h2>
          <p className="text-gray-600 mb-4">{error}</p>
          <button
            onClick={loadOrCreateSession}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Try Again
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto h-screen flex flex-col">
        {/* Header */}
        <div className="bg-white shadow-sm border-b border-gray-200 px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-lg font-semibold text-gray-900">Live Chat Support</h1>
              <p className="text-sm text-gray-600">
                {session?.agentId ? 'Connected to support agent' : 'Waiting for support agent...'}
              </p>
            </div>
            <div className="flex items-center space-x-2">
              <div className={`w-2 h-2 rounded-full ${session?.agentId ? 'bg-green-500' : 'bg-yellow-500'}`}></div>
              <span className="text-sm text-gray-600">
                {session?.agentId ? 'Online' : 'Connecting...'}
              </span>
            </div>
          </div>
        </div>

        {/* Messages */}
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-4">
          {session?.messages.map((message) => (
            <div key={message.id} className={`flex ${getMessageAlignment(message)}`}>
              <div className={`max-w-xs lg:max-w-md px-4 py-2 rounded-lg ${getMessageColor(message)}`}>
                <div className="text-sm font-medium mb-1">
                  {getSenderName(message)}
                </div>
                <div className="text-sm">
                  {message.message}
                </div>
                <div className="text-xs opacity-75 mt-1">
                  {formatTime(message.createdAt)}
                </div>
              </div>
            </div>
          ))}
          {isTyping && (
            <div className="flex justify-start">
              <div className="bg-gray-200 text-gray-900 px-4 py-2 rounded-lg">
                <div className="flex items-center space-x-1">
                  <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce"></div>
                  <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
                  <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                </div>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* Message Input */}
        <div className="bg-white border-t border-gray-200 px-6 py-4">
          <div className="flex space-x-4">
            <input
              type="text"
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Type your message..."
              className="flex-1 border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              disabled={!session || session.sessionStatus === 'closed'}
            />
            <button
              onClick={sendMessage}
              disabled={!newMessage.trim() || !session || session.sessionStatus === 'closed'}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              Send
            </button>
          </div>
          <p className="text-xs text-gray-500 mt-2">
            Press Enter to send, Shift+Enter for new line
          </p>
        </div>
      </div>
    </div>
  );
};

export default ChatSupport;

