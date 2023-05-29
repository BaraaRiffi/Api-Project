<?php
require_once('../config/config.php');
require_once('../model/function.php');
header("Content-Type:application/json");
$db_helper = new DbHelper();

if ($_SERVER["REQUEST_METHOD"] == "GET") {
    $db_helper->getAllMeals($conn);
}
