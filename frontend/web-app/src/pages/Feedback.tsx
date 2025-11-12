import React, { useState, useEffect } from 'react';
import { http } from '../api/http';

interface FeedbackSubmission {
  feedbackType: string;
  subject: string;
  message: string;
  rating: number;
  attachments: File[];
}

interface FeedbackItem {
  id: string;
  feedbackType: string;
  subject: string;
  message: string;
  rating: number;
  status: string;
  priority: string;
  assignedTo: string;
  resolutionNotes: string;
  createdAt: string;
  updatedAt: string;
}

const Feedback: React.FC = () => {
  const [feedback, setFeedback] = useState<FeedbackItem[]>([]);
  const [submission, setSubmission] = useState<FeedbackSubmission>({
    feedbackType: 'general',
    subject: '',
    message: '',
    rating: 5,
    attachments: []
  });
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    loadFeedback();
  }, []);

  const loadFeedback = async () => {
    try {
      setLoading(true);
      const response = await http.get('/api/v1/feedback');
      setFeedback(response.data);
    } catch (err) {
      setError('Failed to load feedback');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!submission.subject.trim() || !submission.message.trim()) {
      setError('Please fill in all required fields');
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      
      const formData = new FormData();
      formData.append('feedbackType', submission.feedbackType);
      formData.append('subject', submission.subject);
      formData.append('message', submission.message);
      formData.append('rating', submission.rating.toString());
      
      submission.attachments.forEach((file, index) => {
        formData.append(`attachments`, file);
      });

      await http.post('/api/v1/feedback', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      setSuccess('Feedback submitted successfully!');
      setSubmission({
        feedbackType: 'general',
        subject: '',
        message: '',
        rating: 5,
        attachments: []
      });
      
      // Reload feedback list
      await loadFeedback();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to submit feedback');
    } finally {
      setSubmitting(false);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const files = Array.from(e.target.files);
      setSubmission(prev => ({
        ...prev,
        attachments: [...prev.attachments, ...files]
      }));
    }
  };

  const removeAttachment = (index: number) => {
    setSubmission(prev => ({
      ...prev,
      attachments: prev.attachments.filter((_, i) => i !== index)
    }));
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'open':
        return 'text-yellow-600 bg-yellow-100';
      case 'in_progress':
        return 'text-blue-600 bg-blue-100';
      case 'resolved':
        return 'text-green-600 bg-green-100';
      case 'closed':
        return 'text-gray-600 bg-gray-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority.toLowerCase()) {
      case 'urgent':
        return 'text-red-600 bg-red-100';
      case 'high':
        return 'text-orange-600 bg-orange-100';
      case 'normal':
        return 'text-blue-600 bg-blue-100';
      case 'low':
        return 'text-gray-600 bg-gray-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Feedback & Support</h1>
          <p className="mt-2 text-gray-600">Share your feedback or report issues</p>
        </div>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-md p-4">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-red-800">Error</h3>
                <div className="mt-2 text-sm text-red-700">{error}</div>
              </div>
            </div>
          </div>
        )}

        {success && (
          <div className="mb-6 bg-green-50 border border-green-200 rounded-md p-4">
            <div className="flex">
              <div className="ml-3">
                <h3 className="text-sm font-medium text-green-800">Success</h3>
                <div className="mt-2 text-sm text-green-700">{success}</div>
              </div>
            </div>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Submit Feedback Form */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-6">Submit Feedback</h2>
            
            <form onSubmit={handleSubmit} className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Feedback Type
                </label>
                <select
                  value={submission.feedbackType}
                  onChange={(e) => setSubmission({ ...submission, feedbackType: e.target.value })}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="general">General Feedback</option>
                  <option value="bug_report">Bug Report</option>
                  <option value="feature_request">Feature Request</option>
                  <option value="complaint">Complaint</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Subject *
                </label>
                <input
                  type="text"
                  value={submission.subject}
                  onChange={(e) => setSubmission({ ...submission, subject: e.target.value })}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Brief description of your feedback"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Message *
                </label>
                <textarea
                  value={submission.message}
                  onChange={(e) => setSubmission({ ...submission, message: e.target.value })}
                  rows={6}
                  className="block w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500"
                  placeholder="Please provide detailed information about your feedback"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Rating
                </label>
                <div className="flex space-x-2">
                  {[1, 2, 3, 4, 5].map((rating) => (
                    <button
                      key={rating}
                      type="button"
                      onClick={() => setSubmission({ ...submission, rating })}
                      className={`w-8 h-8 rounded-full border-2 flex items-center justify-center ${
                        rating <= submission.rating
                          ? 'border-yellow-400 bg-yellow-400 text-white'
                          : 'border-gray-300 text-gray-400'
                      }`}
                    >
                      ⭐
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Attachments (Optional)
                </label>
                <input
                  type="file"
                  multiple
                  onChange={handleFileChange}
                  className="block w-full text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                />
                {submission.attachments.length > 0 && (
                  <div className="mt-2 space-y-1">
                    {submission.attachments.map((file, index) => (
                      <div key={index} className="flex items-center justify-between text-sm text-gray-600">
                        <span>{file.name}</span>
                        <button
                          type="button"
                          onClick={() => removeAttachment(index)}
                          className="text-red-600 hover:text-red-800"
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <button
                type="submit"
                disabled={submitting}
                className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50"
              >
                {submitting ? 'Submitting...' : 'Submit Feedback'}
              </button>
            </form>
          </div>

          {/* Feedback History */}
          <div className="bg-white shadow rounded-lg p-6">
            <h2 className="text-lg font-medium text-gray-900 mb-6">Your Feedback</h2>
            
            {loading ? (
              <div className="text-center py-8">
                <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                <p className="mt-2 text-gray-600">Loading feedback...</p>
              </div>
            ) : feedback.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-600">No feedback submitted yet.</p>
              </div>
            ) : (
              <div className="space-y-4">
                {feedback.map((item) => (
                  <div key={item.id} className="border border-gray-200 rounded-lg p-4">
                    <div className="flex items-start justify-between mb-2">
                      <h3 className="text-sm font-medium text-gray-900">
                        {item.subject}
                      </h3>
                      <div className="flex space-x-2">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(item.status)}`}>
                          {item.status}
                        </span>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getPriorityColor(item.priority)}`}>
                          {item.priority}
                        </span>
                      </div>
                    </div>
                    <p className="text-sm text-gray-600 mb-2">
                      {item.message.length > 100 ? `${item.message.substring(0, 100)}...` : item.message}
                    </p>
                    <div className="flex items-center justify-between text-xs text-gray-500">
                      <span>{item.feedbackType} • Rating: {item.rating}/5</span>
                      <span>{new Date(item.createdAt).toLocaleDateString()}</span>
                    </div>
                    {item.resolutionNotes && (
                      <div className="mt-2 p-2 bg-gray-50 rounded text-sm text-gray-700">
                        <strong>Resolution:</strong> {item.resolutionNotes}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Feedback;

