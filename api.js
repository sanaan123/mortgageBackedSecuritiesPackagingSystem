// src/services/api.js
import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:3001', // Use JSON Server URL for mocking
});

export default api;
