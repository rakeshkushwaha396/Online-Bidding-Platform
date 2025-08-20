import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import DashboardLayout from '../components/DashboardLayout';
import '../styles/UserDashboard.css';
import { connectUserWebSocket, disconnectWebSocket } from '../utils/websocket';


const UserDashboard = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tick, setTick] = useState(0);
  const navigate = useNavigate();
  const storedUsername = localStorage.getItem('username') || 'User';

  useEffect(() => {
    fetchAllProducts();

    connectUserWebSocket((newProduct) => {
      setProducts((prev) => [newProduct, ...prev]);
    });

    const intervalId = setInterval(() => {
      setTick((prev) => prev + 1); 
    }, 1000);

    return () => {
      disconnectWebSocket();
      clearInterval(intervalId);
    };
  }, []);

  const fetchAllProducts = async () => {
    try {
      setLoading(true);
      const res = await axios.get('http://localhost:8089/products/all');
      setProducts(res.data);
      setError(null);
    } catch (err) {
      console.error('Failed to load products', err);
      setError('Failed to load products. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

  const getStatus = (start, end) => {
    const now = new Date();
    const startDate = new Date(start);
    const endDate = new Date(end);
    if (now < startDate) return 'Upcoming';
    if (now >= startDate && now <= endDate) return 'Live';
    return 'Ended';
  };

  const getRemainingTime = (end) => {
    const now = new Date();
    const endDate = new Date(end);
    const remaining = endDate - now;

    if (remaining <= 0) return '0d 0h 0m';

    const days = Math.floor(remaining / (1000 * 60 * 60 * 24));
    const hours = Math.floor((remaining / (1000 * 60 * 60)) % 24);
    const minutes = Math.floor((remaining / (1000 * 60)) % 60);

    return `${days}d ${hours}h ${minutes}m`;
  };

  const formatDateTime = (dateString) => {
    return new Date(dateString).toLocaleString();
  };

  return (
    <DashboardLayout>
      <div className="welcome-section">
        <h1>Welcome back, {storedUsername}!</h1>
        <p>Discover amazing auctions or list your own items to get started.</p>
        <div className="stats-container">
          <div className="stat-card">
            <span className="stat-value">{products.length}</span>
            <span className="stat-label">Total Auctions</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">
              {products.filter(p => getStatus(p.auctionStart, p.auctionEnd) === 'Live').length}
            </span>
            <span className="stat-label">Live Auctions</span>
          </div>
          <div className="stat-card">
            <span className="stat-value">
              {products.filter(p => getStatus(p.auctionStart, p.auctionEnd) === 'Upcoming').length}
            </span>
            <span className="stat-label">Upcoming Auctions</span>
          </div>
        </div>
      </div>

      <div className="section-header">
        <h2 className="section-title">Available Auctions</h2>
        <div className="button-wrapper">
          <button
            className="create-auction-btn"
            onClick={() => navigate('/create-auction')}
          >
            + Create Auction
          </button>
        </div>
      </div>

      {loading ? (
        <div className="loading-spinner">
          <div className="spinner"></div>
          <p>Loading products...</p>
        </div>
      ) : error ? (
        <div className="error-message">
          <span className="error-icon">⚠️</span>
          <p>{error}</p>
          <button onClick={fetchAllProducts}>Retry</button>
        </div>
      ) : (
        <div className="product-grid">
          {products.length === 0 ? (
            <div className="no-products">
              <p>No products available at the moment.</p>
              <button
                className="create-auction-btn"
                onClick={() => navigate('/create-auction')}
              >
                Be the first to create one!
              </button>
            </div>
          ) : (
            products.map(product => {
              const status = getStatus(product.auctionStart, product.auctionEnd);
              const remainingTime = getRemainingTime(product.auctionEnd);

              return (
                <div key={product.id} className={`product-card ${status.toLowerCase()}`}>
                  <div className="product-image-container">
                    <img
                      src={`http://localhost:8089${product.imageUrl}`}
                      alt={product.name}
                      onError={(e) => {
                        e.target.onerror = null;
                        e.target.src = '/images/no-image.png';
                      }}
                      className="product-image"
                    />
                    <span className={`product-status-badge ${status.toLowerCase()}`}>
                      {status}
                    </span>
                    <div className="time-remaining">
                      <span className="clock-icon">⏱️</span>
                      {remainingTime}
                    </div>
                  </div>
                  <div className="product-details">
                    <h3>{product.name}</h3>
                    <div className="product-meta">
                      <div className="meta-item">
                        <span className="meta-label">Base Price:</span>
                        <span className="meta-value">₹{product.basePrice.toLocaleString()}</span>
                      </div>
                      <div className="meta-item">
                        <span className="meta-label">Category:</span>
                        <span className="meta-value">{product.category}</span>
                      </div>
                    </div>
                    <div className="product-dates">
                      <div className="date-item">
                        <span className="date-label">Starts:</span>
                        <span className="date-value">{formatDateTime(product.auctionStart)}</span>
                      </div>
                      <div className="date-item">
                        <span className="date-label">Ends:</span>
                        <span className="date-value">{formatDateTime(product.auctionEnd)}</span>
                      </div>
                    </div>
                    <button
                      className="view-btn"
                      onClick={() => navigate(`/product/${product.id}`)}
                    >
                      View Details
                      <span className="arrow-icon">→</span>
                    </button>
                  </div>
                </div>
              );
            })
          )}
        </div>
      )}
    </DashboardLayout>
  );
};

export default UserDashboard;
