<?php
require_once('../config/config.php');
require_once('../model/function.php');
header("Content-Type:application/json");

$db_helper = new DbHelper();

if($_SERVER["REQUEST_METHOD"]=="POST"){
$id = $_POST["id"];
$db_helper->deleteMeal($id,$conn);
}
?>