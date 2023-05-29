<?php
$servername = "localhost";
$serverusername = "root";
$serverpassword = "";
$dbname = "RestaurantDb";

try {
    $conn = new mysqli($servername, $serverusername, $serverpassword);

    if ($conn->connect_error) {
        throw new Exception("Connection failed: " . $conn->connect_error);
    }

    $sql = "CREATE DATABASE IF NOT EXISTS $dbname CHARACTER SET utf8";
    if ($conn->query($sql) === TRUE) {
        
    } else {
        throw new Exception("Error creating database: " . $conn->error);
    }

    $conn->select_db($dbname);


} catch (Exception $e) {
    echo "An error occurred: " . $e->getMessage();
}

?>
