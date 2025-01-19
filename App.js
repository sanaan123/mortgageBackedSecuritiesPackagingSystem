import React, { useState, useCallback } from "react";
import axios from "axios";
import Filters from "./components/Filters";
import AddMortgage from "./components/AddMortgage";

function App() {
    const [filters, setFilters] = useState({});
    const [totalRows, setTotalRows] = useState(0);
    const [totalLoanAmount, setTotalLoanAmount] = useState(0);
    const [rate, setRate] = useState(null);
    const [rateCalculated, setRateCalculated] = useState(false);

    const handleSearch = useCallback(() => {
        axios.post("/api/search", filters).then((response) => {
            const results = response.data;
            setTotalRows(results.length);
            const totalLoanAmount = results.reduce((sum, result) => sum + result.loan_amount, 0);
            setTotalLoanAmount(totalLoanAmount);
        }).catch((error) => {
            console.error("Error fetching search results:", error);
        });
    }, [filters]);

    const handleCalculateRate = useCallback(() => {
        axios.post("/api/calculateRate", filters).then((response) => {
            const { weightedRate, totalLoanAmount } = response.data;
            setRate(weightedRate);
            setTotalLoanAmount(totalLoanAmount);
            setRateCalculated(true);
        }).catch((error) => {
            console.error("Error calculating rate:", error);
        });
    }, [filters]);

    const handlePackageMortgages = () => {
        axios.post("/api/packageMortgages", filters).then((response) => {
            alert(response.data);
            handleReset();
        }).catch((error) => {
            console.error("Error packaging mortgages:", error);
        });
    };

    const handleReset = () => {
        setFilters({});
        setTotalRows(0);
        setTotalLoanAmount(0);
        setRate(null);
        setRateCalculated(false);
    };

    const formatNumber = (number) => {
        return new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(number);
    };

    return (
        <div className="app-container">
            <h1>Mortgage Application System</h1>
            <Filters setFilters={setFilters} onSearch={handleSearch} clearFilters={handleReset} />

            <div className="section">
                <button onClick={handleReset}>Delete All Filters</button>
            </div>

            {Object.keys(filters).length > 0 && (
                <div className="section">
                    <h2>Selected Filters</h2>
                    <ul>
                        {Object.entries(filters).map(([key, value]) => (
                            <li key={key}>
                                {key === "ownerOccupied"
                                    ? `Owner Occupied: ${value ? "Yes" : "No"}`
                                    : `${key.replace(/([A-Z])/g, ' $1')}: ${value}`}
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            {totalRows > 0 && (
                <div className="section">
                    <h2>Results Summary</h2>
                    <p>Total Rows Matched: {totalRows}</p>
                    <p>Total Loan Amount: ${formatNumber(totalLoanAmount)}</p>
                </div>
            )}

            {!rateCalculated && totalRows > 0 && (
                <div className="section">
                    <button onClick={handleCalculateRate}>Calculate Rate</button>
                </div>
            )}

            {rateCalculated && (
                <div className="section">
                    <h2>Calculated Rate</h2>
                    <p>Rate: {rate !== null ? `${formatNumber(rate)}%` : 'N/A'}</p>
                    <div className="button-group">
                        <button onClick={handlePackageMortgages}>Accept</button>
                        <button onClick={handleReset}>Decline</button>
                    </div>
                </div>
            )}

            <AddMortgage />
        </div>
    );
}

export default App;
