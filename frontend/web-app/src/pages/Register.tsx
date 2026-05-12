import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { http } from '../api/http';

interface RegisterFormData {
  email: string;
  password: string;
  fullName: string;
  phone: string;
  emiratesId: string;
  dateOfBirth: string;
  nationality: string;
  acceptTerms: boolean;
}

const Register: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<RegisterFormData>({
    email: '',
    password: '',
    fullName: '',
    phone: '',
    emiratesId: '',
    dateOfBirth: '',
    nationality: 'AE',
    acceptTerms: false
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await http.post('/api/v1/auth/register', formData);
      setMessage('Registration successful! Your account is being reviewed. You will receive an SMS on your registered mobile.');
      setTimeout(() => { navigate('/login'); }, 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      {/* Left side - UAE branding */}
      <div className="hidden lg:flex lg:w-5/12 bg-gradient-to-br from-[#0a1628] via-[#0d2847] to-[#1a3a5c] relative overflow-hidden">
        <div className="absolute inset-0 bg-[url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48ZGVmcz48cGF0dGVybiBpZD0iYSIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSIgd2lkdGg9IjQwIiBoZWlnaHQ9IjQwIiBwYXR0ZXJuVHJhbnNmb3JtPSJyb3RhdGUoNDUpIj48cGF0aCBkPSJNLTEwIDMwaDYwIiBzdHJva2U9InJnYmEoMjEyLDE3NSw1NSwwLjA2KSIgc3Ryb2tlLXdpZHRoPSIxIi8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI2EpIi8+PC9zdmc+')] opacity-50"></div>
        <div className="relative z-10 flex flex-col justify-center px-12">
          <div className="flex items-center gap-3 mb-10">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#d4af37] to-[#b8960c] flex items-center justify-center text-white font-bold text-2xl shadow-lg shadow-[#d4af37]/20">E</div>
            <div>
              <h1 className="text-white text-xl font-bold tracking-wide">Emirates Digital Bank</h1>
              <p className="text-[#d4af37]/70 text-xs tracking-widest uppercase">Open Your Account</p>
            </div>
          </div>
          <h2 className="text-3xl font-bold text-white leading-tight mb-4">
            Your Gateway to<br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#d4af37] to-[#f0d68a]">Islamic Finance</span>
          </h2>
          <p className="text-slate-400 text-base max-w-sm leading-relaxed">
            Open a Shariah-compliant account in minutes. Enjoy zero-fee banking, competitive profit rates, and 24/7 digital access.
          </p>
          <div className="mt-10 space-y-3">
            {['No minimum balance required', 'Free Visa Signature debit card', 'Instant account activation for UAE nationals', 'Multi-currency wallet included'].map((item) => (
              <div key={item} className="flex items-center gap-3 text-slate-300 text-sm">
                <span className="w-5 h-5 rounded-full bg-[#d4af37]/20 flex items-center justify-center text-[#d4af37] text-xs">&#10003;</span>
                {item}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Right side - form */}
      <div className="flex-1 flex items-center justify-center p-6 bg-[#fafbfc] overflow-y-auto">
        <div className="w-full max-w-lg py-8">
          <div className="lg:hidden flex items-center gap-3 mb-6">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-[#d4af37] to-[#b8960c] flex items-center justify-center text-white font-bold text-lg">E</div>
            <h1 className="text-lg font-bold text-slate-800">Emirates Digital Bank</h1>
          </div>

          <div className="mb-6">
            <h2 className="text-2xl font-bold text-slate-800">Open an Account</h2>
            <p className="text-slate-500 mt-1">Complete the form below to get started</p>
          </div>

          {message && <div className="mb-4 p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm rounded-xl">{message}</div>}
          {error && <div className="mb-4 p-3 bg-red-50 border border-red-200 text-red-700 text-sm rounded-xl">{error}</div>}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="sm:col-span-2">
                <label className="block text-sm font-medium text-slate-700 mb-1">Full Name (as per Emirates ID)</label>
                <input name="fullName" type="text" required
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white text-sm"
                  placeholder="Ahmed Mohammed Al-Rashid" value={formData.fullName} onChange={handleChange} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Email Address</label>
                <input name="email" type="email" required
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white text-sm"
                  placeholder="ahmed@company.ae" value={formData.email} onChange={handleChange} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Mobile Number</label>
                <input name="phone" type="tel" required
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white text-sm"
                  placeholder="+971 50 123 4567" value={formData.phone} onChange={handleChange} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Emirates ID Number</label>
                <input name="emiratesId" type="text" required
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white text-sm"
                  placeholder="784-XXXX-XXXXXXX-X" value={formData.emiratesId} onChange={handleChange} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Date of Birth</label>
                <input name="dateOfBirth" type="date" required
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white text-sm"
                  value={formData.dateOfBirth} onChange={handleChange} />
              </div>
              <div className="sm:col-span-2">
                <label className="block text-sm font-medium text-slate-700 mb-1">Nationality</label>
                <select name="nationality" required
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-slate-800 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white text-sm"
                  value={formData.nationality} onChange={handleChange}>
                  <option value="AE">United Arab Emirates</option>
                  <option value="SA">Saudi Arabia</option>
                  <option value="KW">Kuwait</option>
                  <option value="BH">Bahrain</option>
                  <option value="QA">Qatar</option>
                  <option value="OM">Oman</option>
                  <option value="JO">Jordan</option>
                  <option value="EG">Egypt</option>
                  <option value="LB">Lebanon</option>
                  <option value="IN">India</option>
                  <option value="PK">Pakistan</option>
                  <option value="PH">Philippines</option>
                  <option value="GB">United Kingdom</option>
                  <option value="US">United States</option>
                </select>
              </div>
              <div className="sm:col-span-2">
                <label className="block text-sm font-medium text-slate-700 mb-1">Password</label>
                <input name="password" type="password" required
                  className="w-full px-4 py-2.5 rounded-xl border border-slate-300 text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#d4af37]/20 focus:border-[#d4af37] transition-all bg-white text-sm"
                  placeholder="Min. 8 chars with uppercase, digit, special char" value={formData.password} onChange={handleChange} />
              </div>
            </div>

            <div className="flex items-start gap-2 mt-2">
              <input name="acceptTerms" type="checkbox" required
                className="mt-1 h-4 w-4 text-[#d4af37] border-slate-300 rounded"
                checked={formData.acceptTerms} onChange={handleChange} />
              <label className="text-xs text-slate-600">
                I agree to the <span className="text-[#0d2847] font-medium">Terms & Conditions</span>, <span className="text-[#0d2847] font-medium">Privacy Policy</span>, and confirm this account complies with CBUAE regulations and Shariah principles.
              </label>
            </div>

            <button type="submit" disabled={loading}
              className="w-full py-3 px-4 bg-gradient-to-r from-[#0d2847] to-[#1a4a7a] text-white font-semibold rounded-xl hover:from-[#0a1e3d] hover:to-[#153d66] transition-all disabled:opacity-50 shadow-lg shadow-[#0d2847]/20 mt-4">
              {loading ? 'Creating Account...' : 'Open Account'}
            </button>
          </form>

          <p className="text-center text-sm text-slate-500 mt-5">
            Already have an account? <Link to="/login" className="text-[#0d2847] hover:text-[#d4af37] font-medium">Sign In</Link>
          </p>
          <p className="text-center text-[10px] text-slate-400 mt-4">
            Regulated by the Central Bank of the UAE | Shariah-compliant banking
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
