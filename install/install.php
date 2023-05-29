<?php
require_once('../config/config.php');

$Meals =  "CREATE TABLE IF NOT EXISTS Meals (
        id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(255) NOT NULL,
        details VARCHAR(255) NOT NULL,
        price FLOAT NOT NULL,
        quantity INT NOT NULL,
        image VARCHAR(255) NOT NULL,
        rate FLOAT NOT NULL
    )";

if ($conn->query($Meals) === TRUE) {
    echo "Table {Meals} created successfully.<br>";
} else {
    echo "Error creating table {$table_name}: " . $conn->error . "<br>";
}
