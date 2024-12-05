// src/App.js
import React, { useState } from 'react';
import './styles.css'; // Ensure this path is correct
import SearchMortgages from './components/SearchMortgages';
import CalculateRate from './components/CalculateRate';
import PackageMortgages from './components/PackageMortgages';

function App() {
    const [selectedMortgages, setSelectedMortgages] = useState([]);

    return (
        <div className="App">
            <h1>Mortgage Management System</h1>
            <SearchMortgages onSelect={setSelectedMortgages} />
            <CalculateRate selectedMortgages={selectedMortgages} />
            <PackageMortgages selectedMortgages={selectedMortgages} />
        </div>
    );
}

export default App;
