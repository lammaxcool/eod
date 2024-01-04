// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

contract ProcessedIdTracker {
    mapping(string => bool) private processedIds;

    event IdProcessed(string indexed id);

    // Atomic check-and-mark function
    function processId(string memory id) public {
        require(!processedIds[id], "ID already processed");
        processedIds[id] = true;
        emit IdProcessed(id);
    }

    // Function to check if an ID has been processed (optional for external verification)
    function isProcessed(string memory id) public view returns (bool) {
        return processedIds[id];
    }
}
