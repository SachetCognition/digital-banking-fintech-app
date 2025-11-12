import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { http } from '../api/http';

const VerifyEmail: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [token, setToken] = useState('');

  useEffect(() => {
    const tokenParam = searchParams.get('token');
    if (!tokenParam) {
      setError('Invalid verification token.');
      return;
    }
    setToken(tokenParam);
    verifyEmail(tokenParam);
  }, [searchParams]);

  const verifyEmail = async (verificationToken: string) => {
    setLoading(true);
    setError('');
    setMessage('');

    try {
      await http.post('/api/v1/auth/verify-email', null, {
        params: { token: verificationToken }
      });
      setMessage('Email verified successfully! You can now sign in to your account.');
      setTimeout(() => {
        navigate('/login');
      }, 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Email verification failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="text-red-600">{error}</div>
          <button
            onClick={() => navigate('/login')}
            className="mt-4 text-indigo-600 hover:text-indigo-500"
          >
            Go to Sign In
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Verifying your email
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Please wait while we verify your email address
          </p>
        </div>
        
        <div className="mt-8 space-y-6">
          {loading && (
            <div className="text-center">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
              <p className="mt-2 text-sm text-gray-600">Verifying...</p>
            </div>
          )}

          {message && (
            <div className="rounded-md bg-green-50 p-4">
              <div className="text-sm text-green-700">{message}</div>
            </div>
          )}

          {error && (
            <div className="rounded-md bg-red-50 p-4">
              <div className="text-sm text-red-700">{error}</div>
              <div className="mt-2">
                <button
                  onClick={() => verifyEmail(token)}
                  className="text-sm text-indigo-600 hover:text-indigo-500"
                >
                  Try again
                </button>
              </div>
            </div>
          )}

          <div className="text-center">
            <button
              onClick={() => navigate('/login')}
              className="font-medium text-indigo-600 hover:text-indigo-500"
            >
              Go to Sign In
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmail;

