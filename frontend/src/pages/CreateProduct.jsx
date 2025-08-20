import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';
import '../styles/createproduct.css';
import DashboardLayout from '../components/DashboardLayout'; 

const CreateProduct = () => {
  const [formData, setFormData] = useState({
    userId: '',
    category: '',
    name: '',
    description: '',
    basePrice: '',
    auctionStart: '',
    auctionEnd: '',
  });

  const [image, setImage] = useState(null);

  useEffect(() => {
    const storedUserId = localStorage.getItem('userId');
    setFormData((prev) => ({
      ...prev,
      userId: storedUserId || '1',
    }));
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleFileChange = (e) => {
    setImage(e.target.files[0]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (
      !formData.userId ||
      !formData.category ||
      !formData.name ||
      !formData.description ||
      !formData.basePrice ||
      !formData.auctionStart ||
      !formData.auctionEnd ||
      !image
    ) {
      toast.error('Please fill in all fields and upload an image!');
      return;
    }

    const data = new FormData();
    data.append('category', formData.category);
    data.append('name', formData.name);
    data.append('description', formData.description);
    data.append('basePrice', formData.basePrice);
    data.append('auctionStart', formData.auctionStart + ':00');
    data.append('auctionEnd', formData.auctionEnd + ':00');
    data.append('image', image);

    const token = localStorage.getItem('token');

    try {
  const res = await axios.post('http://localhost:8089/products/upload', data, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': `Bearer ${token}`,
    },
  });

  toast.success('✅ Auction created successfully!');

  setFormData({
    userId: formData.userId,
    category: '',
    name: '',
    description: '',
    basePrice: '',
    auctionStart: '',
    auctionEnd: '',
  });
  setImage(null);
} catch (err) {
  toast.error('❌ Upload failed: ' + (err.response?.data || err.message));
}

  };

  return (
    <DashboardLayout> 
      <div className="auth-box">
        <h2 style={{ marginBottom: '20px' }}>Create Auction</h2>
        <form onSubmit={handleSubmit} encType="multipart/form-data">
          <select
            name="category"
            value={formData.category}
            onChange={handleChange}
            required
          >
            <option value="">Select Category</option>
            <option value="Electronics">Electronics</option>
            <option value="Fashion">Fashion</option>
            <option value="Antiques">Antiques</option>
            <option value="Furniture">Furniture</option>
             <option value="Art & Crafts">Art & Crafts</option>
              <option value="Automobiles">Automobiles</option>
               <option value="Others">Others</option>
          </select>

          <input
            type="text"
            name="name"
            placeholder="Product Name"
            value={formData.name}
            onChange={handleChange}
            required
          />
          <textarea
            name="description"
            placeholder="Product Description"
            value={formData.description}
            onChange={handleChange}
            required
            rows={3}
          />
          <input
            type="number"
            name="basePrice"
            placeholder="Base Price (₹)"
            value={formData.basePrice}
            onChange={handleChange}
            required
          />
          <label style={{ fontWeight: 'bold', marginTop: '10px' }}>Auction Start:</label>
          <input
            type="datetime-local"
            name="auctionStart"
            value={formData.auctionStart}
            onChange={handleChange}
            required
          />
          <label style={{ fontWeight: 'bold', marginTop: '10px' }}>Auction End:</label>
          <input
            type="datetime-local"
            name="auctionEnd"
            value={formData.auctionEnd}
            onChange={handleChange}
            required
          />
          <label style={{ fontWeight: 'bold', marginTop: '10px' }}>Upload Image(jpg, jpeg, png)*:</label>
          <input
            type="file"
            accept="image/*"
            onChange={handleFileChange}
            required
          />
          <button type="submit" style={{ marginTop: '15px' }}>
            Create Auction
          </button>
        </form>
      </div>
    </DashboardLayout>
  );
};

export default CreateProduct;
