// src/components/SearchMortgages.js
import React, { useState, useEffect } from 'react';
import api from '../services/api';

function SearchMortgages({ onSelect }) {
    const [filters, setFilters] = useState({
        msamd: '',
        incomeToDebtMin: '',
        incomeToDebtMax: '',
        county: '',
        loanType: '',
        tractToMsamdIncomeMin: '',
        tractToMsamdIncomeMax: '',
        loanPurpose: '',
        propertyType: '',
    });
    const [results, setResults] = useState([]);
    const [selectedMortgages, setSelectedMortgages] = useState([]);
    const [activeFilters, setActiveFilters] = useState([]);
    const [rowCount, setRowCount] = useState(0);
    const [loanAmountSum, setLoanAmountSum] = useState(0);

    // State for dropdown options
    const [msamdOptions, setMsamdOptions] = useState([]);
    const [countyOptions, setCountyOptions] = useState([]);
    const [loanTypeOptions, setLoanTypeOptions] = useState([]);
    const [loanPurposeOptions, setLoanPurposeOptions] = useState([]);
    const [propertyTypeOptions, setPropertyTypeOptions] = useState([]);

    useEffect(() => {
        // Fetch filter options from the backend
        const fetchFilterOptions = async () => {
            try {
                const [msamdRes, countyRes, loanTypeRes, loanPurposeRes, propertyTypeRes] = await Promise.all([
                    api.get('/api/msamds'),
                    api.get('/api/counties'),
                    api.get('/api/loanTypes'),
                    api.get('/api/loanPurposes'),
                    api.get('/api/propertyTypes')
                ]);

                setMsamdOptions(msamdRes.data);
                setCountyOptions(countyRes.data);
                setLoanTypeOptions(loanTypeRes.data);
                setLoanPurposeOptions(loanPurposeRes.data);
                setPropertyTypeOptions(propertyTypeRes.data);
            } catch (error) {
                console.error('Error fetching filter options:', error);
            }
        };

        fetchFilterOptions();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFilters({
            ...filters,
            [name]: value
        });
    };

    const handleSearch = () => {
        api.post('/api/mortgages/search', filters)
            .then(response => {
                setResults(response.data);
                updateActiveFilters();
                updateRowCountAndLoanSum(response.data);
            })
            .catch(error => console.error('Error:', error));
    };

    const updateActiveFilters = () => {
        const active = [];
        for (const key in filters) {
            if (filters[key]) {
                active.push(`${key}=${filters[key]}`);
            }
        }
        setActiveFilters(active);
    };

    const updateRowCountAndLoanSum = (data) => {
        setRowCount(data.length);
        const totalLoanAmount = data.reduce((sum, mortgage) => sum + mortgage.loanAmount, 0);
        setLoanAmountSum(totalLoanAmount);
    };

    const handleSelectMortgage = (e) => {
        const { value, checked } = e.target;
        const updatedSelections = checked
            ? [...selectedMortgages, value]
            : selectedMortgages.filter(id => id !== value);

        setSelectedMortgages(updatedSelections);
        onSelect(updatedSelections);
    };

    const deleteFilter = (filterName) => {
        setFilters({
            ...filters,
            [filterName]: ''
        });
        setActiveFilters(activeFilters.filter(f => !f.startsWith(filterName)));
    };

    const clearFilters = () => {
        setFilters({
            msamd: '',
            incomeToDebtMin: '',
            incomeToDebtMax: '',
            county: '',
            loanType: '',
            tractToMsamdIncomeMin: '',
            tractToMsamdIncomeMax: '',
            loanPurpose: '',
            propertyType: '',
        });
        setActiveFilters([]);
        setRowCount(0);
        setLoanAmountSum(0);
    };

    return (
        <div>
            <h2>Search Mortgages</h2>
            <form>
                <div>
                    <label>MSAMD:</label>
                    <select name="msamd" value={filters.msamd} onChange={handleChange}>
                        <option value="">Select MSAMD</option>
                        {msamdOptions.map(option => (
                            <option key={option} value={option}>{option}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Income to Debt Ratio (Min):</label>
                    <input
                        type="number"
                        name="incomeToDebtMin"
                        value={filters.incomeToDebtMin}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>Income to Debt Ratio (Max):</label>
                    <input
                        type="number"
                        name="incomeToDebtMax"
                        value={filters.incomeToDebtMax}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>County:</label>
                    <select name="county" value={filters.county} onChange={handleChange}>
                        <option value="">Select County</option>
                        {countyOptions.map(option => (
                            <option key={option} value={option}>{option}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Loan Type:</label>
                    <select name="loanType" value={filters.loanType} onChange={handleChange}>
                        <option value="">Select Loan Type</option>
                        {loanTypeOptions.map(option => (
                            <option key={option} value={option}>{option}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Tract to MSAMD Income (Min):</label>
                    <input
                        type="number"
                        name="tractToMsamdIncomeMin"
                        value={filters.tractToMsamdIncomeMin}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>Tract to MSAMD Income (Max):</label>
                    <input
                        type="number"
                        name="tractToMsamdIncomeMax"
                        value={filters.tractToMsamdIncomeMax}
                        onChange={handleChange}
                    />
                </div>
                <div>
                    <label>Loan Purpose:</label>
                    <select name="loanPurpose" value={filters.loanPurpose} onChange={handleChange}>
                        <option value="">Select Loan Purpose</option>
                        {loanPurposeOptions.map(option => (
                            <option key={option} value={option}>{option}</option>
                        ))}
                    </select>
                </div>
                <div>
                    <label>Property Type:</label>
                    <select name="propertyType" value={filters.propertyType} onChange={handleChange}>
                        <option value="">Select Property Type</option>
                        {propertyTypeOptions.map(option => (
                            <option key={option} value={option}>{option}</option>
                        ))}
                    </select>
                </div>
                <button type="button" onClick={handleSearch}>Search</button>
                <button type="button" onClick={clearFilters}>Clear Filters</button>
            </form>
            <div>
                <h3>Active Filters</h3>
                <ul>
                    {activeFilters.map((filter, index) => (
                        <li key={index}>
                            {filter}
                            <button type="button" onClick={() => deleteFilter(filter.split('=')[0])}>Delete</button>
                        </li>
                    ))}
                </ul>
            </div>
            <div>
                <h3>Results Summary</h3>
                <div>Number of Rows: {rowCount}</div>
                <div>Loan Amount Sum: ${loanAmountSum}</div>
            </div>
            <ul>
                {results.map(result => (
                    <li key={result.id}>
                        <label>
                            <input
                                type="checkbox"
                                value={result.id}
                                onChange={handleSelectMortgage}
                            />
                            {result.borrowerName}
                        </label>
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default SearchMortgages;
