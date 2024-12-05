import React, { useState, useEffect } from 'react';
import api from '../services/api';

function PackageMortgages({ selectedMortgages }) {
    const [packageResult, setPackageResult] = useState(null);

    useEffect(() => {
        const handlePackage = () => {
            api.post('/api/mortgages/package', { mortgages: selectedMortgages })
                .then(response => setPackageResult(response.data))
                .catch(error => console.error('Error:', error));
        };

        if (selectedMortgages.length > 0) {
            handlePackage();
        }
    }, [selectedMortgages]);

    return (
        <div>
            <h2>Package Mortgages</h2>
            {selectedMortgages.length === 0 ? (
                <div>Please select mortgages from the search results.</div>
            ) : (
                <button onClick={() => {
                    const handlePackage = () => {
                        api.post('/api/mortgages/package', { mortgages: selectedMortgages })
                            .then(response => setPackageResult(response.data))
                            .catch(error => console.error('Error:', error));
                    };
                    handlePackage();
                }}>Package Mortgages</button>
            )}
            {packageResult && (
                <div>Packaging Result: {packageResult.message}</div>
            )}
        </div>
    );
}

export default PackageMortgages;
