import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { toast, ToastContainer } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import 'react-toastify/dist/ReactToastify.css';
import '../styles/AdminDashboard.css';

const VerifiedUsers = () => {
  const [users, setUsers] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    if (!isAdmin) {
      navigate('/');
    } else {
      fetchUsers();
    }
  }, [navigate]);

  const fetchUsers = async () => {
    try {
      const res = await axios.get('http://localhost:8089/admin/users/all');
      setUsers(res.data);
    } catch {
      toast.error('❌ Failed to fetch verified users');
    }
  };

  const toggleBlock = async (id, shouldBlock) => {
    try {
      await axios.post(`http://localhost:8089/admin/block-user/${id}?block=${shouldBlock}`);
      toast.success(shouldBlock ? '🔒 User Blocked' : '🔓 User Unblocked');
      fetchUsers(); // Refresh the list
    } catch {
      toast.error('❌ Failed to update block status');
    }
  };

  return (
    <div className="admin-dashboard">
      <ToastContainer />
      <button className="back-btn" onClick={() => navigate('/admin/dashboard')}>← Back to Dashboard</button>
      <h2>All Verified Users</h2>
      <div className="card-grid">
        {users.length === 0 ? (
          <p>No verified users available.</p>
        ) : (
          users.map((user) => (
            <div className="card verified" key={user.id}>
              <p><b>Full Name:</b> {user.fullname}</p>
              <p><b>Email:</b> {user.email}</p>
              <p><b>Status:</b> {user.isBlocked ? 'Blocked' : 'Active'}</p>
              <div className="actions">
                {!user.isBlocked ? (
                  <button
                    className="block-btn"
                    onClick={() => toggleBlock(user.id, true)}
                  >
                    🔒 Block
                  </button>
                ) : (
                  <button
                    className="unblock-btn"
                    onClick={() => toggleBlock(user.id, false)}
                  >
                    🔓 Unblock
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default VerifiedUsers;
