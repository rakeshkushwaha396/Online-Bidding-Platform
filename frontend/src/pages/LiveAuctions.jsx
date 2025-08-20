import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/LiveAuctions.css';
import DashboardLayout from '../components/DashboardLayout';
import { connectUserWebSocket, disconnectWebSocket } from '../utils/websocket';

const categories = ['All', 'Electronics', 'Fashion', 'Antiques', 'Furniture', 'Art & Crafts', 'Automobiles', 'Others'];

const LiveAuctions = () => {
  const [products, setProducts] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [activeCategory, setActiveCategory] = useState('All');
  const [tick, setTick] = useState(0);
  const navigate = useNavigate();

  
  useEffect(() => {
    fetchLiveProducts();

    connectUserWebSocket((newProduct) => {
      const now = new Date();
      const start = new Date(newProduct.auctionStart);
      const end = new Date(newProduct.auctionEnd);

      if (now >= start && now <= end) {
        setProducts(prev => {
          const updated = [newProduct, ...prev];
          
          const unique = [...new Map(updated.map(p => [p.id, p])).values()];
          return unique;
        });
      }
    });

    const intervalId = setInterval(() => setTick(t => t + 1), 1000);

    return () => {
      disconnectWebSocket();
      clearInterval(intervalId);
    };
  }, []);

  
  useEffect(() => {
    if (activeCategory === 'All') {
      setFiltered(products);
    } else {
      const list = products.filter(p => p.category.toLowerCase() === activeCategory.toLowerCase());
      setFiltered(list);
    }
  }, [activeCategory, products]);

  const fetchLiveProducts = async () => {
    try {
      const res = await axios.get('http://localhost:8089/products/live-with-seller');
      const now = new Date();

      const liveOnly = res.data.filter(product => {
        const start = new Date(product.auctionStart);
        const end = new Date(product.auctionEnd);
        return now >= start && now <= end;
      });

      setProducts(liveOnly);
    } catch (err) {
      console.error('❌ Failed to fetch live products:', err);
    }
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

  return (
    <DashboardLayout>
      <div className="live-auction-page">
        <h2 className="page-title">Filter By Category</h2>

        <div className="filter-buttons">
          {categories.map(cat => (
            <button
              key={cat}
              className={`filter-btn ${activeCategory === cat ? 'active' : ''}`}
              onClick={() => setActiveCategory(cat)}
            >
              {cat}
            </button>
          ))}
        </div>

        <h2 className="page-title">Live Auctions</h2>
        {filtered.length === 0 ? (
          <p className="no-products">❌ No live products found in this category.</p>
        ) : (
          <div className="auction-grid">
            {filtered.map(product => {
              const remainingTime = getRemainingTime(product.auctionEnd);

              return (
                <div key={product.id} className="product-card">
                  <img
                    src={`http://localhost:8089${product.imageUrl}`}
                    alt={product.name}
                    onError={(e) => { e.target.src = '/images/no-image.png'; }}
                    className="product-image"
                  />
                  <div className="card-content">
                    <h3>{product.name}</h3>
                    <p className="product-description">{product.description}</p>

                    <div className="meta-grid">
                      <div className="meta-item">
                        <span className="meta-label">Base Price</span>
                        <span className="meta-value price-value">₹{product.basePrice}</span>
                      </div>
                      <div className="meta-item">
                        <span className="meta-label">Category</span>
                        <span className="meta-value">{product.category}</span>
                      </div>
                      <div className="meta-item">
                        <span className="meta-label">Status</span>
                        <span className="meta-value">Live</span>
                      </div>
                      <div className="meta-item">
                        <span className="meta-label">Time Left</span>
                        <span className="meta-value">{remainingTime}</span>
                      </div>
                    </div>

                    <div className="action-bar">
                      <button className="view-btn" onClick={() => navigate(`/product/${product.id}`)}>
                        View Details
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
};

export default LiveAuctions;
