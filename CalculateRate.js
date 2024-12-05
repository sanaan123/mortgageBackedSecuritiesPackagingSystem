import React, { useState, useEffect } from 'react';
import api from '../services/api';

function CalculateRate({ selectedMortgages }) {
    const [rate, setRate] = useState(null);
    const [totalCost, setTotalCost] = useState(null);

    useEffect(() => {
        const handleCalculate = () => {
            api.post('/api/mortgages/calculateRate', { mortgages: selectedMortgages })
                .then(response => {
                    setRate(response.data.rate);
                    setTotalCost(response.data.totalCost);
                })
                .catch(error => console.error('Error:', error));
        };

        if (selectedMortgages.length > 0) {
            handleCalculate();
        }
    }, [selectedMortgages]);

    return (
        <div>
            <h2>Calculate Rate</h2>
            {selectedMortgages.length === 0 ? (
                <div>Please select mortgages from the search results.</div>
            ) : (
                <button onClick={() => {
                    const handleCalculate = () => {
                        api.post('/api/mortgages/calculateRate', { mortgages: selectedMortgages })
                            .then(response => {
                                setRate(response.data.rate);
                                setTotalCost(response.data.totalCost);
                            })
                            .catch(error => console.error('Error:', error));
                    };
                    handleCalculate();
                }}>Calculate Rate</button>
            )}
            {rate !== null && totalCost !== null && (
                <div>
                    <div>Calculated Rate: {rate}%</div>
                    <div>Total Cost: ${totalCost}</div>
                </div>
            )}
        </div>
    );
}

export default CalculateRate;
