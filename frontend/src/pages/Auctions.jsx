import React, { useState, useEffect } from 'react';
import { Box, Typography, Card, CardContent, CardMedia, Button, TextField } from '@mui/material';
import axios from 'axios';
import { toast } from 'react-toastify';

const Auctions = () => {
  const [products, setProducts] = useState([]);
  const [bidAmount, setBidAmount] = useState('');

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/products');
        setProducts(response.data);
      } catch (error) {
        toast.error('Failed to fetch products');
      }
    };
    fetchProducts();
    const interval = setInterval(fetchProducts, 5000); 
    return () => clearInterval(interval);
  }, []);

  const handleBid = async (productId) => {
    try {
      const token = localStorage.getItem('token');
      await axios.post(`http://localhost:8080/api/products/${productId}/bids`, 
        { amount: bidAmount },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      toast.success('Bid placed successfully!');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Bid failed');
    }
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>Live Auctions</Typography>
      
      {products.map(product => (
        <Card key={product.id} sx={{ mb: 3 }}>
          <CardMedia
            component="img"
            height="140"
            image={`http://localhost:8080/uploads/${product.image}`}
            alt={product.name}
          />
          <CardContent>
            <Typography gutterBottom variant="h5">{product.name}</Typography>
            <Typography variant="body2" color="text.secondary">
              {product.description}
            </Typography>
            <Typography variant="h6" sx={{ mt: 2 }}>
              Current Price: ₹{product.currentPrice || product.basePrice}
            </Typography>
            <Typography variant="body2">
              Ends at: {new Date(product.auctionEnd).toLocaleString()}
            </Typography>
            
            <Box sx={{ display: 'flex', alignItems: 'center', mt: 2 }}>
              <TextField
                size="small"
                label="Your Bid"
                type="number"
                value={bidAmount}
                onChange={(e) => setBidAmount(e.target.value)}
                sx={{ mr: 2 }}
              />
              <Button 
                variant="contained"
                onClick={() => handleBid(product.id)}
              >
                Place Bid
              </Button>
            </Box>
          </CardContent>
        </Card>
      ))}
    </Box>
  );
};

export default Auctions;