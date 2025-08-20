import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { toast, ToastContainer } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import 'react-toastify/dist/ReactToastify.css';
import '../styles/AdminDashboard.css';

const AdminDashboard = () => {
  const [pendingUsers, setPendingUsers] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    if (!isAdmin) {
      navigate('/');
    } else {
      fetchPendingUsers();
    }
  }, [navigate]);

  const fetchPendingUsers = async () => {
    try {
      const res = await axios.get('http://localhost:8089/admin/users');
      setPendingUsers(res.data);
    } catch {
      toast.error('❌ Failed to load pending users');
    }
  };

  const verifyUser = async (id) => {
    try {
      await axios.post(`http://localhost:8089/admin/verify/${id}`);
      toast.success('✅ User verified');
      setPendingUsers((prev) => prev.filter((u) => u.id !== id));
    } catch {
      toast.error('❌ Verification failed');
    }
  };

  const rejectUser = async (id) => {
    try {
      await axios.post(`http://localhost:8089/admin/reject/${id}`);
      toast.info('❌ User rejected');
      setPendingUsers((prev) => prev.filter((u) => u.id !== id));
    } catch {
      toast.error('❌ Rejection failed');
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    toast.info('You have been logged out');
    navigate('/');
  };

  return (
    <div className="admin-dashboard">
      <ToastContainer />
      <div className="dashboard-header">
        <h2>🧾 Pending User Approvals</h2>
        <div className="dashboard-actions">
          <button className="verified-btn" onClick={() => navigate('/admin/verified-users')}>
            👥 View Verified Users
          </button>
          <button className="logout-btn" onClick={handleLogout}>
            🔒 Logout
          </button>
        </div>
      </div>

      {pendingUsers.length === 0 ? (
        <p className="no-users">🎉 No users to approve.</p>
      ) : (
        <div className="card-grid">
          {pendingUsers.map((user) => (
            <div className="card" key={user.id}>
              <h4>{user.fullname}</h4>
              <p><strong>👤 Username:</strong> {user.username}</p>
              <p><strong>📧 Email:</strong> {user.email}</p>
              <p><strong>📞 Mobile:</strong> {user.mobile}</p>
              <p><strong>🆔 Aadhaar No:</strong> {user.aadhaarNo}</p>
              <p><strong>💳 PAN No:</strong> {user.panNo}</p>

              <div className="doc-images">
                <div>
                  <strong>Aadhaar:</strong>
                  <a href={`http://localhost:8089/uploads/${user.aadhaarPhoto}`} target="_blank" rel="noreferrer">
                    <img src={`http://localhost:8089/uploads/${user.aadhaarPhoto}`} alt="Aadhaar" />
                  </a>
                </div>
                <div>
                  <strong>PAN:</strong>
                  <a href={`http://localhost:8089/uploads/${user.panPhoto}`} target="_blank" rel="noreferrer">
                    <img src={`http://localhost:8089/uploads/${user.panPhoto}`} alt="PAN" />
                  </a>
                </div>
              </div>

              <div className="actions">
                <button onClick={() => verifyUser(user.id)} className="approve-btn">✅ Approve</button>
                <button onClick={() => rejectUser(user.id)} className="reject-btn">❌ Reject</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
