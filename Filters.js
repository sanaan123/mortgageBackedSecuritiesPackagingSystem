import React, { useEffect, useState } from "react";
import axios from "../api";

function Filters({ setFilters, onSearch, clearFilters }) {
    const [filterOptions, setFilterOptions] = useState({
    });
    const [selectedFilters, setSelectedFilters] = useState({
    });

    useEffect(() => {
        axios.get("/api/filters").then((response) => {
             console.log("Options fetched:", response.data);
            setFilterOptions(response.data);
        }).catch(error=>{
            console.error("Error fetching filters:", error);
        })
    }, []);

    const handleFilterChange = (event) => {
        const { name, value, type } = event.target;
        let processedValue = value;

        if (type === "checkbox") {
        } else if (
            (name === "msamd" ||
                name === "county" ||
                name === "loan_Type" ||
                name === "loan_Purpose" ||
                name === "property_Type") &&
            value.includes(" ")
        ) {
            processedValue = value.split(" ")[0];
        }

        setSelectedFilters((prevFilters) => ({ ...prevFilters, [name]: processedValue }));
        setFilters((prevFilters) => ({ ...prevFilters, [name]: processedValue }));
    };

    const handleRatioChange = (event) => {
        const { name, value } = event.target;
        setSelectedFilters((prevFilters) => ({ ...prevFilters, [name]: value }));
        setFilters((prevFilters) => ({ ...prevFilters, [name]: value }));
    };

    useEffect(() => {
        if (clearFilters) {
            setSelectedFilters({});
        }
    }, [clearFilters]);

    const handleSearchClick = () => {
        onSearch();
    };

    return (
        <div>
            <h2>Filters</h2>
            {Object.keys(filterOptions).map((filter) => (
                <div key={filter}>
                    <label>{filter}</label>
                    <select
                        name={filter}
                        value={selectedFilters[filter] || ""}
                        onChange={handleFilterChange}
                    >
                        <option value="">Select {filter}</option>
                        {filterOptions[filter].map((option) => (
                            <option key={option} value={option}>
                                {option}
                            </option>
                        ))}
                    </select>
                </div>
            ))}

            <div>
                <label>Min Income-to-Debt Ratio</label>
                <input
                    type="number"
                    name="minIncomeToDebtRatio"
                    value={selectedFilters.minIncomeToDebtRatio || ""}
                    onChange={handleRatioChange}
                />
            </div>
            <div>
                <label>Max Income-to-Debt Ratio</label>
                <input
                    type="number"
                    name="maxIncomeToDebtRatio"
                    value={selectedFilters.maxIncomeToDebtRatio || ""}
                    onChange={handleRatioChange}
                />
            </div>
            <div className="center-button">
                <button onClick={handleSearchClick}>Search</button>
            </div>
            </div>
            );
            }

            export default Filters;
