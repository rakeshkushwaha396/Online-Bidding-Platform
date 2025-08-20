import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import '../styles/ViewDetails.css';
import DashboardLayout from '../components/DashboardLayout';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const ViewDetails = () => {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [bidAmount, setBidAmount] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProductDetails();
  }, [id]);

  
  useEffect(() => {
    const socket = new SockJS('http://localhost:8089/ws');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe(`/topic/bids/${id}`, (msg) => {
          const data = JSON.parse(msg.body);
          setProduct(prev => ({
            ...prev,
            currentBid: data.currentBid,
            totalBids: data.totalBids,
          }));
        });
      }
    });

    stompClient.activate();

    return () => {
      if (stompClient && stompClient.active) {
        stompClient.deactivate();
      }
    };
  }, [id]);

  const fetchProductDetails = async () => {
    try {
      const res = await axios.get(`http://localhost:8089/products/details/${id}`);
      setProduct(res.data);
      setMessage('');
    } catch (err) {
      console.error('Fetch error:', err);
      setMessage('Product not found.');
    } finally {
      setLoading(false);
    }
  };

  const handleBidSubmit = async () => {
    const token = localStorage.getItem('token');

    if (!token) {
      alert('Please log in to place a bid.');
      return;
    }

    if (Number(bidAmount) <= product.currentBid) {
      alert(`Bid must be greater than current bid ₹${product.currentBid}`);
      return;
    }

    try {
      const formData = new FormData();
      formData.append('productId', product.productId);
      formData.append('bidAmount', bidAmount);

      const res = await axios.post('http://localhost:8089/bids/place', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${token}`,
        },
      });

      alert('✅ Bid placed successfully!');
      setBidAmount('');
      fetchProductDetails(); 
    } catch (err) {
      console.error('Bid error:', err.response?.data || err.message);
      alert(err.response?.data || 'Failed to place bid.');
    }
  };

  if (loading) return <div className="loader">Loading...</div>;
  if (!product) return <div className="error">{message}</div>;

  return (
    <DashboardLayout>
      <div className="view-details-container">
        <div className="product-view-card">
          <div className="left-section">
            <img
              src={`http://localhost:8089${product.imageUrl}`}
              alt={product.name}
              className="product-image"
              onError={(e) => {
                e.target.src = 'https://via.placeholder.com/400x300?text=No+Image';
              }}
            />
          </div>
          <div className="right-section">
            <h2>{product.name}</h2>
            <p className="description">{product.description}</p>

            <div className="details-grid">
              <p><strong>Base Price:</strong> ₹{product.basePrice}</p>
              <p><strong>Current Bid:</strong> ₹{product.currentBid}</p>
              <p><strong>Total Bids:</strong> {product.totalBids}</p>
              <p><strong>Status:</strong> {product.status}</p>
              <p><strong>Remaining Time:</strong> {product.remainingTime}</p>
              <p><strong>Category:</strong> {product.category || 'N/A'}</p>
              <p><strong>Seller Name:</strong> {product.sellerName || 'Not Available'}</p>
              <p><strong>Seller Number:</strong> {product.sellerMobile || 'Not Available'}</p>
            </div>

            {product.status === 'LIVE' && (
              <div className="bid-box">
                <h4>Place Your Bid</h4>
                <input
                  type="number"
                  min={product.currentBid + 1}
                  placeholder={`Enter amount > ₹${product.currentBid}`}
                  value={bidAmount}
                  onChange={(e) => setBidAmount(e.target.value)}
                />
                <button onClick={handleBidSubmit}>Place Bid</button>
              </div>
            )}

            {message && <div className="message">{message}</div>}
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default ViewDetails;
