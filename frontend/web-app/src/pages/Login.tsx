import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { http } from '../api/http';

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({ email: '', password: '', mfaCode: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [mfaRequired, setMfaRequired] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const response = await http.post('/api/v1/auth/login', formData);
      if (response.data.mfaRequired) {
        setMfaRequired(true);
      } else {
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('user', JSON.stringify({
          id: response.data.customerId,
          email: response.data.email,
          fullName: response.data.fullName,
          roles: response.data.roles,
        }));
        navigate('/');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleDemoLogin = () => {
    localStorage.setItem('user', JSON.stringify({
      id: 'demo-001',
      email: 'ahmed.alrashid@emiratesdigital.ae',
      fullName: 'Ahmed Al-Rashid',
      roles: ['CUSTOMER', 'ADMIN'],
    }));
    localStorage.setItem('accessToken', 'demo-token');
    navigate('/');
  };

  return (
    <div className="min-h-screen flex">
      {/* Left side - UAE branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-[#0a1628] via-[#0d2847] to-[#1a3a5c] relative overflow-hidden">
        {/* Gold accent pattern */}
        <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZGVmcz48cGF0dGVybiBpZD0iYSIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBwYXR0ZXJuVHJhbnNmb3JtPSJyb3RhdGUoNDUpIj48cGF0aCBkPSJNLTEwIDMwaDYwIiBzdHJva2U9InJnYmEoMjEyLDE3NSw1NSwwLjA2KSIgc3Ryb2tlLXdpZHRoPSIxIi8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI2EpIi8+PC9zdmc+')] opacity-50"></div>
        <div className="relative z-10 flex flex-col justify-center px-16">
          <div className="flex items-center gap-3 mb-10">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#d4af37] to-[#b8960c] flex items-center justify-center text-white font-bold text-2xl shadow-lg shadow-[#d4af37]/20">E</div>
            <div>
              <h1 className="text-white text-xl font-bold tracking-wide">Emirates Digital Bank</h1>
              <p className="text-[#d4af37]/70 text-xs tracking-widest uppercase">Trusted Since 2008</p>
            </div>
          </div>
          <h2 className="text-4xl font-bold text-white leading-tight mb-4">
            Banking Excellence<br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#d4af37] to-[#f0d68a]">for the UAE</span>
          </h2>
          <p className="text-slate-400 text-lg max-w-md leading-relaxed">
            Shariah-compliant digital banking platform, regulated by the Central Bank of the UAE. Your gateway to seamless financial services.
          </p>
          <div className="mt-12 grid grid-cols-3 gap-8">
            {[
              { val: '2M+', label: 'Customers' },
              { val: 'CBUAE', label: 'Regulated' },
              { val: '99.99%', label: 'Uptime SLA' },
            ].map((s) => (
              <div key={s.label}>
                <p className="text-2xl font-bold text-[#d4af37]">{s.val}</p>
                <p className="text-sm text-slate-400 mt-1">{s.label}</p>
              </div>
            ))}
          </div>
          <div className="mt-12 flex items-center gap-4 text-xs text-slate-500">
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
              PCI DSS Certified
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
              ISO 27001
            </span>
            <span className="flex items-center gap-1.5">
              <span className="w-2 h-2 rounded-full bg-emerald-400"></span>
              Shariah Compliant
            </span>
          </div>
        </div>
      </div>

      {/* Right side - form */}
      <div className="flex-1 flex items-center justify-center p-6 bg-[#fafbfc]">
        <div className="w-full max-w-md">
          {/* Mobile logo */}
          <div className="lg:hidden flex items-center gap-3 mb-8">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#d4af37] to-[#b8960c] flex items-center justify-center text-white font-bold text-lg">E</div>
            <h1 className="text-lg font-bold text-slate-800">Emirates Digital Bank</h1>
          </div>

          <div className="mb-8">
            <h2 className="text-2xl font-bold text-slate-800">Ahlan wa Sahlan</h2>
            <p className="text-slate-500 mt-1">Sign in to your Emirates Digital account</p>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1.5">Email / Emirates ID</label>
              <input
                name="email" type="email" required
                className="w-full px-4 py-3 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white"
                placeholder="ahmed@company.ae"
                value={formData.email} onChange={handleChange}
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="text-sm font-medium text-slate-700">Password</label>
                <Link to="/forgot-password" className="text-sm text-[#0d2847] hover:text-[#d4af37] font-medium">Forgot password?</Link>
              </div>
              <input
                name="password" type="password" required
                className="w-full px-4 py-3 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white"
                placeholder="Enter your password"
                value={formData.password} onChange={handleChange}
              />
            </div>

            {mfaRequired && (
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1.5">OTP Code</label>
                <input
                  name="mfaCode" type="text" required
                  className="w-full px-4 py-3 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white"
                  placeholder="Enter 6-digit OTP"
                  value={formData.mfaCode} onChange={handleChange}
                />
              </div>
            )}

            <button
              type="submit" disabled={loading}
              className="w-full py-3 px-4 bg-gradient-to-r from-[#0d2847] to-[#1a4a7a] text-white font-semibold rounded-xl hover:from-[#0a1e3d] hover:to-[#153d66] focus:outline-none focus:ring-2 focus:ring-[#0d2847]/50 transition-all disabled:opacity-50 shadow-lg shadow-[#0d2847]/20"
            >
              {loading ? 'Signing in...' : 'Sign In'}
            </button>

            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center"><div className="w-full border-t border-slate-200"></div></div>
              <div className="relative flex justify-center text-sm"><span className="px-4 bg-[#fafbfc] text-slate-400">or</span></div>
            </div>

            <button
              type="button" onClick={handleDemoLogin}
              className="w-full py-3 px-4 bg-gradient-to-r from-[#d4af37] to-[#c9a22e] text-white font-semibold rounded-xl hover:from-[#c9a22e] hover:to-[#b8960c] transition-all shadow-lg shadow-[#d4af37]/20"
            >
              Continue with Demo Account
            </button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-6">
            New to Emirates Digital? <Link to="/register" className="text-[#0d2847] hover:text-[#d4af37] font-medium">Open an Account</Link>
          </p>

          <p className="text-center text-[10px] text-slate-400 mt-8">
            Regulated by the Central Bank of the UAE. Licensed under CBUAE Reg. No. 2024/DFM/08
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
