<?php
require_once('../config/config.php');
require_once('../model/function.php');
header("Content-Type:application/json");
$db_helper = new DbHelper();
if($_SERVER["REQUEST_METHOD"]=="POST"){
$name = $_POST["name"];
$details = $_POST["details"];
$price = $_POST["price"];
$quantity = $_POST["quantity"];
$myFile = $_FILES["image"];
$rate = $_POST["rate"];

$db_helper->insertNewMeal($name,$details,$price,$quantity,$myFile,$rate,$conn);
}
?>